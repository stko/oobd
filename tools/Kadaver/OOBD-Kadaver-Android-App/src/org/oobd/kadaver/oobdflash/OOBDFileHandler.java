package org.oobd.kadaver.oobdflash;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.oobd.kadaver.ActivityMain;
import org.oobd.kadaver.Constants;

import android.os.Environment;
import android.os.Handler;

public class OOBDFileHandler {

	// all about Proxies and URLs in Java can be found here: http://www.rgagnon.com/javadetails/java-0085.html

	static String downloadFileName = "";
	static String FilelistContent = "";
	static String preferedFirmwareFileName = null;
	static String httpProxyHost = null;
	static int httpProxyPort = 0;
	static InputStream preferedFirmwareInputStream = null;
	static long preferedFirmwareSize = 0;
	static ZipFile zipFile = null;
	static Handler h;
	
	public static void setHandler(Handler handler){
		h = handler;
	}

	public static boolean download(String urlString, String outputFile) {
		try {
			System.out.println("Try to download Firmware Archive\nfrom URL:" + urlString + "\ninto temp file:" + outputFile);

			if(ActivityMain.mActivity!= null){
				ActivityMain.mActivity.setFlashStatusText("Downloading File...", false);
			}
			Properties props = new Properties();
			try {
				props.load(new FileInputStream("OOBDFlash.props"));
			} catch (IOException ignored) {}
			httpProxyPort = Integer.parseInt(props.getProperty("httpProxyPort", "0"));
			httpProxyHost = props.getProperty("httpProxyHost", null);
			URL url = new URL(urlString);
			URLConnection urconn = null;
			if (httpProxyPort != 0 && httpProxyHost != null) {
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpProxyHost, httpProxyPort));
				urconn = url.openConnection(proxy);
			} else {
				System.setProperty("java.net.useSystemProxies", "true"); // use system proxy settings
				urconn = url.openConnection();
			}


			InputStream isr = urconn.getInputStream();
			FileOutputStream os = new FileOutputStream(outputFile);
			byte[] buf = new byte[1024];
			int recvlen = 0;
			while (recvlen != -1) {
				recvlen = isr.read(buf);
				if (recvlen > 0) {
					os.write(buf, 0, recvlen);
				}
			}
			os.close();
			isr.close();
			System.out.println(":download completed.");
			downloadFileName = outputFile;
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			downloadFileName = "";
			
			System.out.println(":Download failed!");
			return false;
		}
	}

	public static Vector < String > getFilelist(String hardwareID, int Revision) {
		if (downloadFileName == "") {
			return null;
		}
		try {
			System.out.println("Examinating downloaded Firmware Archive: ");
			zipFile = new ZipFile(downloadFileName);
			Enumeration <? extends ZipEntry > entries = zipFile.entries();
			Vector < String > res = new Vector < String > ();
			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) entries.nextElement();
				String fullPath = zipEntry.toString();
				String fileName = "";
				//System.out.println("Zip:"+zipEntry.toString());
				if (fullPath.matches(".*filelist$")) {
					//System.out.println("filelist found: "+fullPath);
					// get filelist entry
					try {
						ZipEntry zipEntryFilelist = zipFile.getEntry(fullPath);
						long size = zipEntryFilelist.getSize();
						byte[] buffer = new byte[(int) size];
						InputStream zipFileListInputStream = zipFile.getInputStream(zipEntryFilelist);
						int bytes_read = zipFileListInputStream.read(buffer); //read filelist into buffer
						zipFileListInputStream.close();
						FilelistContent = new String(buffer, 0, bytes_read);
						//System.out.println("Filelist content:\n"+FilelistContent);
						Matcher matcher = Pattern.compile("(export FWBIN=)(.+)\\n").matcher(FilelistContent);
						if (matcher.find()) {
							System.out.println("prefered Firmware: " + matcher.group(2));
							preferedFirmwareFileName = "Flashloader_Package/" + matcher.group(2);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
				//start to evaluate the file name
				//first, get the filename only
				Matcher matcher = Pattern.compile("[^/]+$").matcher(fullPath);
				if (matcher.find()) {
					fileName = matcher.group(); //filename without path
					Matcher matcher2 = Pattern.compile("(OOBD_FW_)(\\w+)_(\\d+)\\.bin").matcher(fileName);
					if (matcher2.find()) { // is it a file name matching the firmware binary file naming convention?
						System.out.println("Hardware: " + matcher2.group(2));
						System.out.println("Revision: " + matcher2.group(3));

						res.add(zipEntry.toString());
					}
				}
			}
			zipFile.close();
			zipFile = null;
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static String unpackZip(String zipname)
	{    
		if(ActivityMain.mActivity!= null){
			ActivityMain.mActivity.setFlashStatusText("Unpacking ZipFile...", false);
		}
	     InputStream is;
	     ZipInputStream zis;
	     String path, zipfilename;
	     try 
	     {
	         String filename, binFilename = null;
	         path = zipname.substring(0, zipname.lastIndexOf("/")+1);
	         zipfilename = zipname.substring(zipname.lastIndexOf("/")+1, zipname.length()-4);
	         
	         is = new FileInputStream(zipname);
	         zis = new ZipInputStream(new BufferedInputStream(is));          
	         ZipEntry ze;
	         byte[] buffer = new byte[1024];
	         int count;
	         File parentDir = new File(Environment.getExternalStorageDirectory() + Constants.KADAVER_FOLDER);
	         if(!parentDir.exists()){
	      	   parentDir.mkdirs();
	         }
	         while ((ze = zis.getNextEntry()) != null) 
	         {
	        	 
	             // zapis do souboru
	             filename = ze.getName();
	             
	            	 
	             // Need to create directories if not exists, or
	             // it will generate an Exception...
	             if (ze.isDirectory()) {
	                File fmd = new File(filename);
	                fmd.mkdirs();
	                continue;
	             }
	             if(filename.endsWith(".bin")){
//		             filename = filename.replace("/", "_");
	            	 filename = filename.substring(filename.lastIndexOf("/")+1);
	            	 filename = zipfilename + "_" + filename;
//		             binFilename = path+filename;
		             binFilename = parentDir+File.separator+filename;
		             FileOutputStream fout = new FileOutputStream(parentDir+File.separator+filename);
	
		             // cteni zipu a zapis
		             while ((count = zis.read(buffer)) != -1) 
		             {
		                 fout.write(buffer, 0, count);             
		             }
		             
		             fout.close();    
	             }
	             zis.closeEntry();
	         }

	         zis.close();
	         return binFilename;
	     } 
	     catch(IOException e)
	     {
	    	 if(ActivityMain.mActivity!= null){
	 			ActivityMain.mActivity.setFlashStatusText("Error: Unpacking ZipFile...", true);
	 		 }
	         e.printStackTrace();
	         return null;
	     }

	   
	}
	
	public static boolean OpenPreferedFirmwareInputStream() {
		if (downloadFileName == "" || preferedFirmwareFileName == null) {
			return false;
		}
		try {
			zipFile = new ZipFile(downloadFileName);
			if (zipFile == null) {
				System.out.println("can't open Zip File???");
			}
			System.out.println("Using Archive:" + downloadFileName + "\ncontaining the prefered Firmware: " + preferedFirmwareFileName);
			ZipEntry zipEntryFilelist = zipFile.getEntry(preferedFirmwareFileName);
			preferedFirmwareInputStream = zipFile.getInputStream(zipEntryFilelist);
			preferedFirmwareSize = zipEntryFilelist.getSize();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public static String getPreferedFirmwareName() {
		return preferedFirmwareFileName;
	}

	public static InputStream getPreferedFirmwareInputStream() {
		return preferedFirmwareInputStream;
	}

	public static long getPreferedFirmwareSize() {
		return preferedFirmwareSize;
	}

	public static void ClosePreferedFirmwareInputStream() {
		try {
			if (preferedFirmwareInputStream != null) {
				preferedFirmwareInputStream.close();
				preferedFirmwareInputStream = null;
			}
			if (zipFile != null) {
				zipFile.close();
				zipFile = null;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}


}