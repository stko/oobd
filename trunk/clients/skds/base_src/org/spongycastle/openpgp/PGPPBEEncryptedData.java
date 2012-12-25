package org.spongycastle.openpgp;

import java.io.EOFException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.security.Provider;

import org.spongycastle.bcpg.BCPGInputStream;
import org.spongycastle.bcpg.InputStreamPacket;
import org.spongycastle.bcpg.SymmetricEncIntegrityPacket;
import org.spongycastle.bcpg.SymmetricKeyEncSessionPacket;
import org.spongycastle.openpgp.operator.PBEDataDecryptorFactory;
import org.spongycastle.openpgp.operator.PGPDataDecryptor;
import org.spongycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder;
import org.spongycastle.openpgp.operator.jcajce.JcePBEDataDecryptorFactoryBuilder;
import org.spongycastle.util.io.TeeInputStream;

/**
 * A password based encryption object.
 */
public class PGPPBEEncryptedData
    extends PGPEncryptedData
{
    SymmetricKeyEncSessionPacket    keyData;
    
    PGPPBEEncryptedData(
        SymmetricKeyEncSessionPacket    keyData,
        InputStreamPacket               encData)
    {
        super(encData);
        
        this.keyData = keyData;
    }
    
    /**
     * Return the raw input stream for the data stream.
     * 
     * @return InputStream
     */
    public InputStream getInputStream()
    {
        return encData.getInputStream();
    }

    /**
     * Return the decrypted input stream, using the passed in passPhrase.
     *
     * @param passPhrase
     * @param provider
     * @return InputStream
     * @throws PGPException
     * @throws NoSuchProviderException
     *  @deprecated use PBEDataDecryptorFactory method
     */
    public InputStream getDataStream(
        char[]                passPhrase,
        String                provider)
        throws PGPException, NoSuchProviderException
    {
        return getDataStream(passPhrase, PGPUtil.getProvider(provider));
    }

    /**
     * Return the decrypted input stream, using the passed in passPhrase.
     * 
     * @param passPhrase
     * @param provider
     * @return InputStream
     * @throws PGPException
     * @deprecated use PBEDataDecryptorFactory method
     */
    public InputStream getDataStream(
        char[]                passPhrase,
        Provider              provider)
        throws PGPException
    {
        return getDataStream(new JcePBEDataDecryptorFactoryBuilder(new JcaPGPDigestCalculatorProviderBuilder().setProvider(provider).build()).setProvider(provider).build(passPhrase));
    }

   /**
     * Return the symmetric key algorithm required to decrypt the data protected by this object.
     *
     * @param dataDecryptorFactory   decryptor factory to use to recover the session data.
     * @return  the integer encryption algorithm code.
     * @throws PGPException if the session data cannot be recovered.
     */
    public int getSymmetricAlgorithm(
        PBEDataDecryptorFactory dataDecryptorFactory)
        throws PGPException
    {
        byte[]       key = dataDecryptorFactory.makeKeyFromPassPhrase(keyData.getEncAlgorithm(), keyData.getS2K());
        byte[]       sessionData = dataDecryptorFactory.recoverSessionData(keyData.getEncAlgorithm(), key, keyData.getSecKeyData());

        return sessionData[0];
    }

   /**
     * Open an input stream which will provide the decrypted data protected by this object.
     *
     * @param dataDecryptorFactory  decryptor factory to use to recover the session data and provide the stream.
     * @return  the resulting input stream
     * @throws PGPException  if the session data cannot be recovered or the stream cannot be created.
     */
    public InputStream getDataStream(
        PBEDataDecryptorFactory dataDecryptorFactory)
        throws PGPException
    {
        try
        {
            int          keyAlgorithm = keyData.getEncAlgorithm();
            byte[]       key = dataDecryptorFactory.makeKeyFromPassPhrase(keyAlgorithm, keyData.getS2K());
            boolean      withIntegrityPacket = encData instanceof SymmetricEncIntegrityPacket;

            byte[]       sessionData = dataDecryptorFactory.recoverSessionData(keyData.getEncAlgorithm(), key, keyData.getSecKeyData());
            byte[]       sessionKey = new byte[sessionData.length - 1];

            System.arraycopy(sessionData, 1, sessionKey, 0, sessionKey.length);

            PGPDataDecryptor dataDecryptor = dataDecryptorFactory.createDataDecryptor(withIntegrityPacket, sessionData[0] & 0xff, sessionKey);

            encStream = new BCPGInputStream(dataDecryptor.getInputStream(encData.getInputStream()));

            if (withIntegrityPacket)
            {
                truncStream = new TruncatedStream(encStream);

                integrityCalculator = dataDecryptor.getIntegrityCalculator();

                encStream = new TeeInputStream(truncStream, integrityCalculator.getOutputStream());
            }

            byte[] iv = new byte[dataDecryptor.getBlockSize()];
            for (int i = 0; i != iv.length; i++)
            {
                int    ch = encStream.read();

                if (ch < 0)
                {
                    throw new EOFException("unexpected end of stream.");
                }

                iv[i] = (byte)ch;
            }

            int    v1 = encStream.read();
            int    v2 = encStream.read();

            if (v1 < 0 || v2 < 0)
            {
                throw new EOFException("unexpected end of stream.");
            }


            // Note: the oracle attack on "quick check" bytes is not deemed
            // a security risk for PBE (see PGPPublicKeyEncryptedData)

            boolean repeatCheckPassed = iv[iv.length - 2] == (byte) v1
                    && iv[iv.length - 1] == (byte) v2;

            // Note: some versions of PGP appear to produce 0 for the extra
            // bytes rather than repeating the two previous bytes
            boolean zeroesCheckPassed = v1 == 0 && v2 == 0;

            if (!repeatCheckPassed && !zeroesCheckPassed)
            {
                throw new PGPDataValidationException("data check failed.");
            }

            return encStream;
        }
        catch (PGPException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new PGPException("Exception creating cipher", e);
        }
    }
}
