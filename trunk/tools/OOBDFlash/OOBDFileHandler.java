import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.zip.*;
import java.util.Vector;
import java.util.Enumeration;
import java.util.regex.*;

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

	public static boolean download(String urlString, String outputFile) {
		try {
			System.out.println("Try to download Firmware Archive\nfrom URL:" + urlString + "\ninto temp file:" + outputFile);


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