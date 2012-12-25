// taken from http://sloanseaman.com/wordpress/2012/05/13/revisited-pgp-encryptiondecryption-in-java/
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.spongycastle.bcpg.ArmoredOutputStream;
import org.spongycastle.bcpg.HashAlgorithmTags;
import org.spongycastle.bcpg.PublicKeyAlgorithmTags;
import org.spongycastle.bcpg.sig.KeyFlags;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.openpgp.PGPCompressedData;
import org.spongycastle.openpgp.PGPCompressedDataGenerator;
import org.spongycastle.openpgp.PGPEncryptedData;
import org.spongycastle.openpgp.PGPEncryptedDataGenerator;
import org.spongycastle.openpgp.PGPEncryptedDataList;
import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPLiteralData;
import org.spongycastle.openpgp.PGPLiteralDataGenerator;
import org.spongycastle.openpgp.PGPObjectFactory;
import org.spongycastle.openpgp.PGPOnePassSignature;
import org.spongycastle.openpgp.PGPOnePassSignatureList;
import org.spongycastle.openpgp.PGPPrivateKey;
import org.spongycastle.openpgp.PGPPublicKey;
import org.spongycastle.openpgp.PGPPublicKeyEncryptedData;
import org.spongycastle.openpgp.PGPPublicKeyRing;
import org.spongycastle.openpgp.PGPPublicKeyRingCollection;
import org.spongycastle.openpgp.PGPSecretKey;
import org.spongycastle.openpgp.PGPSecretKeyRing;
import org.spongycastle.openpgp.PGPSecretKeyRingCollection;
import org.spongycastle.openpgp.PGPSignature;
import org.spongycastle.openpgp.PGPSignatureGenerator;
import org.spongycastle.openpgp.PGPSignatureList;
import org.spongycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.spongycastle.openpgp.PGPSignatureSubpacketVector;
import org.spongycastle.openpgp.PGPUtil;

import org.spongycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.spongycastle.openpgp.operator.PGPContentSignerBuilder;
import org.spongycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.spongycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.spongycastle.openpgp.operator.bc.BcPGPContentVerifierBuilderProvider;
import org.spongycastle.openpgp.operator.bc.BcPGPDataEncryptorBuilder;
import org.spongycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.spongycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;
import org.spongycastle.openpgp.operator.bc.BcPublicKeyKeyEncryptionMethodGenerator;


















public class PGPUtils {

    private static final int   BUFFER_SIZE = 1 << 16; // should always be power of 2
    private static final int   KEY_FLAGS = 27;
    private static final int[] MASTER_KEY_CERTIFICATION_TYPES = new int[]{
    	PGPSignature.POSITIVE_CERTIFICATION,
    	PGPSignature.CASUAL_CERTIFICATION,
    	PGPSignature.NO_CERTIFICATION,
    	PGPSignature.DEFAULT_CERTIFICATION
    };

    @SuppressWarnings("unchecked")
    public static PGPPublicKey readPublicKey(InputStream in)
    	throws IOException, PGPException
    {

        PGPPublicKeyRingCollection keyRingCollection = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(in));

        //
        // we just loop through the collection till we find a key suitable for encryption, in the real
        // world you would probably want to be a bit smarter about this.
        //
        PGPPublicKey publicKey = null;

        //
        // iterate through the key rings.
        //
        Iterator<PGPPublicKeyRing> rIt = keyRingCollection.getKeyRings();

        while (publicKey == null && rIt.hasNext()) {
            PGPPublicKeyRing kRing = rIt.next();
            Iterator<PGPPublicKey> kIt = kRing.getPublicKeys();
            while (publicKey == null && kIt.hasNext()) {
                PGPPublicKey key = kIt.next();
                if (key.isEncryptionKey()) {
                    publicKey = key;
                }
            }
        }

        if (publicKey == null) {
            throw new IllegalArgumentException("Can't find public key in the key ring.");
        }
        if (!isForEncryption(publicKey)) {
            throw new IllegalArgumentException("KeyID " + publicKey.getKeyID() + " not flagged for encryption.");
        }

        return publicKey;
    }

    @SuppressWarnings("unchecked")
	public static PGPSecretKey readSecretKey(InputStream in)
		throws IOException, PGPException
	{

        PGPSecretKeyRingCollection keyRingCollection = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(in));

        //
        // We just loop through the collection till we find a key suitable for signing.
        // In the real world you would probably want to be a bit smarter about this.
        //
        PGPSecretKey secretKey = null;

        Iterator<PGPSecretKeyRing> rIt = keyRingCollection.getKeyRings();
        while (secretKey == null && rIt.hasNext()) {
            PGPSecretKeyRing keyRing = rIt.next();
            Iterator<PGPSecretKey> kIt = keyRing.getSecretKeys();
            while (secretKey == null && kIt.hasNext()) {
                PGPSecretKey key = kIt.next();
                if (key.isSigningKey()) {
                    secretKey = key;
                }
            }
        }

        // Validate secret key
        if (secretKey == null) {
            throw new IllegalArgumentException("Can't find private key in the key ring.");
        }
        if (!secretKey.isSigningKey()) {
            throw new IllegalArgumentException("Private key does not allow signing.");
        }
        if (secretKey.getPublicKey().isRevoked()) {
            throw new IllegalArgumentException("Private key has been revoked.");
        }
        if (!hasKeyFlags(secretKey.getPublicKey(), KeyFlags.SIGN_DATA)) {
            throw new IllegalArgumentException("Key cannot be used for signing.");
        }

        return secretKey;
    }

    /**
     * Load a secret key ring collection from keyIn and find the private key corresponding to
     * keyID if it exists.
     *
     * @param keyIn input stream representing a key ring collection.
     * @param keyID keyID we want.
     * @param pass passphrase to decrypt secret key with.
     * @return
     * @throws IOException
     * @throws PGPException
     * @throws NoSuchProviderException
     */
    public  static PGPPrivateKey findPrivateKey(InputStream keyIn, long keyID, char[] pass)
    	throws IOException, PGPException, NoSuchProviderException
    {
        PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(keyIn));
        return findPrivateKey(pgpSec.getSecretKey(keyID), pass);

    }

    /**
     * Load a secret key and find the private key in it
     * @param pgpSecKey The secret key
     * @param pass passphrase to decrypt secret key with
     * @return
     * @throws PGPException
     */
    public static PGPPrivateKey findPrivateKey(PGPSecretKey pgpSecKey, char[] pass)
    	throws PGPException, java.security.NoSuchProviderException
    {
    	if (pgpSecKey == null) return null;

        PBESecretKeyDecryptor decryptor = new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider()).build(pass);
        return pgpSecKey.extractPrivateKey(decryptor);
//	return pgpSecKey.extractPrivateKey(pass, BouncyCastleProvider.PROVIDER_NAME);
    }

    /**
     * decrypt the passed in message stream
     */
 
   @SuppressWarnings("unchecked")
/*
	public static void decryptFile(InputStream in, OutputStream out, InputStream keyIn, char[] passwd)
    	throws Exception
    {
    	Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());

        in = org.spongycastle.openpgp.PGPUtil.getDecoderStream(in);

        PGPObjectFactory pgpF = new PGPObjectFactory(in);
        PGPEncryptedDataList enc;

        Object o = pgpF.nextObject();
        //
        // the first object might be a PGP marker packet.
        //
        if (o instanceof  PGPEncryptedDataList) {
            enc = (PGPEncryptedDataList) o;
        } else {
            enc = (PGPEncryptedDataList) pgpF.nextObject();
        }

        //
        // find the secret key
        //
        Iterator<PGPPublicKeyEncryptedData> it = enc.getEncryptedDataObjects();
        PGPPrivateKey sKey = null;
        PGPPublicKeyEncryptedData pbe = null;

        while (sKey == null && it.hasNext()) {
            pbe = it.next();

            sKey = findPrivateKey(keyIn, pbe.getKeyID(), passwd);
        }

        if (sKey == null) {
            throw new IllegalArgumentException("Secret key for message not found.");
        }

        InputStream clear = pbe.getDataStream(new BcPublicKeyDataDecryptorFactory(sKey));
//        InputStream clear = pbe.getDataStream(sKey, BouncyCastleProvider.PROVIDER_NAME);


        PGPObjectFactory plainFact = new PGPObjectFactory(clear);

        Object message = plainFact.nextObject();

        if (message instanceof  PGPCompressedData) {
            PGPCompressedData cData = (PGPCompressedData) message;
            PGPObjectFactory pgpFact = new PGPObjectFactory(cData.getDataStream());

            message = pgpFact.nextObject();
        }

        if (message instanceof  PGPLiteralData) {
            PGPLiteralData ld = (PGPLiteralData) message;

            InputStream unc = ld.getInputStream();
            int ch;

            while ((ch = unc.read()) >= 0) {
                out.write(ch);
            }
        } else if (message instanceof  PGPOnePassSignatureList) {
            throw new PGPException("Encrypted message contains a signed message - not literal data.");
        } else {
            throw new PGPException("Message is not a simple encrypted file - type unknown.");
        }

        if (pbe.isIntegrityProtected()) {
            if (!pbe.verify()) {
            	throw new PGPException("Message failed integrity check");
            }
        }
    }
*/
	public static void decryptFile(InputStream in, OutputStream out, InputStream keyIn, char[] passwd)
    	throws Exception
    {

            InputStream unc = decryptFileStream( in,   keyIn,  passwd);
            int ch;

            while ((ch = unc.read()) >= 0) {
                out.write(ch);
            }
     }


    /**
     * returns an encrypted input stream
     */
    @SuppressWarnings("unchecked")
	public static InputStream decryptFileStream(InputStream in,  InputStream keyIn, char[] passwd)
    	throws Exception
    {
	InputStream unc =null;    	
	Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());

        in = org.spongycastle.openpgp.PGPUtil.getDecoderStream(in);

        PGPObjectFactory pgpF = new PGPObjectFactory(in);
        PGPEncryptedDataList enc;

        Object o = pgpF.nextObject();
        //
        // the first object might be a PGP marker packet.
        //
        if (o instanceof  PGPEncryptedDataList) {
            enc = (PGPEncryptedDataList) o;
        } else {
            enc = (PGPEncryptedDataList) pgpF.nextObject();
        }

        //
        // find the secret key
        //
        Iterator<PGPPublicKeyEncryptedData> it = enc.getEncryptedDataObjects();
        PGPPrivateKey sKey = null;
        PGPPublicKeyEncryptedData pbe = null;

        while (sKey == null && it.hasNext()) {
            pbe = it.next();

            sKey = findPrivateKey(keyIn, pbe.getKeyID(), passwd);
        }

        if (sKey == null) {
            throw new IllegalArgumentException("Secret key for message not found.");
        }

        InputStream clear = pbe.getDataStream(new BcPublicKeyDataDecryptorFactory(sKey));
//        InputStream clear = pbe.getDataStream(sKey, BouncyCastleProvider.PROVIDER_NAME);


        PGPObjectFactory plainFact = new PGPObjectFactory(clear);

        Object message = plainFact.nextObject();

        if (message instanceof  PGPCompressedData) {
            PGPCompressedData cData = (PGPCompressedData) message;
            PGPObjectFactory pgpFact = new PGPObjectFactory(cData.getDataStream());

            message = pgpFact.nextObject();
        }

        if (message instanceof  PGPLiteralData) {
            PGPLiteralData ld = (PGPLiteralData) message;

            unc= ld.getInputStream();
            /*
           InputStream unc = ld.getInputStream();
		int ch;

            while ((ch = unc.read()) >= 0) {
                out.write(ch);
            }
	*/
        } else if (message instanceof  PGPOnePassSignatureList) {
            throw new PGPException("Encrypted message contains a signed message - not literal data.");
        } else {
            throw new PGPException("Message is not a simple encrypted file - type unknown.");
        }

        if (pbe.isIntegrityProtected()) {
            if (!pbe.verify()) {
            	throw new PGPException("Message failed integrity check");
            }
        }
	return unc;
    }





//-------------  NEW  from http://blog.mrjaredpowell.com/2010/Automate_decryption_Bouncy_Castle.htm

  public static void decryptFile(InputStream in, InputStream keyIn, char[] passwd) throws Exception {
       in = PGPUtil.getDecoderStream(in);
       PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(keyIn));
       try {
           PGPObjectFactory pgpF = new PGPObjectFactory(in);
           PGPEncryptedDataList enc;
           Object o = pgpF.nextObject();
           if (o instanceof  PGPEncryptedDataList) {
               enc = (PGPEncryptedDataList) o;
           } else {
               enc = (PGPEncryptedDataList) pgpF.nextObject();
           }
           System.out.println(enc.size() + " enc size.");
           //
           @SuppressWarnings("rawtypes")
           Iterator it = enc.getEncryptedDataObjects();
           PGPPrivateKey sKey = null;
           PGPPublicKeyEncryptedData pbe = null;
   
           while (sKey == null && it.hasNext()) {
               pbe = (PGPPublicKeyEncryptedData) it.next();
               sKey = KeyReader.findSecretKey(pgpSec, pbe.getKeyID(), passwd);
           }
   
           if (sKey == null) {
               throw new IllegalArgumentException("Failed to find private key with ID " + pbe.getKeyID());
           }


       		//---- added by OOBD
		InputStream clear = pbe.getDataStream(new BcPublicKeyDataDecryptorFactory(sKey));
		//---- instead of
              	//InputStream clear = pbe.getDataStream(sKey, "SC");
   
           PGPObjectFactory plainFact = new PGPObjectFactory(clear);
   
           PGPCompressedData cData = (PGPCompressedData) plainFact.nextObject();
   
           InputStream compressedStream = new BufferedInputStream(cData.getDataStream());
           PGPObjectFactory pgpFact = new PGPObjectFactory(compressedStream);
   
           Object message = pgpFact.nextObject();
   
           if (message instanceof  PGPLiteralData) {
               PGPLiteralData ld = (PGPLiteralData) message;
   
               FileOutputStream fOut = new FileOutputStream("/media/ram/" + ld.getFileName());
               BufferedOutputStream bOut = new BufferedOutputStream(fOut);
   
               InputStream unc = ld.getInputStream();
               int ch;
   
               while ((ch = unc.read()) >= 0) {
                   bOut.write(ch);
               }
   
               bOut.close();
           } else if (message instanceof  PGPOnePassSignatureList) {
               throw new PGPException("encrypted message contains a signed message - not literal data.");
           } else {
               throw new PGPException("message is not a simple encrypted file - type unknown.");
           }
   
           if (pbe.isIntegrityProtected()) {
               if (!pbe.verify()) {
                   System.err.println("message failed integrity check");
               } else {
                   System.err.println("message integrity check passed");
               }
           } else {
               System.err.println("no message integrity check");
           }
       } catch (PGPException e) {
           System.err.println(e);
           if (e.getUnderlyingException() != null) {
               e.getUnderlyingException().printStackTrace();
           }
       }
   }

public class EncryptFiles {
   
   /**
    * Encrypts a file for transfer over standard FTP or via email
    * @param out Output Stream containing the file
    * @param fileName String representation of the file we want to create
    * @param encKey PGPPublicKey to encrypt the file.
    * @param armor boolean value decides whether we need a new instance of an ArmoredOutputStream
    * @param withIntegrityCheck boolean value for setting the Integrity Packet
    * @throws IOException
    * @throws NoSuchProviderException
    */
   public static void encryptFile(OutputStream out, String fileName, PGPPublicKey encKey, boolean armor, boolean withIntegrityCheck) 
											throws IOException, NoSuchProviderException {
       if (armor) {
           out = new ArmoredOutputStream(out);
       }
       try {
           PGPEncryptedDataGenerator cPk = new PGPEncryptedDataGenerator(PGPEncryptedData.CAST5, 
									       withIntegrityCheck,new SecureRandom(), "BC");
           cPk.addMethod(encKey);
           OutputStream cOut = cPk.open(out, new byte[1 << 16]);
           PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);
           PGPUtil.writeFileToLiteralData(comData.open(cOut), PGPLiteralData.BINARY, new File(fileName), new byte[1 << 16]);
           comData.close();
           cOut.close();
           out.close();
       } catch (PGPException e) {
           System.err.println(e);
           if (e.getUnderlyingException() != null) {
               e.getUnderlyingException().printStackTrace();
           }
       }
   }
   
}







/*
    public static void encryptFile(
        OutputStream out,
        String fileName,
        PGPPublicKey encKey,
        boolean armor,
        boolean withIntegrityCheck)
        throws IOException, NoSuchProviderException, PGPException
    {
    	Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());

        if (armor) {
            out = new ArmoredOutputStream(out);
        }

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);

        PGPUtil.writeFileToLiteralData(
                comData.open(bOut),
                PGPLiteralData.BINARY,
                new File(fileName) );

        comData.close();

        BcPGPDataEncryptorBuilder dataEncryptor = new BcPGPDataEncryptorBuilder(PGPEncryptedData.TRIPLE_DES);
        dataEncryptor.setWithIntegrityPacket(withIntegrityCheck);
        dataEncryptor.setSecureRandom(new SecureRandom());

        PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(dataEncryptor);
        encryptedDataGenerator.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(encKey));

        byte[] bytes = bOut.toByteArray();
        OutputStream cOut = encryptedDataGenerator.open(out, bytes.length);
        cOut.write(bytes);
        cOut.close();
        out.close();
    }

    @SuppressWarnings("unchecked")
	public static void signEncryptFile(
        OutputStream out,
        String fileName,
        PGPPublicKey publicKey,
        PGPSecretKey secretKey,
        String password,
        boolean armor,
        boolean withIntegrityCheck )
        throws Exception
    {

        // Initialize spongy Castle security provider
        Provider provider = new spongyCastleProvider();
        Security.addProvider(provider);

        if (armor) {
            out = new ArmoredOutputStream(out);
        }

        BcPGPDataEncryptorBuilder dataEncryptor = new BcPGPDataEncryptorBuilder(PGPEncryptedData.TRIPLE_DES);
        dataEncryptor.setWithIntegrityPacket(withIntegrityCheck);
        dataEncryptor.setSecureRandom(new SecureRandom());

        PGPEncryptedDataGenerator encryptedDataGenerator = new PGPEncryptedDataGenerator(dataEncryptor);
        encryptedDataGenerator.addMethod(new BcPublicKeyKeyEncryptionMethodGenerator(publicKey));

        OutputStream encryptedOut = encryptedDataGenerator.open(out, new byte[PGPUtils.BUFFER_SIZE]);

        // Initialize compressed data generator
        PGPCompressedDataGenerator compressedDataGenerator = new PGPCompressedDataGenerator(PGPCompressedData.ZIP);
        OutputStream compressedOut = compressedDataGenerator.open(encryptedOut, new byte [PGPUtils.BUFFER_SIZE]);

        // Initialize signature generator
        PGPPrivateKey privateKey = findPrivateKey(secretKey, password.toCharArray());

        PGPContentSignerBuilder signerBuilder = new BcPGPContentSignerBuilder(secretKey.getPublicKey().getAlgorithm(),
                HashAlgorithmTags.SHA1);

        PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(signerBuilder);
        signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, privateKey);

        boolean firstTime = true;
        Iterator<String> it = secretKey.getPublicKey().getUserIDs();
        while (it.hasNext() && firstTime) {
            PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
            spGen.setSignerUserID(false, it.next());
            signatureGenerator.setHashedSubpackets(spGen.generate());
            // Exit the loop after the first iteration
            firstTime = false;
        }
        signatureGenerator.generateOnePassVersion(false).encode(compressedOut);

        // Initialize literal data generator
        PGPLiteralDataGenerator literalDataGenerator = new PGPLiteralDataGenerator();
        OutputStream literalOut = literalDataGenerator.open(
            compressedOut,
            PGPLiteralData.BINARY,
            fileName,
            new Date(),
            new byte [PGPUtils.BUFFER_SIZE] );

        // Main loop - read the "in" stream, compress, encrypt and write to the "out" stream
        FileInputStream in = new FileInputStream(fileName);
        byte[] buf = new byte[PGPUtils.BUFFER_SIZE];
        int len;
        while ((len = in.read(buf)) > 0) {
            literalOut.write(buf, 0, len);
            signatureGenerator.update(buf, 0, len);
        }

        in.close();
        literalDataGenerator.close();
        // Generate the signature, compress, encrypt and write to the "out" stream
        signatureGenerator.generate().encode(compressedOut);
        compressedDataGenerator.close();
        encryptedDataGenerator.close();
        if (armor) {
            out.close();
        }
    }
    public static boolean verifyFile(
    	InputStream in,
        InputStream keyIn,
        String extractContentFile)
        throws Exception
    {
        in = PGPUtil.getDecoderStream(in);

        PGPObjectFactory pgpFact = new PGPObjectFactory(in);
        PGPCompressedData c1 = (PGPCompressedData)pgpFact.nextObject();

        pgpFact = new PGPObjectFactory(c1.getDataStream());

        PGPOnePassSignatureList p1 = (PGPOnePassSignatureList)pgpFact.nextObject();

        PGPOnePassSignature ops = p1.get(0);

        PGPLiteralData p2 = (PGPLiteralData)pgpFact.nextObject();

        InputStream dIn = p2.getInputStream();

        IOUtils.copy(dIn, new FileOutputStream(extractContentFile));

        int ch;
        PGPPublicKeyRingCollection pgpRing = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(keyIn));

        PGPPublicKey key = pgpRing.getPublicKey(ops.getKeyID());

        FileOutputStream out = new FileOutputStream(p2.getFileName());

        ops.init(new BcPGPContentVerifierBuilderProvider(), key);

        while ((ch = dIn.read()) >= 0)
        {
            ops.update((byte)ch);
            out.write(ch);
        }

        out.close();

        PGPSignatureList p3 = (PGPSignatureList)pgpFact.nextObject();
        return ops.verify(p3.get(0));
    }
*/

    /**
     * From LockBox Lobs PGP Encryption tools.
     * http://www.lockboxlabs.org/content/downloads
     *
     * I didn't think it was worth having to import a 4meg lib for three methods
     * @param key
     * @return
     */
    public static boolean isForEncryption(PGPPublicKey key)
    {
        if (key.getAlgorithm() == PublicKeyAlgorithmTags.RSA_SIGN
            || key.getAlgorithm() == PublicKeyAlgorithmTags.DSA
            || key.getAlgorithm() == PublicKeyAlgorithmTags.EC
            || key.getAlgorithm() == PublicKeyAlgorithmTags.ECDSA)
        {
            return false;
        }

        return hasKeyFlags(key, KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE);
    }

    /**
     * From LockBox Lobs PGP Encryption tools.
     * http://www.lockboxlabs.org/content/downloads
     *
     * I didn't think it was worth having to import a 4meg lib for three methods
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
	private static boolean hasKeyFlags(PGPPublicKey encKey, int keyUsage) {
        if (encKey.isMasterKey()) {
            for (int i = 0; i != PGPUtils.MASTER_KEY_CERTIFICATION_TYPES.length; i++) {
                for (Iterator<PGPSignature> eIt = encKey.getSignaturesOfType(PGPUtils.MASTER_KEY_CERTIFICATION_TYPES[i]); eIt.hasNext();) {
                    PGPSignature sig = eIt.next();
                    if (!isMatchingUsage(sig, keyUsage)) {
                        return false;
                    }
                }
            }
        }
        else {
            for (Iterator<PGPSignature> eIt = encKey.getSignaturesOfType(PGPSignature.SUBKEY_BINDING); eIt.hasNext();) {
                PGPSignature sig = eIt.next();
                if (!isMatchingUsage(sig, keyUsage)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * From LockBox Lobs PGP Encryption tools.
     * http://www.lockboxlabs.org/content/downloads
     *
     * I didn't think it was worth having to import a 4meg lib for three methods
     * @param key
     * @return
     */
    private static boolean isMatchingUsage(PGPSignature sig, int keyUsage) {
        if (sig.hasSubpackets()) {
            PGPSignatureSubpacketVector sv = sig.getHashedSubPackets();
            if (sv.hasSubpacket(PGPUtils.KEY_FLAGS)) {
                if ((sv.getKeyFlags() & keyUsage) == 0) {
                    return false;
                }
            }
        }
        return true;
    }

}

