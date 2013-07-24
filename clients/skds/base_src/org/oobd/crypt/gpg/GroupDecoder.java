package org.oobd.crypt.gpg;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;

import org.oobd.base.Core;
import org.oobd.crypt.AES.EncodeDecodeAES;
import org.spongycastle.openpgp.PGPException;

public class GroupDecoder {
	/**
	 * decrypts as first step the group secret key ring, which is encrypted with
	 * the users key and phassphase in the second step it returns the encrypted
	 * input stream, which is encrypted with the group key and phassphrase
	 * 
	 * @param in
	 *            the encrypted input stream
	 * @param userkeyFile
	 *            the input stream of the users key file
	 * @param groupkeyFile
	 *            the input stream of the group key file
	 * @param groupPass
	 *            the group key passphrase
	 * @param userPass
	 *            the user key passphrase
	 * @return the decrypted input stream
	 * @throws IOException
	 * @throws PGPException
	 * @throws NoSuchProviderException
	 */

	public static InputStream decryptGroup(InputStream in,
			InputStream userkeyFile, InputStream groupkeyFile,
			char[] groupPass, char[] userPass) {
		InputStream groupKeyStream = null;
		/*
		 * Core.getSingleInstance().outputText("OS-BuildString:"+android.os.Build
		 * .VERSION.RELEASE);
		 * Core.getSingleInstance().outputText("OS-Version>4.1:"+new
		 * Integer(EncodeDecodeAES
		 * .compareVersionStrings(android.os.Build.VERSION
		 * .RELEASE,"4.2")).toString());
		 * Core.getSingleInstance().outputText("User Password: "+new
		 * String(userPass));
		 * Core.getSingleInstance().userAlert("OS-BuildString:"
		 * +android.os.Build.VERSION.RELEASE);
		 * Core.getSingleInstance().userAlert("OS-Version>4.1:"+new
		 * Integer(EncodeDecodeAES
		 * .compareVersionStrings(android.os.Build.VERSION
		 * .RELEASE,"4.2")).toString());
		 * Core.getSingleInstance().userAlert("User Password: "+new
		 * String(userPass));
		 */
		try {
			groupKeyStream = PGPUtils.decryptFileStream(groupkeyFile,
					userkeyFile, userPass);
		} catch (NoSuchProviderException e1) {
			Core.getSingleInstance().userAlert("Group Keys: Internal Error. Details:"+e1.getMessage());
			e1.printStackTrace();
		} catch (IOException e1) {
			Core.getSingleInstance().userAlert(
					"Group Key Error: Can't read PGP Key file(s). Details:"+e1.getMessage());
		} catch (PGPException e1) {
			Core.getSingleInstance().userAlert(
					"Group Key Error: Invalid PGP Key or pass phrase. Details:"+e1.getMessage());
		} catch (IllegalArgumentException e1) {
			Core.getSingleInstance().userAlert(
					"Group Key Error: Failed to find private key. Details:"+e1.getMessage());
		}
		InputStream unc = null;
		try {
			unc = PGPUtils.decryptFileStream(in, groupKeyStream, groupPass);
		} catch (NoSuchProviderException e1) {
			Core.getSingleInstance().userAlert(
					"Script Encryption: Internal Error. Details:"+e1.getMessage());
			e1.printStackTrace();
		} catch (IOException e1) {
			Core.getSingleInstance().userAlert(
					"Script Encryption Error: Can't read PGP Key file(s). Details:"+e1.getMessage());
		} catch (PGPException e1) {
			Core.getSingleInstance().userAlert(
					"Script Encryption Error: Invalid group Key. Details:"+e1.getMessage());
		} catch (IllegalArgumentException e1) {
			Core.getSingleInstance().userAlert(
					"Script Encryption Error: Failed to find private key. Details:"+e1.getMessage());
		}
		try {
			userkeyFile.close();
			groupkeyFile.close();
		} catch (IOException e) {
		}
		return unc;
	}

}
