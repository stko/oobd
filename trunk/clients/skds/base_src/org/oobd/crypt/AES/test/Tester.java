import org.oobd.crypt.AES.EncodeDecodeAES;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Basic symmetric encryption example
 */
public class Tester {

	private static String seedWith16Chars = "This is my seed.";
	private static String textToEncrypt = "Schrabbeldabbel";

	public static void main(String[] args) throws Exception {
		try {
			String encrypted = EncodeDecodeAES.encrypt(seedWith16Chars, textToEncrypt);
			System.out.println("Encrypt: "+encrypted);
			String decrypted = EncodeDecodeAES.decrypt(seedWith16Chars, encrypted);
			System.out.println("Decrypt: "+ decrypted);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

           
         
