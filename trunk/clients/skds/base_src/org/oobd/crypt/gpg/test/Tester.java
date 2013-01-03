package org.oobd.crypt.gpg.test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import org.oobd.crypt.gpg.GroupDecoder;

public class Tester {

	private static final String PASSPHRASE_USER = "test";
	private static final String PASSPHRASE_GROUP = "abc";
	private static final String DE_INPUT = "gpg/script.pgp";
	private static final String DE_GROUP_INPUT = "gpg/skoehle6_groups.pgp";
	private static final String DE_OUTPUT = "gpg/script.decoded";
	private static final String DE_KEY_FILE = "gpg/userkey.sec";


	public static void main(String[] args) {
		try{
			//PGPUtils.init();
        		FileInputStream in = new FileInputStream(DE_INPUT);
        		FileOutputStream out = new FileOutputStream(DE_OUTPUT);
			InputStream unc = GroupDecoder.decryptGroup(
				in ,
				new FileInputStream(DE_KEY_FILE) ,
				new FileInputStream(DE_GROUP_INPUT) ,
				PASSPHRASE_GROUP.toCharArray(),
				PASSPHRASE_USER.toCharArray()); 
			int ch;
			while ((ch = unc.read()) >= 0) {
				out.write(ch);
			}

			in.close();
			out.close();
			unc.close();
		}catch (Exception ex){
			System.out.println("Exception "+ex.getMessage());
			ex.printStackTrace();
		}
	}
}

