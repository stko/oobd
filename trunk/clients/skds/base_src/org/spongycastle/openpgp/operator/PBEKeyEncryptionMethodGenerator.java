package org.spongycastle.openpgp.operator;

import java.security.SecureRandom;

import org.spongycastle.bcpg.ContainedPacket;
import org.spongycastle.bcpg.S2K;
import org.spongycastle.bcpg.SymmetricKeyEncSessionPacket;
import org.spongycastle.openpgp.PGPException;

public abstract class PBEKeyEncryptionMethodGenerator
    extends PGPKeyEncryptionMethodGenerator
{
    private char[] passPhrase;
    private PGPDigestCalculator s2kDigestCalculator;
    private S2K s2k;
    private SecureRandom random;

    protected PBEKeyEncryptionMethodGenerator(
        char[] passPhrase,
        PGPDigestCalculator s2kDigestCalculator)
    {
        this.passPhrase = passPhrase;
        this.s2kDigestCalculator = s2kDigestCalculator;
    }

    public PBEKeyEncryptionMethodGenerator setSecureRandom(SecureRandom random)
    {
        this.random = random;

        return this;
    }

    public byte[] getKey(int encAlgorithm)
        throws PGPException
    {
        if (s2k == null)
        {
            byte[]        iv = new byte[8];

            if (random == null)
            {
                random = new SecureRandom();
            }

            random.nextBytes(iv);

            s2k = new S2K(s2kDigestCalculator.getAlgorithm(), iv, 0x60);
        }

        return PGPUtil.makeKeyFromPassPhrase(s2kDigestCalculator, encAlgorithm, s2k, passPhrase);
    }

    public ContainedPacket generate(int encAlgorithm, byte[] sessionInfo)
        throws PGPException
    {
        byte[] key = getKey(encAlgorithm);

        if (sessionInfo == null)
        {
            return new SymmetricKeyEncSessionPacket(encAlgorithm, s2k, null);
        }

        //
        // the passed in session info has the an RSA/ElGamal checksum added to it, for PBE this is not included.
        //
        byte[] nSessionInfo = new byte[sessionInfo.length - 2];

        System.arraycopy(sessionInfo, 0, nSessionInfo, 0, nSessionInfo.length);

        return new SymmetricKeyEncSessionPacket(encAlgorithm, s2k, encryptSessionInfo(encAlgorithm, key, nSessionInfo));
    }

    abstract protected byte[] encryptSessionInfo(int encAlgorithm, byte[] key, byte[] sessionInfo)
        throws PGPException;
}
