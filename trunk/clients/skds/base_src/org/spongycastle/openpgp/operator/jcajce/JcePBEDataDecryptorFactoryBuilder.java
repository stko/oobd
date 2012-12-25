package org.spongycastle.openpgp.operator.jcajce;

import java.security.Provider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.spongycastle.jcajce.DefaultJcaJceHelper;
import org.spongycastle.jcajce.NamedJcaJceHelper;
import org.spongycastle.jcajce.ProviderJcaJceHelper;
import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.operator.PBEDataDecryptorFactory;
import org.spongycastle.openpgp.operator.PGPDataDecryptor;
import org.spongycastle.openpgp.operator.PGPDigestCalculatorProvider;

public class JcePBEDataDecryptorFactoryBuilder
{
    private OperatorHelper helper = new OperatorHelper(new DefaultJcaJceHelper());
    private PGPDigestCalculatorProvider calculatorProvider;

    /**
     * Base constructor.
     *
     * @param calculatorProvider   a digest calculator provider to provide calculators to support the key generation calculation required.
     */
    public JcePBEDataDecryptorFactoryBuilder(PGPDigestCalculatorProvider calculatorProvider)
    {
        this.calculatorProvider = calculatorProvider;
    }

    /**
     * Set the provider object to use for creating cryptographic primitives in the resulting factory the builder produces.
     *
     * @param provider  provider object for cryptographic primitives.
     * @return  the current builder.
     */
    public JcePBEDataDecryptorFactoryBuilder setProvider(Provider provider)
    {
        this.helper = new OperatorHelper(new ProviderJcaJceHelper(provider));

        return this;
    }

    /**
     * Set the provider name to use for creating cryptographic primitives in the resulting factory the builder produces.
     *
     * @param providerName  the name of the provider to reference for cryptographic primitives.
     * @return  the current builder.
     */
    public JcePBEDataDecryptorFactoryBuilder setProvider(String providerName)
    {
        this.helper = new OperatorHelper(new NamedJcaJceHelper(providerName));

        return this;
    }

    public PBEDataDecryptorFactory build(char[] passPhrase)
    {
         return new PBEDataDecryptorFactory(passPhrase, calculatorProvider)
         {
             public byte[] recoverSessionData(int keyAlgorithm, byte[] key, byte[] secKeyData)
                 throws PGPException
             {
                 try
                 {
                     if (secKeyData != null && secKeyData.length > 0)
                     {
                         String cipherName = PGPUtil.getSymmetricCipherName(keyAlgorithm);
                         Cipher keyCipher = helper.createCipher(cipherName + "/CFB/NoPadding");

                         keyCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, cipherName), new IvParameterSpec(new byte[keyCipher.getBlockSize()]));

                         return keyCipher.doFinal(secKeyData);
                     }
                     else
                     {
                         byte[] keyBytes = new byte[key.length + 1];

                         keyBytes[0] = (byte)keyAlgorithm;
                         System.arraycopy(key, 0, keyBytes, 1, key.length);

                         return keyBytes;
                     }
                 }
                 catch (Exception e)
                 {
                     throw new PGPException("Exception recovering session info", e);
                 }
             }

             public PGPDataDecryptor createDataDecryptor(boolean withIntegrityPacket, int encAlgorithm, byte[] key)
                 throws PGPException
             {
                 return helper.createDataDecryptor(withIntegrityPacket, encAlgorithm, key);
             }
         };
    }
}
