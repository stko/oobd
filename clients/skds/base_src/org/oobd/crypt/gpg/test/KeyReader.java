import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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



public class KeyReader {
   
   /**
    * Reads the keyring from an InputStream and grabs the correct public key
    * @param in InputStream containing the public keyring
    * @return public key for encryption and decryption
    * @throws IOException
    * @throws PGPException
    */
   @SuppressWarnings("rawtypes")
   public static PGPPublicKey readPublicKey(InputStream in) throws IOException, PGPException {
       in = PGPUtil.getDecoderStream(in);
       PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(in);
       PGPPublicKey key = null;
       Iterator rIt = pgpPub.getKeyRings();
       while (key == null && rIt.hasNext()) {
           PGPPublicKeyRing kRing = (PGPPublicKeyRing) rIt.next();
           Iterator kIt = kRing.getPublicKeys();
           while (key == null && kIt.hasNext()) {
               PGPPublicKey k = (PGPPublicKey) kIt.next();
               if (k.isEncryptionKey()) {
                   key = k;
               }
           }
       }
       if (key == null) {
           throw new IllegalArgumentException("Can't find encryption key in key ring.");
       }
       return key;
   }
   
   /**
    * Load a secret key from keyring collection and find the secret key corresponding to
    * keyID if it exists.
    *
    * @param pgpSec PGPSecretKeyRingCollection containing all private keys.
    * @param keyID id of the key we want.
    * @param pass passphrase to decrypt secret key with.
    * @return the private key used for encryption and decryption.
    * @throws IOException
    * @throws PGPException
    * @throws NoSuchProviderException
    */
   public static PGPPrivateKey findSecretKey(PGPSecretKeyRingCollection pgpSec, long keyID, char[] pass) 
							throws IOException, PGPException, NoSuchProviderException {
       PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);
       if (pgpSecKey == null) {
           return null;
       }

	//--- added by oobd---
        PBESecretKeyDecryptor decryptor = new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider()).build(pass);
        return pgpSecKey.extractPrivateKey(decryptor);


       	//---- instead of 
	//return pgpSecKey.extractPrivateKey(pass, "SC");
   }
   
}

