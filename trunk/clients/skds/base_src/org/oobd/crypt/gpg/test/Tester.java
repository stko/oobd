import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;

public class Tester {

	private static final String PASSPHRASE = "test";

	private static final String DE_INPUT = "gpg/x.pgp";
	private static final String DE_OUTPUT = "gpg/x.txt";
	private static final String DE_KEY_FILE = "gpg/secring.skr";

	private static final String E_INPUT = "src/test/x.txt";
	private static final String E_OUTPUT = "src/test/x.pgp";
	private static final String E_KEY_FILE = "src/test/pubring.pkr";

	public static void testDecrypt() throws Exception {
/*		PGPFileProcessor p = new PGPFileProcessor();
		p.setInputFileName(DE_INPUT);
		p.setOutputFileName(DE_OUTPUT);
		p.setPassphrase(PASSPHRASE);
		p.setSecretKeyFileName(DE_KEY_FILE);
*/
        FileInputStream in = new FileInputStream(DE_INPUT);
        FileInputStream keyIn = new FileInputStream(DE_KEY_FILE);
        //InputStream keyIn = PGPUtils.decryptFileStream( new FileInputStream("gpg/skoehle6_groups.pgp"),   new FileInputStream("gpg/userkey.sec"), PASSPHRASE.toCharArray());
	//InputStream keyIn = PGPUtils.decryptFileStream( new FileInputStream("gpg/Hotel_Sunnyville.pgp"),   new FileInputStream("gpg/userkey.sec"), PASSPHRASE.toCharArray());
        FileOutputStream out = new FileOutputStream(DE_OUTPUT);
         //PGPUtils.decryptFile(in, out, keyIn, PASSPHRASE.toCharArray());
           PGPUtils.decryptFile(in,  keyIn, PASSPHRASE.toCharArray());
           //InputStream unc = PGPUtils.decryptFileStream( new FileInputStream("gpg/script.pgp"),   keyIn, "abc".toCharArray());
/*           InputStream unc = keyIn;
            int ch;

            while ((ch = unc.read()) >= 0) {
                out.write(ch);
            }
*/
        in.close();
        out.close();
        keyIn.close();
//		System.out.println(p.decrypt());
	}
/*
	public static void testEncrypt() throws Exception {
		PGPFileProcessor p = new PGPFileProcessor();
		p.setInputFileName(E_INPUT);
		p.setOutputFileName(E_OUTPUT);
		p.setPassphrase(PASSPHRASE);
		p.setPublicKeyFileName(E_KEY_FILE);
		System.out.println(p.encrypt());
	}

*/

	public static void main(String[] args) {
		try{
			testDecrypt();
		}catch (Exception ex){
			System.out.println("Exception "+ex.getMessage());
			ex.printStackTrace();
		}
	}
}

