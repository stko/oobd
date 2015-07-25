package org.oobd.kadaver.oobdflash;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import java.io.*;

import org.oobd.kadaver.ActivityMain;




class OOBDFlashHandler {

	
	static OutputStream os = null;
	static InputStream is = null;

	public static boolean examine(OOBDDongleDescriptor dongle, OutputStream o, InputStream i) {

		try {
			System.out.print(".");
			
			os = o;
			is = i;
			byte buffer[] = new byte[1024];
			int bytes_read;
			String greeting = "p 0 0 0\r";
			os.write("\r\rp 0 0 0\r".getBytes());
			sleep(200);
			while (is.available() > 0) {
				is.read(buffer);
				sleep(200);
			}
			echoWrite(greeting);
			String received = readln();
			echoWrite(greeting);
			received = readln();
			received = received.replace("\r", "\n");
			String[] temp = received.split("\\s+");
			//in case just the bootloader is active, we do the following steps to have the bootloader string in received, before we close the socket
			echoWrite("\n");
			received = readln();
			received = received.replace("\r", "\n");


			/* print substrings */
			/*
			    for(int i =0; i < temp.length ; i++) {
			        System.out.println(Integer.toString(i)+":"+ temp[i]);
			    }
			    */
			close();
			//System.out.println("\nexamine string:"+received);
			if (temp.length > 4 && "OOBD".equals(temp[0])) {
				dongle.hardwareID = temp[1];
				dongle.revision = temp[2];
				dongle.design = temp[3];
				dongle.layout = temp[4];

				return true;
			} else {
				//look for the bootloader string
				temp = received.split("\\s+");
				if (temp.length > 0 & temp[temp.length - 1].equals("OOBD-Flashloader>")) {
					System.out.print("bootloader active-");
					dongle.hardwareID = "0";
					dongle.revision = "0";
					dongle.design = "0";
					dongle.layout = "0";
					return true;
				}
				return false;
			}

		} catch (IOException e) {
			System.err.print(e.toString());
			return false;
		}

	}


	public static boolean switchToFlashMode(String dongleURL, OutputStream o, InputStream i) {
		if(ActivityMain.mActivity!= null){
			ActivityMain.mActivity.setFlashStatusText("Switch to FlashMode...", false);
			ActivityMain.mActivity.eventFlowControl(ActivityMain.BTSEND, null);
		}
		int runstatus = 3;
		try {
			System.out.println("FLASH ORDER: switchToFlashMode 1");
			os = o;
			is = i;
			byte buffer[] = new byte[1024];
			int bytes_read;
			while (runstatus > 0) {
				System.out.println("FLASH ORDER: switchToFlashMode 2 " +os + "   " );
				os.write("\r\n".getBytes());
				System.out.println("FLASH ORDER: switchToFlashMode 3");
				sleep(500);
				if(ActivityMain.mActivity!= null){
					ActivityMain.mActivity.eventFlowControl(ActivityMain.BTSEND, null);
				}
				System.out.println("FLASH ORDER: switchToFlashMode 3");
				String received = readln();
				received = received.replace("\r", "\n");
				//System.out.print("reveived: "+received);
				String[] temp = received.split("\\s+");
				if (temp.length > 0 & temp[temp.length - 1].equals(">")) {
					System.out.print("Firmware found-");
					echoWrite("p 0 99 2\r");
					sleep(100);
					echoWrite("fff");
					received = readln();
					echoWrite("\r\r\r");
					sleep(1000);
					received = readln();
					echoWrite("\r");
					sleep(1000);
					if(ActivityMain.mActivity!= null){
						ActivityMain.mActivity.eventFlowControl(ActivityMain.BTSEND, null);
					}
					received = readln();
					temp = received.split("\\s+");
				}
				System.out.println("FLASH ORDER: switchToFlashMode 3");
				if (temp.length > 0 & temp[temp.length - 1].equals("OOBD-Flashloader>")) {
					System.out.println("bootloader active-No Way back: start the flash upload...");
					echoWrite("1");
					if(ActivityMain.mActivity!= null){
						ActivityMain.mActivity.setFlashStatusText("Bootloader active - No Way back: Start the flash upload...", false);
					}
					return true;

				} else {
					System.out.println("FLASH ORDER: switchToFlashMode 4");
					runstatus -= 1; // reduce the trial conter by 1
					if (runstatus == 1) { // it seems serious, the dongle does not answer. Last chance is to kill a potential running ymodem- transfer..
						//Try to send YMODEM cancel signal 0x18 a few times..
						os.write("\u0018\u0018\u0018\u0018\u0018\u0018".getBytes());
						// Try to send YMODEM cancel signal 'a' a few times..
						os.write("aaaa".getBytes());
					}
				}

			}
		} catch (IOException e) {
			if(ActivityMain.mActivity!= null){
				ActivityMain.mActivity.setFlashStatusText("Error: FlashMode not reached...", true);
			}
			System.err.print(e.toString());
			return false;
		}
		return false;
	}

	public static InputStream getInputStream() {
		return is;
	}


	public static OutputStream getOutputStream() {
		return os;
	}

	public static void resetDongle() {
		System.out.println("try to start firmware again");
		echoWrite("3");
	}

	public static void sleep(int millisecs) {
		try {
			Thread.sleep(millisecs);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}

	}



	public static void close() {
		try {
			if (os != null) {
				os.close();
				os = null;
			}
			if (is != null) {
				is.close();
				is = null;
			}
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static String readln() {
		boolean recvDone = false;
		String res = "";
		byte buffer[] = new byte[1024];
		int bytes_read;
		while (!recvDone) {
			try {
				int ready = is.available();
				if (ready == 0) {
					sleep(500);
				}
				ready = is.available();
				if (ready != 0) {
					bytes_read = is.read(buffer);
					res = res + new String(buffer, 0, bytes_read);
					recvDone = res.substring(res.length() - 1).equals("\r");
				} else {
					recvDone = true;
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return res;
	}

	public static void echoWrite(String cmd) {
		byte buffer[] = new byte[1024];
		int bytes_read;
		try {
			os.flush();
			os.write(cmd.getBytes());
			bytes_read = is.read(buffer, 0, cmd.length());
		} catch (IOException ex) {
			ex.printStackTrace();
		}

	}


}