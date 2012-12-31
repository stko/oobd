package org.oobd.crypt.gpg;

import java.io.InputStream;


public class GroupDecoder {
  /**
    * decrypts as first step the group secret key ring, which is encrypted with the users key and phassphase
    * in the second step it returns the encrypted input stream, which is encrypted with the group key and phassphrase
    *
    * @param in the encrypted input stream
    * @param userkeyFile the input stream of the users key file
    * @param groupkeyFile the input stream of the group key file
    * @param groupPass the group key passphrase
    * @param userPass the user key passphrase
    * @return the decrypted input stream
    * @throws IOException
    * @throws PGPException
    * @throws NoSuchProviderException
    */

	public static InputStream decryptGroup(InputStream in , InputStream userkeyFile , InputStream groupkeyFile , char[] groupPass, char[] userPass) throws Exception {
		InputStream groupKeyStream = PGPUtils.decryptFileStream( groupkeyFile,   userkeyFile, userPass);
		InputStream unc = PGPUtils.decryptFileStream( in,   groupKeyStream, groupPass);
		userkeyFile.close();
		groupkeyFile.close();
		return unc;
	}

}

