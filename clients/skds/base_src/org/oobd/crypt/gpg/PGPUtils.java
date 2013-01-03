package org.oobd.crypt.gpg;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedInputStream;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Iterator;


import org.spongycastle.bcpg.PublicKeyAlgorithmTags;
import org.spongycastle.bcpg.sig.KeyFlags;
import org.spongycastle.openpgp.PGPCompressedData;
import org.spongycastle.openpgp.PGPEncryptedDataList;
import org.spongycastle.openpgp.PGPException;
import org.spongycastle.openpgp.PGPLiteralData;
import org.spongycastle.openpgp.PGPObjectFactory;
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
import org.spongycastle.openpgp.PGPSignatureSubpacketVector;
import org.spongycastle.openpgp.PGPUtil;

import org.spongycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.spongycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.spongycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.spongycastle.openpgp.operator.bc.BcPublicKeyDataDecryptorFactory;


















public class PGPUtils {

    private static final int   BUFFER_SIZE = 1 << 16; // should always be power of 2
    private static final int   KEY_FLAGS = 27;
    private static final int[] MASTER_KEY_CERTIFICATION_TYPES = new int[]{
    	PGPSignature.POSITIVE_CERTIFICATION,
    	PGPSignature.CASUAL_CERTIFICATION,
    	PGPSignature.NO_CERTIFICATION,
    	PGPSignature.DEFAULT_CERTIFICATION
    };

public static void init()
{
	Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
}



//Read more: http://www.aviransplace.com/2004/10/12/using-rsa-encryption-with-java/#ixzz2GYW2AadB

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

	public static void decryptFile(InputStream in, OutputStream out, InputStream keyIn, char[] passwd)
    	throws Exception
    {

            InputStream unc = decryptFileStream( in,   keyIn,  passwd);
            int ch;

            while ((ch = unc.read()) >= 0) {
                out.write(ch);
            }
     }

//-------------  NEW  from http://blog.mrjaredpowell.com/2010/Automate_decryption_Bouncy_Castle.htm

    /**
     * returns an encrypted input stream
     */
    @SuppressWarnings("unchecked")

  public static InputStream decryptFileStream(InputStream in, InputStream keyIn, char[] passwd) throws Exception {
  	InputStream unc =null;    	
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
 		unc = ld.getInputStream();
           } else if (message instanceof  PGPOnePassSignatureList) {
               throw new PGPException("encrypted message contains a signed message - not literal data.");
           } else {
               throw new PGPException("message is not a simple encrypted file - type unknown.");
           }
 /* -------------  the commended routine below always caused an EOFreached error
           if (pbe.isIntegrityProtected()) {
               if (!pbe.verify()) {
                   System.err.println("message failed integrity check");
               } else {
                   System.err.println("message integrity check passed");
               }
           } else {
               System.err.println("no message integrity check");
           }
*/
       } catch (PGPException e) {
           System.err.println(e);
           if (e.getUnderlyingException() != null) {
               e.getUnderlyingException().printStackTrace();
           }
       }
	return unc;
   }







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



/*public class EncryptFiles {
   
   //*
    //* Encrypts a file for transfer over standard FTP or via email
    //* @param out Output Stream containing the file
    //* @param fileName String representation of the file we want to create
    //* @param encKey PGPPublicKey to encrypt the file.
    //* @param armor boolean value decides whether we need a new instance of an ArmoredOutputStream
    //* @param withIntegrityCheck boolean value for setting the Integrity Packet
    //* @throws IOException
    //* @throws NoSuchProviderException
    //
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
*/
}
