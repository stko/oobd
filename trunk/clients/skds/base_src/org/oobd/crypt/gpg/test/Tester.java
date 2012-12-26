import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;

public class Tester {

/*
	private static final String PASSPHRASE_USER = "test";
	private static final String PASSPHRASE_GROUP = "abc";
	private static final String DE_INPUT = "gpg/x.pgp";
	private static final String DE_OUTPUT = "gpg/x.txt";
	private static final String DE_KEY_FILE = "gpg/secring.skr";
*/
	private static final String PASSPHRASE_USER = "test";
	private static final String PASSPHRASE_GROUP = "abc";
	private static final String DE_INPUT = "gpg/script.pgp";
	private static final String DE_GROUP_INPUT = "gpg/skoehle6_groups.pgp";
	private static final String DE_OUTPUT = "gpg/script.decoded";
	private static final String DE_KEY_FILE = "gpg/userkey.sec";


	public static void testDecrypt() throws Exception {

        FileInputStream in = new FileInputStream(DE_INPUT);
        FileInputStream keyIn = new FileInputStream(DE_KEY_FILE);
        FileOutputStream out = new FileOutputStream(DE_OUTPUT);
        InputStream groupKeyStream = PGPUtils.decryptFileStream( new FileInputStream(DE_GROUP_INPUT),   new FileInputStream(DE_KEY_FILE), PASSPHRASE_USER.toCharArray());
         //PGPUtils.decryptFile(in, out, keyIn, PASSPHRASE_USER.toCharArray());
           //PGPUtils.decryptFile(in,  keyIn, PASSPHRASE.toCharArray());
           InputStream unc = PGPUtils.decryptFileStream( new FileInputStream(DE_INPUT),   groupKeyStream, PASSPHRASE_GROUP.toCharArray());
           //InputStream unc = firstStream;
            int ch;

            try{
		while ((ch = unc.read()) >= 0) {
	                out.write(ch);
	            }
		}catch (Exception ex){
			System.out.println("Exception "+ex.getMessage());
			ex.printStackTrace();
		}

        in.close();
        out.close();
        keyIn.close();
	}

	public static void main(String[] args) {
		try{
			testDecrypt();
		}catch (Exception ex){
			System.out.println("Exception "+ex.getMessage());
			ex.printStackTrace();
		}
	}
}

