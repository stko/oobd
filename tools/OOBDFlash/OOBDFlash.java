import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.bluetooth.*;
import java.io.*;
import javax.microedition.io.*;
import java.net.URI;


/**
 *
 * Minimal Services Search example.
 */
public class OOBDFlash {


	public static void main(String[] args) {
		String downloadURL = "https://github.com/stko/oobd/blob/master/interface/Designs/CORTEX/STM32F103C8_Eclipse_GCC/D2/flashfiles/AllinOne/Flashloader_Package.zip?raw=true";
		String flashFileName = "";
		String dongleMAC = "";
		File tempDownloadFile = null;

		//parsing the command line
		if (args.length > 0) { //Arguments?
			if (args.length % 2 != 0) { //as the arguments are always pairs, the number of args must be even
				usage();
			}
			for (int i = 0; i < args.length; i += 2) {
				if ("-l".equalsIgnoreCase(args[i])) {
					downloadURL = new File(args[i + 1]).toURI().toString();
				} else {
					if ("-u".equalsIgnoreCase(args[i])) {
						downloadURL = args[i + 1];
					} else {
						if ("-f".equalsIgnoreCase(args[i])) {
							flashFileName = args[i + 1];
						} else {
							if ("-d".equalsIgnoreCase(args[i])) {
								dongleMAC = "btspp://" + args[i + 1] + ":1;authenticate=false;encrypt=false;master=false";
							} else {
								System.err.println("unknown option: " + args[i]);
								usage();
							}
						}
					}
				}
			}
		}


		// No local firmware file given? Then we need to download it
		if (flashFileName.equalsIgnoreCase("")) {
			try {
				tempDownloadFile = File.createTempFile("OOBD_Firmware_Download", ".zip");
			} catch (IOException ex) {
				ex.printStackTrace();
				System.err.println("Error: can not create temporary download file - Terminating..");
				System.exit(1);
			}
			if (OOBDFileHandler.download(downloadURL, tempDownloadFile.getAbsolutePath())) {
				OOBDFileHandler.getFilelist("", 0);
			} else {
				System.err.println("Error: can not download firmware Archive from " + downloadURL + " - Terminating..");
				System.exit(1);
			}
		}
		if (dongleMAC.equalsIgnoreCase("")) { // no Dongle MAC given? So we are interactive
			// First run RemoteDeviceDiscovery and use discoved device
			Vector < OOBDDongleDescriptor > serviceFound = new Vector < OOBDDongleDescriptor > ();
			BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
			String cmd = "d";
			while (!cmd.equalsIgnoreCase("e")) {
				if (cmd.equalsIgnoreCase("d")) { //scan for dongles
					System.err.print("scan for dongles:");
					serviceFound = OobdBtWrapper.discover();
					for (int i = 0; i < serviceFound.size(); i++) {
						OOBDDongleDescriptor oobdDongleDescr = (OOBDDongleDescriptor) serviceFound.get(i);
						System.err.print("d");
						if (!OOBDFlashHandler.examine(oobdDongleDescr)) {
							serviceFound.remove(i);
							i--;
						}
						OOBDFlashHandler.close();
					}
				}
				System.err.println("\n--------------------------------------------------------\nNr. of Dongles found: " + Integer.toString(serviceFound.size()));
				if (serviceFound.size() > 0) {
					System.err.println("Select the number of the dongle to flash");
					for (int i = 0; i < serviceFound.size(); i++) {
						System.err.print("\t" + Integer.toString(i + 1) + ":");
						System.err.print("\t" + ((OOBDDongleDescriptor) serviceFound.get(i)).friendlyName);
						System.err.print("\t" + ((OOBDDongleDescriptor) serviceFound.get(i)).hardwareID);
						System.err.print("\t" + ((OOBDDongleDescriptor) serviceFound.get(i)).revision);
						//System.err.print("\t" + ((OOBDDongleDescriptor) serviceFound.get(i)).url);
						System.err.println();
					}
					System.err.println("-or-");
				}
				System.err.println("Select one of the commands");
				System.err.println("\td:\tnew Device discovery");
				System.err.println("\te:\tEnd");
				System.err.print(">");
				try {
					cmd = buffer.readLine();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
				int device = 0;
				try {
					device = Integer.parseInt(cmd);
				} catch (NumberFormatException ex) {
					device = 0;
				}
				if (device > 0 && device <= serviceFound.size()) {
					OOBDDongleDescriptor oobdDongleDescr = (OOBDDongleDescriptor) serviceFound.get(device - 1);
					System.err.println("Flashing " + oobdDongleDescr.friendlyName);
					if (OOBDFlashHandler.switchToFlashMode(oobdDongleDescr.url)) {
						if (flashFileName.equalsIgnoreCase("")) {
							if (!OOBDFileHandler.OpenPreferedFirmwareInputStream()) {
								System.err.println("Error:Can not read from download archive:" + downloadURL + "- Terminating");
								System.exit(1);
							}

							YModem1K myYModem = new YModem1K();
							if (myYModem.ymodemtransfer(OOBDFlashHandler.getInputStream(), OOBDFlashHandler.getOutputStream(), OOBDFileHandler.getPreferedFirmwareInputStream(), OOBDFileHandler.getPreferedFirmwareName(), OOBDFileHandler.getPreferedFirmwareSize())) {
								//						if (myYModem.ymodemtransfer(OOBDFlashHandler.getInputStream(), OOBDFlashHandler.getOutputStream(), fileInputStream,                                file.getName(),                              file.length())){
								System.err.println("Dongle successfully Flashed!");
							}
							OOBDFileHandler.ClosePreferedFirmwareInputStream();
						} else { // flash local firmware file

							FileInputStream fileInputStream = null;
							File file = null;
							try {
								fileInputStream = new FileInputStream(flashFileName);
								file = new File(flashFileName);

							} catch (IOException ex) {
								System.err.println("Error:Can not read local firmware file:" + flashFileName + "- Terminating");
								System.exit(1);
							}
							YModem1K myYModem = new YModem1K();
							//						if (myYModem.ymodemtransfer(OOBDFlashHandler.getInputStream(), OOBDFlashHandler.getOutputStream(), OOBDFileHandler.getPreferedFirmwareInputStream(),OOBDFileHandler.getPreferedFirmwareName(), OOBDFileHandler.getPreferedFirmwareSize())){
							if (myYModem.ymodemtransfer(OOBDFlashHandler.getInputStream(), OOBDFlashHandler.getOutputStream(), fileInputStream, file.getName(), file.length())) {
								System.err.println("Dongle successfully Flashed!");
							} else {
								System.err.println("Error: Dongle not flashed :-(");
							}
							try {
								fileInputStream.close();
							} catch (IOException ex) {}
						}
						OOBDFlashHandler.resetDongle();
					} else {
						System.err.println("Flash mode NOT reached!");
					}
					OOBDFlashHandler.close();
				}
			}
		} else { //non -interactive mode
			if (OOBDFlashHandler.switchToFlashMode(dongleMAC)) {
				if (flashFileName.equalsIgnoreCase("")) {
					if (!OOBDFileHandler.OpenPreferedFirmwareInputStream()) {
						System.err.println("Error:Can not read from download archive:" + downloadURL + "- Terminating");
						System.exit(1);
					}

					YModem1K myYModem = new YModem1K();
					if (myYModem.ymodemtransfer(OOBDFlashHandler.getInputStream(), OOBDFlashHandler.getOutputStream(), OOBDFileHandler.getPreferedFirmwareInputStream(), OOBDFileHandler.getPreferedFirmwareName(), OOBDFileHandler.getPreferedFirmwareSize())) {
						//						if (myYModem.ymodemtransfer(OOBDFlashHandler.getInputStream(), OOBDFlashHandler.getOutputStream(), fileInputStream,                                file.getName(),                              file.length())){
						System.err.println("Dongle successfully Flashed!");
					}
					OOBDFileHandler.ClosePreferedFirmwareInputStream();
				} else { // flash local firmware file

					FileInputStream fileInputStream = null;
					File file = null;
					try {
						fileInputStream = new FileInputStream(flashFileName);
						file = new File(flashFileName);

					} catch (IOException ex) {
						System.err.println("Error:Can not read local firmware file:" + flashFileName + "- Terminating");
						System.exit(1);
					}
					YModem1K myYModem = new YModem1K();
					//						if (myYModem.ymodemtransfer(OOBDFlashHandler.getInputStream(), OOBDFlashHandler.getOutputStream(), OOBDFileHandler.getPreferedFirmwareInputStream(),OOBDFileHandler.getPreferedFirmwareName(), OOBDFileHandler.getPreferedFirmwareSize())){
					if (myYModem.ymodemtransfer(OOBDFlashHandler.getInputStream(), OOBDFlashHandler.getOutputStream(), fileInputStream, file.getName(), file.length())) {
						System.err.println("Dongle successfully Flashed!");
					} else {
						System.err.println("Error: Dongle not flashed :-(");
					}
					try {
						fileInputStream.close();
					} catch (IOException ex) {}
				}
				OOBDFlashHandler.resetDongle();
			} else {
				System.err.println("Flash mode NOT reached!");
			}
			OOBDFlashHandler.close();
		}
	}

	static void usage() {
		System.err.println("oobdflash - flashes OOBD Dongles easily");
		System.err.println("OOBDFlash is part of the OOBD.org tool set");
		System.err.println();
		System.err.println("Usage:");
		System.err.println("java -jar OOBDFlash.jar [-d BT-MAC] [-f firmwarefile | -u Download-URL | -l Archivefile]");
		System.err.println();
		System.err.println("Options:");
		System.err.println();
		System.err.println("-d BT-MAC : Uses given Dongle Bluetooth MAC address (Format: 12 char hex string: 00112233445566) to contact Dongle");
		System.err.println("if not given, device discovery is started");
		System.err.println();
		System.err.println("-f firmwarefile : Uses local firmwarefile to flash");
		System.err.println("if not given, Firmware archive is downloaded and extracted from Internet");
		System.err.println();
		System.err.println("-u Download-URL : Uses alternative URL to download firmware archive");
		System.err.println("for local files use option -l instead");
		System.err.println("if not given, build-in default URL is used");
		System.err.println();
		System.err.println("-l Archivefile : Uses local file as firmware archive");
		System.err.println();

		System.exit(0);
	}

}
