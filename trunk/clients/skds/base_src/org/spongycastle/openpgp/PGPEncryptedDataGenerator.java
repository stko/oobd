package org.spongycastle.openpgp;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.spongycastle.bcpg.BCPGOutputStream;
import org.spongycastle.bcpg.HashAlgorithmTags;
import org.spongycastle.bcpg.PacketTags;
import org.spongycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openpgp.operator.PBEKeyEncryptionMethodGenerator;
import org.spongycastle.openpgp.operator.PGPDataEncryptor;
import org.spongycastle.openpgp.operator.PGPDataEncryptorBuilder;
import org.spongycastle.openpgp.operator.PGPDigestCalculator;
import org.spongycastle.openpgp.operator.PGPKeyEncryptionMethodGenerator;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcePBEKeyEncryptionMethodGenerator;
import org.spongycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator;
import org.spongycastle.util.io.TeeOutputStream;

/**
 *  Generator for encrypted objects.
 */
public class PGPEncryptedDataGenerator
    implements SymmetricKeyAlgorithmTags, StreamGenerator
{
    /**
     * Specifier for SHA-1 S2K PBE generator.
     */
    public static final int S2K_SHA1 = HashAlgorithmTags.SHA1;

    /**
     * Specifier for SHA-224 S2K PBE generator.
     */
    public static final int S2K_SHA224 = HashAlgorithmTags.SHA224;

    /**
     * Specifier for SHA-256 S2K PBE generator.
     */
    public static final int S2K_SHA256 = HashAlgorithmTags.SHA256;

    /**
     * Specifier for SHA-384 S2K PBE generator.
     */
    public static final int S2K_SHA384 = HashAlgorithmTags.SHA384;

    /**
     * Specifier for SHA-512 S2K PBE generator.
     */
    public static final int S2K_SHA512 = HashAlgorithmTags.SHA512;

    private BCPGOutputStream     pOut;
    private OutputStream         cOut;
    private boolean              oldFormat = false;
    private PGPDigestCalculator digestCalc;
    private OutputStream            genOut;
    private PGPDataEncryptorBuilder dataEncryptorBuilder;

    private List            methods = new ArrayList();
    private int             defAlgorithm;
    private SecureRandom    rand;
    private Provider        defProvider;
    
   /**
       * Base constructor.
       *
       * @param encAlgorithm the symmetric algorithm to use.
       * @param rand source of randomness
       * @param provider the provider name to use for encryption algorithms.
       * @deprecated  use constructor that takes a PGPDataEncryptor
       */
    public PGPEncryptedDataGenerator(
        int                 encAlgorithm,
        SecureRandom        rand,
        String              provider)
    {
        this(new JcePGPDataEncryptorBuilder(encAlgorithm).setSecureRandom(rand).setProvider(provider));
    }

   /**
       * Base constructor.
       *
       * @param encAlgorithm the symmetric algorithm to use.
       * @param rand source of randomness
       * @param provider the provider to use for encryption algorithms.
       * @deprecated  use constructor that takes a PGPDataEncryptorBuilder
       */
    public PGPEncryptedDataGenerator(
        int                 encAlgorithm,
        SecureRandom        rand,
        Provider            provider)
    {
        this(new JcePGPDataEncryptorBuilder(encAlgorithm).setSecureRandom(rand).setProvider(provider));
    }

    /**
        * Creates a cipher stream which will have an integrity packet
        * associated with it.
        *
        * @param encAlgorithm
        * @param withIntegrityPacket
        * @param rand
        * @param provider
        * @deprecated  use constructor that takes a PGPDataEncryptorBuilder
        */
    public PGPEncryptedDataGenerator(
        int                 encAlgorithm,
        boolean             withIntegrityPacket,
        SecureRandom        rand,
        String              provider)
    {
        this(new JcePGPDataEncryptorBuilder(encAlgorithm).setWithIntegrityPacket(withIntegrityPacket).setSecureRandom(rand).setProvider(provider));
    }

    /**
        * Creates a cipher stream which will have an integrity packet
        * associated with it.
        *
        * @param encAlgorithm
        * @param withIntegrityPacket
        * @param rand
        * @param provider
        * @deprecated  use constructor that takes a PGPDataEncryptorBuilder
        */
    public PGPEncryptedDataGenerator(
        int                 encAlgorithm,
        boolean             withIntegrityPacket,
        SecureRandom        rand,
        Provider            provider)
    {
        this(new JcePGPDataEncryptorBuilder(encAlgorithm).setWithIntegrityPacket(withIntegrityPacket).setSecureRandom(rand).setProvider(provider));
    }

   /**
       * Base constructor.
       *
       * @param encAlgorithm the symmetric algorithm to use.
       * @param rand source of randomness
       * @param oldFormat PGP 2.6.x compatibility required.
       * @param provider the provider to use for encryption algorithms.
       * @deprecated  use constructor that takes a PGPDataEncryptorBuilder
       */
    public PGPEncryptedDataGenerator(
        int                 encAlgorithm,
        SecureRandom        rand,
        boolean             oldFormat,
        String              provider)
    {
        this(new JcePGPDataEncryptorBuilder(encAlgorithm).setSecureRandom(rand).setProvider(provider), oldFormat);
    }

   /**
       * Base constructor.
       *
       * @param encAlgorithm the symmetric algorithm to use.
       * @param rand source of randomness
       * @param oldFormat PGP 2.6.x compatibility required.
       * @param provider the provider to use for encryption algorithms.
       * @deprecated  use constructor that takes a PGPDataEncryptorBuilder
       */
    public PGPEncryptedDataGenerator(
        int                 encAlgorithm,
        SecureRandom        rand,
        boolean             oldFormat,
        Provider            provider)
    {
        this(new JcePGPDataEncryptorBuilder(encAlgorithm).setSecureRandom(rand).setProvider(provider), oldFormat);
    }

   /**
       * Base constructor.
       *
       * @param encryptorBuilder builder to create actual data encryptor.
       */
    public PGPEncryptedDataGenerator(PGPDataEncryptorBuilder encryptorBuilder)
    {
        this(encryptorBuilder, false);
    }

   /**
       * Base constructor with the option to turn on formatting for PGP 2.6.x compatibility.
       *
       * @param encryptorBuilder builder to create actual data encryptor.
       * @param oldFormat PGP 2.6.x compatibility required.
       */
    public PGPEncryptedDataGenerator(PGPDataEncryptorBuilder encryptorBuilder, boolean oldFormat)
    {
        this.dataEncryptorBuilder = encryptorBuilder;
        this.oldFormat = oldFormat;

        this.defAlgorithm = dataEncryptorBuilder.getAlgorithm();
        this.rand = dataEncryptorBuilder.getSecureRandom();
    }

    /**
     * Add a PBE encryption method to the encrypted object using the default algorithm (S2K_SHA1).
     * 
     * @param passPhrase
     * @throws NoSuchProviderException
     * @throws PGPException
     * @deprecated  use addMethod that takes  PGPKeyEncryptionMethodGenerator
     */
    public void addMethod(
        char[]    passPhrase) 
        throws NoSuchProviderException, PGPException
    {
        addMethod(passPhrase, HashAlgorithmTags.SHA1);
    }

    /**
     * Add a PBE encryption method to the encrypted object.
     *
     * @param passPhrase passphrase to use to generate key.
     * @param s2kDigest digest algorithm to use for S2K calculation
     * @throws NoSuchProviderException
     * @throws PGPException
     * @deprecated  use addMethod that takes  PGPKeyEncryptionMethodGenerator
     */
    public void addMethod(
        char[]    passPhrase,
        int       s2kDigest)
        throws NoSuchProviderException, PGPException
    {
        if (defProvider == null)
        {
            defProvider = new BouncyCastleProvider();
        }

        addMethod(new JcePBEKeyEncryptionMethodGenerator(passPhrase, new JcaPGPDigestCalculatorProviderBuilder().setProvider(defProvider).build().get(s2kDigest)).setProvider(defProvider).setSecureRandom(rand));
    }

    /**
     * Add a public key encrypted session key to the encrypted object.
     * 
     * @param key
     * @throws NoSuchProviderException
     * @throws PGPException
     * @deprecated  use addMethod that takes  PGPKeyEncryptionMethodGenerator
     */
    public void addMethod(
        PGPPublicKey    key) 
        throws NoSuchProviderException, PGPException
    {   
        if (!key.isEncryptionKey())
        {
            throw new IllegalArgumentException("passed in key not an encryption key!");
        }

        if (defProvider == null)
        {
            defProvider = new BouncyCastleProvider();
        }

        addMethod(new JcePublicKeyKeyEncryptionMethodGenerator(key).setProvider(defProvider).setSecureRandom(rand));
    }

    /**
        *  Added a key encryption method to be used to encrypt the session data associated
        *  with this encrypted data.
        *
        * @param method  key encryption method to use.
        */
    public void addMethod(PGPKeyEncryptionMethodGenerator method)
    {
        methods.add(method);
    }

    private void addCheckSum(
        byte[]    sessionInfo)
    {
        int    check = 0;
        
        for (int i = 1; i != sessionInfo.length - 2; i++)
        {
            check += sessionInfo[i] & 0xff;
        }
        
        sessionInfo[sessionInfo.length - 2] = (byte)(check >> 8);
        sessionInfo[sessionInfo.length - 1] = (byte)(check);
    }

    private byte[] createSessionInfo(
        int     algorithm,
        byte[]  keyBytes)
    {
        byte[] sessionInfo = new byte[keyBytes.length + 3];
        sessionInfo[0] = (byte) algorithm;
        System.arraycopy(keyBytes, 0, sessionInfo, 1, keyBytes.length);
        addCheckSum(sessionInfo);
        return sessionInfo;
    }

    /**
     * If buffer is non null stream assumed to be partial, otherwise the
     * length will be used to output a fixed length packet.
     * <p>
     * The stream created can be closed off by either calling close()
     * on the stream or close() on the generator. Closing the returned
     * stream does not close off the OutputStream parameter out.
     * 
     * @param out
     * @param length
     * @param buffer
     * @return
     * @throws java.io.IOException
     * @throws PGPException
     * @throws IllegalStateException
     */
    private OutputStream open(
        OutputStream    out,
        long            length,
        byte[]          buffer)
        throws IOException, PGPException, IllegalStateException
    {
        if (cOut != null)
        {
            throw new IllegalStateException("generator already in open state");
        }

        if (methods.size() == 0)
        {
            throw new IllegalStateException("no encryption methods specified");
        }

        byte[] key = null;

        pOut = new BCPGOutputStream(out);

        defAlgorithm = dataEncryptorBuilder.getAlgorithm();
        rand = dataEncryptorBuilder.getSecureRandom();

        if (methods.size() == 1)
        {    

            if (methods.get(0) instanceof PBEKeyEncryptionMethodGenerator)
            {
                PBEKeyEncryptionMethodGenerator m = (PBEKeyEncryptionMethodGenerator)methods.get(0);

                key = m.getKey(dataEncryptorBuilder.getAlgorithm());

                pOut.writePacket(((PGPKeyEncryptionMethodGenerator)methods.get(0)).generate(defAlgorithm, null));
            }
            else
            {
                key = PGPUtil.makeRandomKey(defAlgorithm, rand);
                byte[] sessionInfo = createSessionInfo(defAlgorithm, key);
                PGPKeyEncryptionMethodGenerator m = (PGPKeyEncryptionMethodGenerator)methods.get(0);

                pOut.writePacket(m.generate(defAlgorithm, sessionInfo));
            }
        }
        else // multiple methods
        {
            key = PGPUtil.makeRandomKey(defAlgorithm, rand);
            byte[] sessionInfo = createSessionInfo(defAlgorithm, key);

            for (int i = 0; i != methods.size(); i++)
            {
                PGPKeyEncryptionMethodGenerator m = (PGPKeyEncryptionMethodGenerator)methods.get(i);

                pOut.writePacket(m.generate(defAlgorithm, sessionInfo));
            }
        }

        try
        {
            PGPDataEncryptor dataEncryptor = dataEncryptorBuilder.build(key);

            digestCalc = dataEncryptor.getIntegrityCalculator();
            
            if (buffer == null)
            {
                //
                // we have to add block size + 2 for the generated IV and + 1 + 22 if integrity protected
                //
                if (digestCalc != null)
                {
                    pOut = new ClosableBCPGOutputStream(out, PacketTags.SYM_ENC_INTEGRITY_PRO, length + dataEncryptor.getBlockSize() + 2 + 1 + 22);

                    pOut.write(1);        // version number
                }
                else
                {
                    pOut = new ClosableBCPGOutputStream(out, PacketTags.SYMMETRIC_KEY_ENC, length + dataEncryptor.getBlockSize() + 2, oldFormat);
                }
            }
            else
            {
                if (digestCalc != null)
                {
                    pOut = new ClosableBCPGOutputStream(out, PacketTags.SYM_ENC_INTEGRITY_PRO, buffer);
                    pOut.write(1);        // version number
                }
                else
                {
                    pOut = new ClosableBCPGOutputStream(out, PacketTags.SYMMETRIC_KEY_ENC, buffer);
                }
            }

            genOut = cOut = dataEncryptor.getOutputStream(pOut);

            if (digestCalc != null)
            {
                genOut = new TeeOutputStream(digestCalc.getOutputStream(), cOut);
            }

            byte[] inLineIv = new byte[dataEncryptor.getBlockSize() + 2];
            rand.nextBytes(inLineIv);
            inLineIv[inLineIv.length - 1] = inLineIv[inLineIv.length - 3];
            inLineIv[inLineIv.length - 2] = inLineIv[inLineIv.length - 4];

            genOut.write(inLineIv);

            return new WrappedGeneratorStream(genOut, this);
        }
        catch (Exception e)
        {
            throw new PGPException("Exception creating cipher", e);
        }
    }

    /**
     * Return an outputstream which will encrypt the data as it is written
     * to it.
     * <p>
     * The stream created can be closed off by either calling close()
     * on the stream or close() on the generator. Closing the returned
     * stream does not close off the OutputStream parameter out.
     * 
     * @param out
     * @param length
     * @return OutputStream
     * @throws IOException
     * @throws PGPException
     */
    public OutputStream open(
        OutputStream    out,
        long            length)
        throws IOException, PGPException
    {
        return this.open(out, length, null);
    }
    
    /**
     * Return an outputstream which will encrypt the data as it is written
     * to it. The stream will be written out in chunks according to the size of the
     * passed in buffer.
     * <p>
     * The stream created can be closed off by either calling close()
     * on the stream or close() on the generator. Closing the returned
     * stream does not close off the OutputStream parameter out.
     * <p>
     * <b>Note</b>: if the buffer is not a power of 2 in length only the largest power of 2
     * bytes worth of the buffer will be used.
     * 
     * @param out
     * @param buffer the buffer to use.
     * @return OutputStream
     * @throws IOException
     * @throws PGPException
     */
    public OutputStream open(
        OutputStream    out,
        byte[]          buffer)
        throws IOException, PGPException
    {
        return this.open(out, 0, buffer);
    }
    
    /**
     * Close off the encrypted object - this is equivalent to calling close on the stream
     * returned by the open() method.
     * <p>
     * <b>Note</b>: This does not close the underlying output stream, only the stream on top of it created by the open() method.
     * @throws java.io.IOException
     */
    public void close()
        throws IOException
    {
        if (cOut != null)
        {    
            if (digestCalc != null)
            {
                //
                // hand code a mod detection packet
                //
                BCPGOutputStream bOut = new BCPGOutputStream(genOut, PacketTags.MOD_DETECTION_CODE, 20);

                bOut.flush();

                byte[] dig = digestCalc.getDigest();

                cOut.write(dig);
            }

            cOut.close();

            cOut = null;
            pOut = null;
        }
    }

    private class ClosableBCPGOutputStream
        extends BCPGOutputStream
    {
        public ClosableBCPGOutputStream(OutputStream out, int symmetricKeyEnc, byte[] buffer)
            throws IOException
        {
            super(out, symmetricKeyEnc, buffer);
        }

        public ClosableBCPGOutputStream(OutputStream out, int symmetricKeyEnc, long length, boolean oldFormat)
            throws IOException
        {
            super(out, symmetricKeyEnc, length, oldFormat);
        }

        public ClosableBCPGOutputStream(OutputStream out, int symEncIntegrityPro, long length)
            throws IOException
        {
            super(out, symEncIntegrityPro, length);
        }

        public void close()
            throws IOException
        {
             this.finish();
        }
    }
}
