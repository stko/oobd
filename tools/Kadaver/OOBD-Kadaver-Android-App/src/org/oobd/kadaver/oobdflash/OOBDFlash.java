package org.oobd.kadaver.oobdflash;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.oobd.kadaver.ActivityMain;

import android.os.Environment;
import android.os.Handler;


/**
 *
 * Minimal Services Search example.
 */
public class OOBDFlash {

	String downloadURL = "https://oobd.googlecode.com/svn/trunk/interface/Designs/CORTEX/STM32F103C8_Eclipse_GCC/D2/flashfiles/AllinOne/Flashloader_Package.zip";
	String flashFileName = "";
	String dongleMAC = "";
	File tempDownloadFile = null;
	Handler mReportStatusHandler;
	InputStream is;
	OutputStream os;
	
	/**
	 * 
	 * @param dongleMAC needs always be provided
	 * @param downloadURL can be null
	 * @param flashFileName can be null
	 * @param h Handler for UI Response
	 * @param o Outputstream of BT connection
	 * @param i Inputstream of BT connection
	 */
	public OOBDFlash(String dongleMAC, String downloadURL, String flashFileName, Handler h, OutputStream o, InputStream i){
		this.dongleMAC = dongleMAC;
		if(downloadURL!=null){
			this.downloadURL = downloadURL;
		}
		if(flashFileName!=null){
			this.flashFileName = flashFileName;
		}
		os = o;
		is = i;
		setHandler(h);
	}
	
	public void setHandler(Handler h){
		this.mReportStatusHandler = h;
		OOBDFileHandler.setHandler(h);
	}
	
	public void startFlash() {
		if(ActivityMain.mActivity!= null){
			ActivityMain.mActivity.setFlashStatusText("Get File...", false);
		}
		// No local firmware file given? Then we need to download it
		if (flashFileName.equalsIgnoreCase("")) {
//			this.mReportStatusHandler.obtainMessage(org.oobd.kadaver.Constants.HANDLER_FLASH_TOAST, 0,0,"Download: 1 StartFlash").sendToTarget();
			System.out.println("FLASH ORDER: 9 startFlash() OOBDFlash.java");
			try {
				File parentDir = new File(Environment.getExternalStorageDirectory() + org.oobd.kadaver.Constants.KADAVER_FOLDER);
			       if(!parentDir.exists()){
			    	   parentDir.mkdirs();
			       }
				tempDownloadFile =new File(parentDir, "OOBD_Firmware_Download.zip");
			} catch (Exception ex) {
				ex.printStackTrace();
//				this.mReportStatusHandler.obtainMessage(org.oobd.kadaver.Constants.HANDLER_FLASH_TOAST, 1,0, "Download: Error: can not create temporary download file - Terminating..").sendToTarget();
				System.err.println("Error: can not create temporary download file - Terminating..");
				if(ActivityMain.mActivity!= null){
					ActivityMain.mActivity.setFlashStatusText("Error: can not create temporary download file - Terminating", true);
				}
				return;
//				System.exit(1);
			}
			if (OOBDFileHandler.download(downloadURL, tempDownloadFile.getAbsolutePath())) {
//				this.mReportStatusHandler.obtainMessage(org.oobd.kadaver.Constants.HANDLER_FLASH_TOAST, 0,0, "Download: 2 GetFileList").sendToTarget();
//				OOBDFileHandler.getFilelist("", 0);
				flashFileName = tempDownloadFile.getAbsolutePath();
				if(flashFileName.endsWith(".zip")){
					String filename = OOBDFileHandler.unpackZip(flashFileName);
					System.out.println("FLASH filename " + filename);
					if (filename == null) {
//						this.mReportStatusHandler.obtainMessage(org.oobd.kadaver.Constants.HANDLER_FLASH_TOAST, 1,0,"FlashMode: Error:Can not read from download archive:" + downloadURL + "- Terminating").sendToTarget();
						if(ActivityMain.mActivity!= null){
							ActivityMain.mActivity.setFlashStatusText("Error: Can not read from zip archive:" + flashFileName + " - Terminating", true);
						}
						System.err.println("Error:Can not read from zip archive:" + flashFileName + " - Terminating");
						return;
//						System.exit(1);
					}
				if (OOBDFlashHandler.switchToFlashMode(dongleMAC, os, is)) {
						FileInputStream fileInputStream = null;
						File file = null;
						try {
							fileInputStream = new FileInputStream(filename);
							file = new File(filename);
	
						} catch (IOException ex) {
							System.out.println("FLASH DOWNLOADED FILE CATCH:  " + ex.getLocalizedMessage());
							if(ActivityMain.mActivity!= null){
								ActivityMain.mActivity.setFlashStatusText("Error: Can not read local firmware file:" + filename + " - Terminating", true);
							}
							System.err.println("Error: Can not read local firmware file:" + filename + " - Terminating");
							return;
	//						System.exit(1);
						}
						YModem1K myYModem = new YModem1K();
	//					this.mReportStatusHandler.obtainMessage(org.oobd.kadaver.Constants.HANDLER_FLASH_TOAST, 0,0, "File: 2 yModem created").sendToTarget();
						System.out.println("FLASH DOWNLOADED FILE 1:  " + myYModem);
						System.out.println("FLASH DOWNLOADED FILE 1:  " + OOBDFlashHandler.getInputStream());
						System.out.println("FLASH DOWNLOADED FILE 1:  " + OOBDFlashHandler.getOutputStream());
						if (myYModem.ymodemtransfer(OOBDFlashHandler.getInputStream(), OOBDFlashHandler.getOutputStream(), fileInputStream, file.getName(), file.length())) {
	//						this.mReportStatusHandler.obtainMessage(org.oobd.kadaver.Constants.HANDLER_FLASH_TOAST, 1,0, "File: 3 Dongle successfully Flashed!").sendToTarget();
							if(ActivityMain.mActivity!= null){
								ActivityMain.mActivity.isFlashSuccessful = true;
								ActivityMain.mActivity.setFlashStatusText("Done: Dongle successfully Flashed!", true);
							}
							System.err.println("Dongle successfully Flashed!");
						} else {
							if(ActivityMain.mActivity!= null){
								ActivityMain.mActivity.setFlashStatusText("Error: Dongle not flashed!", true);
							}
							System.err.println("Error: Dongle not flashed :-(");
							return;
						}
						try {
							fileInputStream.close();
						} catch (IOException ex) {}
						OOBDFlashHandler.resetDongle();
						return;
					} else {
						
					}
				}
			} else {
//				this.mReportStatusHandler.obtainMessage(org.oobd.kadaver.Constants.HANDLER_FLASH_TOAST, 1,0, "Download: Error: can not download firmware Archive from " + downloadURL + " - Terminating..").sendToTarget();
				if(ActivityMain.mActivity!= null){
					ActivityMain.mActivity.setFlashStatusText("Error: can not download firmware Archive from " + downloadURL + " - Terminating", true);
				}
				System.err.println("Error: can not download firmware Archive from " + downloadURL + " - Terminating..");
				return;
//				System.exit(1);
			}
		}
		else { //non -interactive mode
			if (OOBDFlashHandler.switchToFlashMode(dongleMAC, os, is)) {
				if (flashFileName.equalsIgnoreCase("")) {
//					this.mReportStatusHandler.obtainMessage(org.oobd.kadaver.Constants.HANDLER_FLASH_TOAST, 0,0, "FlashMode: 1 StartFlash").sendToTarget();
					if (!OOBDFileHandler.OpenPreferedFirmwareInputStream()) {
//						this.mReportStatusHandler.obtainMessage(org.oobd.kadaver.Constants.HANDLER_FLASH_TOAST, 1,0,"FlashMode: Error:Can not read from download archive:" + downloadURL + "- Terminating").sendToTarget();
						if(ActivityMain.mActivity!= null){
							ActivityMain.mActivity.setFlashStatusText("Error: Can not read from download archive: " + downloadURL + " - Terminating", true);
						}
						System.err.println("Error: Can not read from download archive:" + downloadURL + "- Terminating");
						return;
//						System.exit(1);
					}

					YModem1K myYModem = new YModem1K();
//					this.mReportStatusHandler.obtainMessage(org.oobd.kadaver.Constants.HANDLER_FLASH_TOAST, 0,0, "FlashMode: 2 yModem created").sendToTarget();
					
					if (myYModem.ymodemtransfer(OOBDFlashHandler.getInputStream(), OOBDFlashHandler.getOutputStream(), OOBDFileHandler.getPreferedFirmwareInputStream(), OOBDFileHandler.getPreferedFirmwareName(), OOBDFileHandler.getPreferedFirmwareSize())) {
						//						if (myYModem.ymodemtransfer(OOBDFlashHandler.getInputStream(), OOBDFlashHandler.getOutputStream(), fileInputStream,                                file.getName(),                              file.length())){
//						this.mReportStatusHandler.obtainMessage(org.oobd.kadaver.Constants.HANDLER_FLASH_TOAST, 1,0, "FlashMode: 3 Dongle successfully Flashed!").sendToTarget();
						System.err.println("Dongle successfully Flashed!");
						if(ActivityMain.mActivity!= null){
							ActivityMain.mActivity.isFlashSuccessful = true;
							ActivityMain.mActivity.setFlashStatusText("Done: Dongle successfully Flashed!", true);
						}
					}
					OOBDFileHandler.ClosePreferedFirmwareInputStream();
					return;
				} else { // flash local firmware file
//					this.mReportStatusHandler.obtainMessage(org.oobd.kadaver.Constants.HANDLER_FLASH_TOAST, 0,0, "File: 1 StartFlash").sendToTarget();
					System.out.println("FLASH ORDER: 13 startFlash() OOBDFlash.java " + flashFileName);
					
					if(flashFileName.endsWith(".zip")){
						String filename = OOBDFileHandler.unpackZip(flashFileName);
						System.out.println("FLASH filename " + filename);
						if (filename == null) {
//							this.mReportStatusHandler.obtainMessage(org.oobd.kadaver.Constants.HANDLER_FLASH_TOAST, 1,0,"FlashMode: Error:Can not read from download archive:" + downloadURL + "- Terminating").sendToTarget();
							if(ActivityMain.mActivity!= null){
								ActivityMain.mActivity.setFlashStatusText("Error: Can not read from zip archive:" + flashFileName + " - Terminating", true);
							}
							System.err.println("Error:Can not read from zip archive:" + flashFileName + " - Terminating");
							return;
//							System.exit(1);
						}

						FileInputStream fileInputStream = null;
						File file = null;
						try {
							fileInputStream = new FileInputStream(filename);
							file = new File(filename);
	
						} catch (IOException ex) {
	//						this.mReportStatusHandler.obtainMessage(org.oobd.kadaver.Constants.HANDLER_FLASH_TOAST, 1,0,"File: Error:Can not read local firmware file:" + flashFileName + "- Terminating").sendToTarget();
							if(ActivityMain.mActivity!= null){
								ActivityMain.mActivity.setFlashStatusText("Error: Can not read local firmware file:" + filename + " - Terminating", true);
							}
							System.err.println("Error: Can not read local firmware file:" + filename + " - Terminating");
							return;
	//						System.exit(1);
						}
						YModem1K myYModem = new YModem1K();
	//					this.mReportStatusHandler.obtainMessage(org.oobd.kadaver.Constants.HANDLER_FLASH_TOAST, 0,0, "File: 2 yModem created").sendToTarget();
	
						
						if (myYModem.ymodemtransfer(OOBDFlashHandler.getInputStream(), OOBDFlashHandler.getOutputStream(), fileInputStream, file.getName(), file.length())) {
	//						this.mReportStatusHandler.obtainMessage(org.oobd.kadaver.Constants.HANDLER_FLASH_TOAST, 1,0, "File: 3 Dongle successfully Flashed!").sendToTarget();
							if(ActivityMain.mActivity!= null){
								ActivityMain.mActivity.isFlashSuccessful = true;
								ActivityMain.mActivity.setFlashStatusText("Done: Dongle successfully Flashed!", true);
							}
							System.err.println("Dongle successfully Flashed!");
						} else {
							if(ActivityMain.mActivity!= null){
								ActivityMain.mActivity.setFlashStatusText("Error: Dongle not flashed!", true);
							}
							System.err.println("Error: Dongle not flashed :-(");
							return;
						}
						try {
							fileInputStream.close();
						} catch (IOException ex) {}
					}
					else {
						FileInputStream fileInputStream = null;
						File file = null;
						try {
							fileInputStream = new FileInputStream(flashFileName);
							file = new File(flashFileName);
	
						} catch (IOException ex) {
	//						this.mReportStatusHandler.obtainMessage(org.oobd.kadaver.Constants.HANDLER_FLASH_TOAST, 1,0,"File: Error:Can not read local firmware file:" + flashFileName + "- Terminating").sendToTarget();
							if(ActivityMain.mActivity!= null){
								ActivityMain.mActivity.setFlashStatusText("Error: Can not read local firmware file:" + flashFileName + " - Terminating", true);
							}
							System.err.println("Error:Can not read local firmware file:" + flashFileName + "- Terminating");
							return;
	//						System.exit(1);
						}
						YModem1K myYModem = new YModem1K();
	//					this.mReportStatusHandler.obtainMessage(org.oobd.kadaver.Constants.HANDLER_FLASH_TOAST, 0,0, "File: 2 yModem created").sendToTarget();
	
						//						if (myYModem.ymodemtransfer(OOBDFlashHandler.getInputStream(), OOBDFlashHandler.getOutputStream(), OOBDFileHandler.getPreferedFirmwareInputStream(),OOBDFileHandler.getPreferedFirmwareName(), OOBDFileHandler.getPreferedFirmwareSize())){
						
						if (myYModem.ymodemtransfer(OOBDFlashHandler.getInputStream(), OOBDFlashHandler.getOutputStream(), fileInputStream, file.getName(), file.length())) {
	//						this.mReportStatusHandler.obtainMessage(org.oobd.kadaver.Constants.HANDLER_FLASH_TOAST, 1,0, "File: 3 Dongle successfully Flashed!").sendToTarget();
							if(ActivityMain.mActivity!= null){
								ActivityMain.mActivity.isFlashSuccessful = true;
								ActivityMain.mActivity.setFlashStatusText("Done: Dongle successfully Flashed!", true);
							}
							System.err.println("Dongle successfully Flashed!");
						} else {
							if(ActivityMain.mActivity!= null){
								ActivityMain.mActivity.setFlashStatusText("Error: Dongle not flashed!", true);
							}
							System.err.println("Error: Dongle not flashed :-(");
							return;
						}
						try {
							fileInputStream.close();
						} catch (IOException ex) {}
					}
				}
				OOBDFlashHandler.resetDongle();
			} else {
//				this.mReportStatusHandler.obtainMessage(org.oobd.kadaver.Constants.HANDLER_FLASH_TOAST, 1,0, "File: Flash mode NOT reached!").sendToTarget();
				System.err.println("Flash mode NOT reached!");
//				if(ActivityMain.mActivity!= null){
//					ActivityMain.mActivity.isFlashSuccessful = true;
//					ActivityMain.mActivity.setFlashStatusText("Done: F!", true);
//				}
			}
			OOBDFlashHandler.close();
		}
	}

	

}