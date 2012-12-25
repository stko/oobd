package org.spongycastle.openpgp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SignatureException;

import org.spongycastle.bcpg.BCPGInputStream;
import org.spongycastle.bcpg.BCPGOutputStream;
import org.spongycastle.bcpg.OnePassSignaturePacket;
import org.spongycastle.openpgp.operator.PGPContentVerifier;
import org.spongycastle.openpgp.operator.PGPContentVerifierBuilder;
import org.spongycastle.openpgp.operator.PGPContentVerifierBuilderProvider;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPContentVerifierBuilderProvider;

/**
 * A one pass signature object.
 */
public class PGPOnePassSignature
{
    private OnePassSignaturePacket sigPack;
    private int                    signatureType;

    private PGPContentVerifier verifier;
    private byte               lastb;
    private OutputStream       sigOut;

    PGPOnePassSignature(
        BCPGInputStream    pIn)
        throws IOException, PGPException
    {
        this((OnePassSignaturePacket)pIn.readPacket());
    }
    
    PGPOnePassSignature(
        OnePassSignaturePacket    sigPack)
        throws PGPException
    {
        this.sigPack = sigPack;
        this.signatureType = sigPack.getSignatureType();
    }
    
    /**
     * Initialise the signature object for verification.
     * 
     * @param pubKey
     * @param provider
     * @throws NoSuchProviderException
     * @throws PGPException
     * @deprecated use init() method.
     */
    public void initVerify(
        PGPPublicKey    pubKey,
        String          provider)
        throws NoSuchProviderException, PGPException
    {
        initVerify(pubKey, PGPUtil.getProvider(provider));
    }

        /**
     * Initialise the signature object for verification.
     *
     * @param pubKey
     * @param provider
     * @throws NoSuchProviderException
     * @throws PGPException
     * @deprecated use init() method.
     */
    public void initVerify(
        PGPPublicKey    pubKey,
        Provider        provider)
        throws PGPException
    {
        init(new JcaPGPContentVerifierBuilderProvider().setProvider(provider), pubKey);
    }

    /**
     * Initialise the signature object for verification.
     *
     * @param verifierBuilderProvider   provider for a content verifier builder for the signature type of interest.
     * @param pubKey  the public key to use for verification
     * @throws PGPException if there's an issue with creating the verifier.
     */
    public void init(PGPContentVerifierBuilderProvider verifierBuilderProvider, PGPPublicKey pubKey)
        throws PGPException
    {
        PGPContentVerifierBuilder verifierBuilder = verifierBuilderProvider.get(sigPack.getKeyAlgorithm(), sigPack.getHashAlgorithm());

        verifier = verifierBuilder.build(pubKey);

        lastb = 0;
        sigOut = verifier.getOutputStream();
    }

    public void update(
        byte    b)
        throws SignatureException
    {
        if (signatureType == PGPSignature.CANONICAL_TEXT_DOCUMENT)
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
        byte[]    bytes)
        throws SignatureException
    {
        if (signatureType == PGPSignature.CANONICAL_TEXT_DOCUMENT)
        {
            for (int i = 0; i != bytes.length; i++)
            {
                this.update(bytes[i]);
            }
        }
        else
        {
            blockUpdate(bytes, 0, bytes.length);
        }
    }
    
    public void update(
        byte[]    bytes,
        int       off,
        int       length)
        throws SignatureException
    {
        if (signatureType == PGPSignature.CANONICAL_TEXT_DOCUMENT)
        {
            int finish = off + length;
            
            for (int i = off; i != finish; i++)
            {
                this.update(bytes[i]);
            }
        }
        else
        {
            blockUpdate(bytes, off, length);
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
        {             // TODO: we really should get rid of signature exception next....
            throw new SignatureException(e.getMessage());
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
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * Verify the calculated signature against the passed in PGPSignature.
     * 
     * @param pgpSig
     * @return boolean
     * @throws PGPException
     * @throws SignatureException
     */
    public boolean verify(
        PGPSignature    pgpSig)
        throws PGPException, SignatureException
    {
        try
        {
            sigOut.write(pgpSig.getSignatureTrailer());

            sigOut.close();
        }
        catch (IOException e)
        {
            throw new PGPException("unable to add trailer: " + e.getMessage(), e);
        }

        return verifier.verify(pgpSig.getSignature());
    }
    
    public long getKeyID()
    {
        return sigPack.getKeyID();
    }
    
    public int getSignatureType()
    {
        return sigPack.getSignatureType();
    }

    public int getHashAlgorithm()
    {
        return sigPack.getHashAlgorithm();
    }

    public int getKeyAlgorithm()
    {
        return sigPack.getKeyAlgorithm();
    }

    public byte[] getEncoded()
        throws IOException
    {
        ByteArrayOutputStream    bOut = new ByteArrayOutputStream();
        
        this.encode(bOut);
        
        return bOut.toByteArray();
    }
    
    public void encode(
        OutputStream    outStream) 
        throws IOException
    {
        BCPGOutputStream    out;
        
        if (outStream instanceof BCPGOutputStream)
        {
            out = (BCPGOutputStream)outStream;
        }
        else
        {
            out = new BCPGOutputStream(outStream);
        }

        out.writePacket(sigPack);
    }
}
