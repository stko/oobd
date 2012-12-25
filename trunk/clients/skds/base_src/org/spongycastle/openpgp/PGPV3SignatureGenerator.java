package org.spongycastle.openpgp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.util.Date;

import org.spongycastle.bcpg.MPInteger;
import org.spongycastle.bcpg.OnePassSignaturePacket;
import org.spongycastle.bcpg.PublicKeyAlgorithmTags;
import org.spongycastle.bcpg.SignaturePacket;
import org.spongycastle.openpgp.operator.PGPContentSigner;
import org.spongycastle.openpgp.operator.PGPContentSignerBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;

/**
 * Generator for old style PGP V3 Signatures.
 */
public class PGPV3SignatureGenerator
{
    private byte            lastb;
    private OutputStream    sigOut;
    private PGPContentSignerBuilder contentSignerBuilder;
    private PGPContentSigner contentSigner;
    private int              sigType;
    private int              providedKeyAlgorithm = -1;

    /**
     * Create a generator for the passed in keyAlgorithm and hashAlgorithm codes.
     * 
     * @param keyAlgorithm
     * @param hashAlgorithm
     * @param provider
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws PGPException
     * @deprecated   use constructor taking PGPContentSignerBuilder.
     */
     public PGPV3SignatureGenerator(
        int  keyAlgorithm,
        int  hashAlgorithm,
        String provider)
        throws NoSuchAlgorithmException, NoSuchProviderException, PGPException
    {
        this(keyAlgorithm, hashAlgorithm, PGPUtil.getProvider(provider));
    }

 /**
     *
     * @param keyAlgorithm
     * @param hashAlgorithm
     * @param provider
     * @throws NoSuchAlgorithmException
     * @throws PGPException
     * @deprecated use constructor taking PGPContentSignerBuilder.
     */
    public PGPV3SignatureGenerator(
        int      keyAlgorithm,
        int      hashAlgorithm,
        Provider provider)
        throws NoSuchAlgorithmException, PGPException
    {
        this.providedKeyAlgorithm = keyAlgorithm;
        this.contentSignerBuilder = new JcaPGPContentSignerBuilder(keyAlgorithm, hashAlgorithm).setProvider(provider);
    }

    /**
     * Create a signature generator built on the passed in contentSignerBuilder.
     *
     * @param contentSignerBuilder  builder to produce PGPContentSigner objects for generating signatures.
     */
    public PGPV3SignatureGenerator(
        PGPContentSignerBuilder contentSignerBuilder)
    {
        this.contentSignerBuilder = contentSignerBuilder;
    }
    
    /**
     * Initialise the generator for signing.
     * 
     * @param signatureType
     * @param key
     * @throws PGPException
     */
    public void init(
        int           signatureType,
        PGPPrivateKey key)
        throws PGPException
    {
        contentSigner = contentSignerBuilder.build(signatureType, key);
        sigOut = contentSigner.getOutputStream();
        sigType = contentSigner.getType();
        lastb = 0;

        if (providedKeyAlgorithm >= 0 && providedKeyAlgorithm != contentSigner.getKeyAlgorithm())
        {
            throw new PGPException("key algorithm mismatch");
        }
    }

    /**
     * Initialise the generator for signing.
     * 
     * @param signatureType
     * @param key
     * @param random
     * @throws PGPException
     * @deprecated random now ignored - set random in PGPContentSignerBuilder
     */
    public void initSign(
        int           signatureType,
        PGPPrivateKey key,
        SecureRandom  random)
        throws PGPException
    {
        init(signatureType, key);
    }

    /**
     * Initialise the generator for signing.
     *
     * @param signatureType
     * @param key
     * @throws PGPException
     * @deprecated use init()
     */
    public void initSign(
        int           signatureType,
        PGPPrivateKey key)
        throws PGPException
    {
        init(signatureType, key);
    }

    public void update(
        byte b) 
        throws SignatureException
    {
        if (sigType == PGPSignature.CANONICAL_TEXT_DOCUMENT)
        {
            if (b == '\r')
            {
                byteUpdate((byte)'\r');
                byteUpdate((byte)'\n');
            }
            else if (b == '\n')
            {
                if (lastb != '\r')
                {
                    byteUpdate((byte)'\r');
                    byteUpdate((byte)'\n');
                }
            }
            else
            {
                byteUpdate(b);
            }
            
            lastb = b;
        }
        else
        {
            byteUpdate(b);
        }
    }
    
    public void update(
        byte[] b) 
        throws SignatureException
    {
        this.update(b, 0, b.length);
    }
    
    public void update(
        byte[]  b,
        int     off,
        int     len) 
        throws SignatureException
    {
        if (sigType == PGPSignature.CANONICAL_TEXT_DOCUMENT)
        {
            int finish = off + len;
            
            for (int i = off; i != finish; i++)
            {
                this.update(b[i]);
            }
        }
        else
        {
            blockUpdate(b, off, len);
        }
    }

    private void byteUpdate(byte b)
        throws SignatureException
    {
        try
        {
            sigOut.write(b);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("unable to update signature");
        }
    }

    private void blockUpdate(byte[] block, int off, int len)
        throws SignatureException
    {
        try
        {
            sigOut.write(block, off, len);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("unable to update signature");
        }
    }

    /**
     * Return the one pass header associated with the current signature.
     * 
     * @param isNested
     * @return PGPOnePassSignature
     * @throws PGPException
     */
    public PGPOnePassSignature generateOnePassVersion(
        boolean isNested)
        throws PGPException
    {
        return new PGPOnePassSignature(new OnePassSignaturePacket(sigType, contentSigner.getHashAlgorithm(), contentSigner.getKeyAlgorithm(), contentSigner.getKeyID(), isNested));
    }
    
    /**
     * Return a V3 signature object containing the current signature state.
     * 
     * @return PGPSignature
     * @throws PGPException
     * @throws SignatureException
     */
    public PGPSignature generate()
        throws PGPException, SignatureException
    {
        long creationTime = new Date().getTime() / 1000;

        ByteArrayOutputStream sOut = new ByteArrayOutputStream();

        sOut.write(sigType);
        sOut.write((byte)(creationTime >> 24));
        sOut.write((byte)(creationTime >> 16));
        sOut.write((byte)(creationTime >> 8));
        sOut.write((byte)creationTime);

        byte[] hData = sOut.toByteArray();

        blockUpdate(hData, 0, hData.length);

        MPInteger[] sigValues;
        if (contentSigner.getKeyAlgorithm() == PublicKeyAlgorithmTags.RSA_SIGN
            || contentSigner.getKeyAlgorithm() == PublicKeyAlgorithmTags.RSA_GENERAL)
            // an RSA signature
        {
            sigValues = new MPInteger[1];
            sigValues[0] = new MPInteger(new BigInteger(1, contentSigner.getSignature()));
        }
        else
        {
            sigValues = PGPUtil.dsaSigToMpi(contentSigner.getSignature());
        }

        byte[] digest = contentSigner.getDigest();
        byte[] fingerPrint = new byte[2];

        fingerPrint[0] = digest[0];
        fingerPrint[1] = digest[1];

        return new PGPSignature(new SignaturePacket(3, contentSigner.getType(), contentSigner.getKeyID(), contentSigner.getKeyAlgorithm(), contentSigner.getHashAlgorithm(), creationTime * 1000, fingerPrint, sigValues));
    }
}
