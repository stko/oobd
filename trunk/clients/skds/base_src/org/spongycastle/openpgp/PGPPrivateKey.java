package org.spongycastle.openpgp;

import java.security.PrivateKey;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.RSAPrivateCrtKey;

import org.spongycastle.bcpg.BCPGKey;
import org.spongycastle.bcpg.DSASecretBCPGKey;
import org.spongycastle.bcpg.ElGamalSecretBCPGKey;
import org.spongycastle.bcpg.PublicKeyPacket;
import org.spongycastle.bcpg.RSASecretBCPGKey;
import org.spongycastle.jce.interfaces.ElGamalPrivateKey;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPKeyConverter;

/**
 * general class to contain a private key for use with other openPGP
 * objects.
 */
public class PGPPrivateKey
{
    private long          keyID;
    private PrivateKey    privateKey;
    private PublicKeyPacket publicKeyPacket;
    private BCPGKey privateKeyDataPacket;

    /**
     * Create a PGPPrivateKey from a regular private key and the keyID of its associated
     * public key.
     *
     * @param privateKey private key tu use.
     * @param keyID keyID of the corresponding public key.
     */
    public PGPPrivateKey(
        PrivateKey        privateKey,
        long              keyID)
    {
        this.privateKey = privateKey;
        this.keyID = keyID;

        if (privateKey instanceof  RSAPrivateCrtKey)
        {
            RSAPrivateCrtKey rsK = (RSAPrivateCrtKey)privateKey;

            privateKeyDataPacket = new RSASecretBCPGKey(rsK.getPrivateExponent(), rsK.getPrimeP(), rsK.getPrimeQ());
        }
        else if (privateKey instanceof DSAPrivateKey)
        {
            DSAPrivateKey dsK = (DSAPrivateKey)privateKey;

            privateKeyDataPacket = new DSASecretBCPGKey(dsK.getX());
        }
        else if (privateKey instanceof  ElGamalPrivateKey)
        {
            ElGamalPrivateKey esK = (ElGamalPrivateKey)privateKey;

            privateKeyDataPacket = new ElGamalSecretBCPGKey(esK.getX());
        }
        else
        {
            throw new IllegalArgumentException("unknown key class");
        }

    }

    public PGPPrivateKey(
        long keyID,
        PublicKeyPacket publicKeyPacket,
        BCPGKey privateKeyDataPacket)
    {
        this.keyID = keyID;
        this.publicKeyPacket = publicKeyPacket;
        this.privateKeyDataPacket = privateKeyDataPacket;
    }

    /**
     * Return the keyID associated with the contained private key.
     * 
     * @return long
     */
    public long getKeyID()
    {
        return keyID;
    }
    
    /**
     * Return the contained private key.
     * 
     * @return PrivateKey
     * @deprecated use a JcaPGPKeyConverter
     */
    public PrivateKey getKey()
    {
        if (privateKey != null)
        {
            return privateKey;
        }

        try
        {
            return new JcaPGPKeyConverter().setProvider(PGPUtil.getDefaultProvider()).getPrivateKey(this);
        }
        catch (PGPException e)
        {
            throw new IllegalStateException("unable to convert key: " + e.toString());
        }
    }

    public PublicKeyPacket getPublicKeyPacket()
    {
        return publicKeyPacket;
    }

    public BCPGKey getPrivateKeyDataPacket()
    {
        return privateKeyDataPacket;
    }
}
