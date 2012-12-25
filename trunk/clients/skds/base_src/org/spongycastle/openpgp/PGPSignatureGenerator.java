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
import org.spongycastle.bcpg.SignatureSubpacket;
import org.spongycastle.bcpg.SignatureSubpacketTags;
import org.spongycastle.bcpg.UserAttributeSubpacket;
import org.spongycastle.bcpg.sig.IssuerKeyID;
import org.spongycastle.bcpg.sig.SignatureCreationTime;
import org.spongycastle.openpgp.operator.PGPContentSigner;
import org.spongycastle.openpgp.operator.PGPContentSignerBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.spongycastle.util.Strings;

/**
 * Generator for PGP Signatures.
 */
public class PGPSignatureGenerator
{
    private SignatureSubpacket[]    unhashed = new SignatureSubpacket[0];
    private SignatureSubpacket[]    hashed = new SignatureSubpacket[0];
    private OutputStream sigOut;
    private PGPContentSignerBuilder contentSignerBuilder;
    private PGPContentSigner contentSigner;
    private int             sigType;
    private byte            lastb;
    private int providedKeyAlgorithm = -1;

    /**
     * Create a generator for the passed in keyAlgorithm and hashAlgorithm codes.
     *
     * @param keyAlgorithm keyAlgorithm to use for signing
     * @param hashAlgorithm algorithm to use for digest
     * @param provider provider to use for digest algorithm
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws PGPException
     * @deprecated use method taking a PGPContentSignerBuilder
     */
    public PGPSignatureGenerator(
        int     keyAlgorithm,
        int     hashAlgorithm,
        String  provider)
        throws NoSuchAlgorithmException, NoSuchProviderException, PGPException
    {
        this(keyAlgorithm, provider, hashAlgorithm, provider);
    }

    /**
     * Create a generator for the passed in keyAlgorithm and hashAlgorithm codes.
     *
     * @deprecated use method taking a PGPContentSignerBuilder
     */
    public PGPSignatureGenerator(
        int      keyAlgorithm,
        int      hashAlgorithm,
        Provider provider)
        throws NoSuchAlgorithmException, PGPException
    {
        this(keyAlgorithm, provider, hashAlgorithm, provider);
    }

    /**
     * Create a generator for the passed in keyAlgorithm and hashAlgorithm codes.
     *
     * @param keyAlgorithm keyAlgorithm to use for signing
     * @param sigProvider provider to use for signature generation
     * @param hashAlgorithm algorithm to use for digest
     * @param digProvider provider to use for digest algorithm
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws PGPException
     * @deprecated use method taking a PGPContentSignerBuilder
     */
    public PGPSignatureGenerator(
        int     keyAlgorithm,
        String  sigProvider,
        int     hashAlgorithm,
        String  digProvider)
        throws NoSuchAlgorithmException, NoSuchProviderException, PGPException
    {
        this(keyAlgorithm, PGPUtil.getProvider(sigProvider), hashAlgorithm, PGPUtil.getProvider(digProvider));
    }

    /**
     *
     * @param keyAlgorithm
     * @param sigProvider
     * @param hashAlgorithm
     * @param digProvider
     * @throws NoSuchAlgorithmException
     * @throws PGPException
     * @deprecated use constructor taking PGPContentSignerBuilder.
     */
    public PGPSignatureGenerator(
        int      keyAlgorithm,
        Provider sigProvider,
        int      hashAlgorithm,
        Provider digProvider)
        throws NoSuchAlgorithmException, PGPException
    {
        this.providedKeyAlgorithm = keyAlgorithm;
        this.contentSignerBuilder = new JcaPGPContentSignerBuilder(keyAlgorithm, hashAlgorithm).setProvider(sigProvider).setDigestProvider(digProvider);
    }

    /**
     * Create a signature generator built on the passed in contentSignerBuilder.
     *
     * @param contentSignerBuilder  builder to produce PGPContentSigner objects for generating signatures.
     */
    public PGPSignatureGenerator(
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
     * @deprecated use init() method
     */
    public void initSign(
        int             signatureType,
        PGPPrivateKey   key)
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
     * @throws PGPException
     */
    public void init(
        int             signatureType,
        PGPPrivateKey   key)
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
     * @deprecated random parameter now ignored.
     */
    public void initSign(
        int             signatureType,
        PGPPrivateKey   key,
        SecureRandom    random)
        throws PGPException
    {
        initSign(signatureType, key);
    }
    
    public void update(
        byte    b) 
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
        byte[]    b) 
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

    public void setHashedSubpackets(
        PGPSignatureSubpacketVector    hashedPcks)
    {
        if (hashedPcks == null)
        {
            hashed = new SignatureSubpacket[0];
            return;
        }
        
        hashed = hashedPcks.toSubpacketArray();
    }
    
    public void setUnhashedSubpackets(
        PGPSignatureSubpacketVector    unhashedPcks)
    {
        if (unhashedPcks == null)
        {
            unhashed = new SignatureSubpacket[0];
            return;
        }

        unhashed = unhashedPcks.toSubpacketArray();
    }
    
    /**
     * Return the one pass header associated with the current signature.
     * 
     * @param isNested
     * @return PGPOnePassSignature
     * @throws PGPException
     */
    public PGPOnePassSignature generateOnePassVersion(
        boolean    isNested)
        throws PGPException
    {
        return new PGPOnePassSignature(new OnePassSignaturePacket(sigType, contentSigner.getHashAlgorithm(), contentSigner.getKeyAlgorithm(), contentSigner.getKeyID(), isNested));
    }
    
    /**
     * Return a signature object containing the current signature state.
     * 
     * @return PGPSignature
     * @throws PGPException
     * @throws SignatureException
     */
    public PGPSignature generate()
        throws PGPException, SignatureException
    {
        MPInteger[]             sigValues;
        int                     version = 4;
        ByteArrayOutputStream   sOut = new ByteArrayOutputStream();
        SignatureSubpacket[]    hPkts, unhPkts;

        if (!packetPresent(hashed, SignatureSubpacketTags.CREATION_TIME))
        {
            hPkts = insertSubpacket(hashed, new SignatureCreationTime(false, new Date()));
        }
        else
        {
            hPkts = hashed;
        }
        
        if (!packetPresent(hashed, SignatureSubpacketTags.ISSUER_KEY_ID) && !packetPresent(unhashed, SignatureSubpacketTags.ISSUER_KEY_ID))
        {
            unhPkts = insertSubpacket(unhashed, new IssuerKeyID(false, contentSigner.getKeyID()));
        }
        else
        {
            unhPkts = unhashed;
        }
        
        try
        {
            sOut.write((byte)version);
            sOut.write((byte)sigType);
            sOut.write((byte)contentSigner.getKeyAlgorithm());
            sOut.write((byte)contentSigner.getHashAlgorithm());
            
            ByteArrayOutputStream    hOut = new ByteArrayOutputStream();
            
            for (int i = 0; i != hPkts.length; i++)
            {
                hPkts[i].encode(hOut);
            }
                
            byte[]                            data = hOut.toByteArray();
    
            sOut.write((byte)(data.length >> 8));
            sOut.write((byte)data.length);
            sOut.write(data);
        }
        catch (IOException e)
        {
            throw new PGPException("exception encoding hashed data.", e);
        }
        
        byte[]    hData = sOut.toByteArray();
        
        sOut.write((byte)version);
        sOut.write((byte)0xff);
        sOut.write((byte)(hData.length >> 24));
        sOut.write((byte)(hData.length >> 16));
        sOut.write((byte)(hData.length >> 8));
        sOut.write((byte)(hData.length));
        
        byte[]    trailer = sOut.toByteArray();

        blockUpdate(trailer, 0, trailer.length);

        if (contentSigner.getKeyAlgorithm() == PublicKeyAlgorithmTags.RSA_SIGN
            || contentSigner.getKeyAlgorithm() == PublicKeyAlgorithmTags.RSA_GENERAL)    // an RSA signature
        {
            sigValues = new MPInteger[1];
            sigValues[0] = new MPInteger(new BigInteger(1, contentSigner.getSignature()));
        }
        else
        {   
            sigValues = PGPUtil.dsaSigToMpi(contentSigner.getSignature());
        }
        
        byte[]                        digest = contentSigner.getDigest();
        byte[]                        fingerPrint = new byte[2];

        fingerPrint[0] = digest[0];
        fingerPrint[1] = digest[1];
        
        return new PGPSignature(new SignaturePacket(sigType, contentSigner.getKeyID(), contentSigner.getKeyAlgorithm(), contentSigner.getHashAlgorithm(), hPkts, unhPkts, fingerPrint, sigValues));
    }

    /**
     * Generate a certification for the passed in id and key.
     * 
     * @param id the id we are certifying against the public key.
     * @param pubKey the key we are certifying against the id.
     * @return the certification.
     * @throws SignatureException
     * @throws PGPException
     */
    public PGPSignature generateCertification(
        String          id,
        PGPPublicKey    pubKey) 
        throws SignatureException, PGPException
    {
        updateWithPublicKey(pubKey);

        //
        // hash in the id
        //
        updateWithIdData(0xb4, Strings.toUTF8ByteArray(id));

        return this.generate();
    }

    /**
     * Generate a certification for the passed in userAttributes
     * @param userAttributes the id we are certifying against the public key.
     * @param pubKey the key we are certifying against the id.
     * @return the certification.
     * @throws SignatureException
     * @throws PGPException
     */
    public PGPSignature generateCertification(
        PGPUserAttributeSubpacketVector userAttributes,
        PGPPublicKey                    pubKey)
        throws SignatureException, PGPException
    {
        updateWithPublicKey(pubKey);

        //
        // hash in the attributes
        //
        try
        {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            UserAttributeSubpacket[] packets = userAttributes.toSubpacketArray();
            for (int i = 0; i != packets.length; i++)
            {
                packets[i].encode(bOut);
            }
            updateWithIdData(0xd1, bOut.toByteArray());
        }
        catch (IOException e)
        {
            throw new PGPException("cannot encode subpacket array", e);
        }

        return this.generate();
    }

    /**
     * Generate a certification for the passed in key against the passed in
     * master key.
     * 
     * @param masterKey the key we are certifying against.
     * @param pubKey the key we are certifying.
     * @return the certification.
     * @throws SignatureException
     * @throws PGPException
     */
    public PGPSignature generateCertification(
        PGPPublicKey    masterKey,
        PGPPublicKey    pubKey) 
        throws SignatureException, PGPException
    {
        updateWithPublicKey(masterKey);
        updateWithPublicKey(pubKey);
        
        return this.generate();
    }
    
    /**
     * Generate a certification, such as a revocation, for the passed in key.
     * 
     * @param pubKey the key we are certifying.
     * @return the certification.
     * @throws SignatureException
     * @throws PGPException
     */
    public PGPSignature generateCertification(
        PGPPublicKey    pubKey)
        throws SignatureException, PGPException
    {
        updateWithPublicKey(pubKey);

        return this.generate();
    }
    
    private byte[] getEncodedPublicKey(
        PGPPublicKey pubKey) 
        throws PGPException
    {
        byte[]    keyBytes;
        
        try
        {
            keyBytes = pubKey.publicPk.getEncodedContents();
        }
        catch (IOException e)
        {
            throw new PGPException("exception preparing key.", e);
        }
        
        return keyBytes;
    }

    private boolean packetPresent(
        SignatureSubpacket[] packets,
        int type)
    {
        for (int i = 0; i != packets.length; i++)
        {
            if (packets[i].getType() == type)
            {
                return true;
            }
        }

        return false;
    }

    private SignatureSubpacket[] insertSubpacket(
        SignatureSubpacket[] packets,
        SignatureSubpacket subpacket)
    {
        SignatureSubpacket[] tmp = new SignatureSubpacket[packets.length + 1];

        tmp[0] = subpacket;
        System.arraycopy(packets, 0, tmp, 1, packets.length);

        return tmp;
    }

    private void updateWithIdData(int header, byte[] idBytes)
        throws SignatureException
    {
        this.update((byte)header);
        this.update((byte)(idBytes.length >> 24));
        this.update((byte)(idBytes.length >> 16));
        this.update((byte)(idBytes.length >> 8));
        this.update((byte)(idBytes.length));
        this.update(idBytes);
    }

    private void updateWithPublicKey(PGPPublicKey key)
        throws PGPException, SignatureException
    {
        byte[] keyBytes = getEncodedPublicKey(key);

        this.update((byte)0x99);
        this.update((byte)(keyBytes.length >> 8));
        this.update((byte)(keyBytes.length));
        this.update(keyBytes);
    }
}
