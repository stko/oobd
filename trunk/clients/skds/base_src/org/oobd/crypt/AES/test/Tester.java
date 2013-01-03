package org.oobd.crypt.AES.test;

import org.oobd.crypt.AES.EncodeDecodeAES;

/**
 * Basic symmetric encryption example
 */
public class Tester {


	public static void main(String[] args) throws Exception {
		try {
			String seed=args[0];
			String textToEncrypt=args[1];
			
			byte[] encrypted = EncodeDecodeAES.encryptBytes(seed, textToEncrypt.getBytes());
			System.out.println(" String seed = \""+seed+"\";");
			System.out.print(" byte[] passPhrase={ ");
			for (int i=0; i< encrypted.length;i++){
				System.out.print(encrypted[i]);
				System.out.print(" , ");
			}
			System.out.println(" } ; ");
			String decrypted = new String(EncodeDecodeAES.decryptBytes(seed, encrypted));
			System.out.println("Decrypt: "+ decrypted);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

           
         
