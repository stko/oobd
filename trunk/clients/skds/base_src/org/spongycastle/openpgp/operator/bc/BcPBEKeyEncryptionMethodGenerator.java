package org.spongycastle.openpgp.operator.bc;

import java.security.SecureRandom;

import org.spongycastle.crypto.BlockCipher;
import org.spongycastle.crypto.BufferedBlockCipher;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.operator.PBEKeyEncryptionMethodGenerator;
import org.spongycastle.openpgp.operator.PGPDigestCalculator;

/**
 * A BC lightweight method generator for supporting PBE based encryption operations.
 */
public class BcPBEKeyEncryptionMethodGenerator
    extends PBEKeyEncryptionMethodGenerator
{
    /**
     *  Create a PBE encryption method generator using the provided calculator for key calculation.
     *
     * @param passPhrase  the passphrase to use as the primary source of key material.
     * @param s2kDigestCalculator  the digest calculator to use for key calculation.
     */
    public BcPBEKeyEncryptionMethodGenerator(char[] passPhrase, PGPDigestCalculator s2kDigestCalculator)
    {
        super(passPhrase, s2kDigestCalculator);
    }

    /**
     * Create a PBE encryption method generator using the default SHA-1 digest calculator for key calculation.
     *
     * @param passPhrase  the passphrase to use as the primary source of key material.
     */
    public BcPBEKeyEncryptionMethodGenerator(char[] passPhrase)
    {
        this(passPhrase, new SHA1PGPDigestCalculator());
    }

    /**
     * Provide a user defined source of randomness.
     *
     * @param random  the secure random to be used.
     * @return  the current generator.
     */
    public PBEKeyEncryptionMethodGenerator setSecureRandom(SecureRandom random)
    {
        super.setSecureRandom(random);

        return this;
    }

    protected byte[] encryptSessionInfo(int encAlgorithm, byte[] key, byte[] sessionInfo)
        throws PGPException
    {
        try
        {
            BlockCipher engine = BcImplProvider.createBlockCipher(encAlgorithm);
            BufferedBlockCipher cipher = BcUtil.createSymmetricKeyWrapper(true, engine, key, new byte[engine.getBlockSize()]);

            byte[] out = new byte[sessionInfo.length];

            int len = cipher.processBytes(sessionInfo, 0, sessionInfo.length, out, 0);

            len += cipher.doFinal(out, len);

            return out;
        }
        catch (InvalidCipherTextException e)
        {
            throw new PGPException("encryption failed: " + e.getMessage(), e);
        }
    }
}
