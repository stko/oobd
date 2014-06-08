import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.bluetooth.*;
import java.io.*;
import javax.microedition.io.*;


/**
*
* Minimal Services Search example.
*/
public class OOBDFlash {


	public static void main(String[] args)  {

		// First run RemoteDeviceDiscovery and use discoved device
		String cmd="d";
		Vector<OOBDDongleDescriptor> serviceFound =new Vector<OOBDDongleDescriptor>() ;
		BufferedReader buffer=new BufferedReader(new InputStreamReader(System.in));
		if (OOBDFileHandler.download("https://oobd.googlecode.com/files/Flashloader_Package.zip", "download.zip")) {
			OOBDFileHandler.getFilelist("",0);
		}

		//System.exit(0);
		while (!cmd.equalsIgnoreCase("e")) {
			if (cmd.equalsIgnoreCase("d")) { //scan for dongles
				System.out.print("scan for dongles");
				serviceFound =OobdBtWrapper.discover();
				for (int i =0; i<serviceFound.size(); i++) {
					OOBDDongleDescriptor oobdDongleDescr = (OOBDDongleDescriptor)serviceFound.get(i);
					System.out.print("d");
					if (!OOBDFlashHandler.examine(oobdDongleDescr)) {
						serviceFound.remove(i);
						i--;
					}
					OOBDFlashHandler.close();
				}
			}
			System.out.println("--------------------------------------------------------\nNr. of Dongles found: "+Integer.toString(serviceFound.size()));
			if (serviceFound.size()>0) {
				System.out.println("Select the number of the dongle to flash");
				for (int i =0; i<serviceFound.size(); i++) {
					System.out.print("\t"+Integer.toString(i+1)+":");
					System.out.print("\t"+((OOBDDongleDescriptor)serviceFound.get(i)).friendlyName);
					System.out.print("\t"+((OOBDDongleDescriptor)serviceFound.get(i)).hardwareID);
					System.out.print("\t"+((OOBDDongleDescriptor)serviceFound.get(i)).revision);
					System.out.print("\t"+((OOBDDongleDescriptor)serviceFound.get(i)).url);
					System.out.println();
				}
				System.out.println("-or-");
			}
			System.out.println("Select one of the commands");
			System.out.println("\td:\tnew Device discovery");
			System.out.println("\te:\tEnd");
			System.out.print(">");
			try {
				cmd=buffer.readLine();
			} catch ( IOException ex ) {
				ex.printStackTrace();
			}
			int device=0;
			try {
				device = Integer.parseInt(cmd);
			} catch ( NumberFormatException ex ) {
				device=0;
			}
			if (device >0 && device <= serviceFound.size()) {
				OOBDDongleDescriptor oobdDongleDescr = (OOBDDongleDescriptor)serviceFound.get(device-1);
				System.out.println("Flashing "+oobdDongleDescr.friendlyName);
				if (OOBDFlashHandler.switchToFlashMode(oobdDongleDescr)) {
					System.out.println("Flash mode successfully reached!");
					if (OOBDFileHandler.OpenPreferedFirmwareInputStream()){
						YModem1K myYModem=new YModem1K();
						if (myYModem.ymodemtransfer(OOBDFlashHandler.getInputStream(), OOBDFlashHandler.getOutputStream(), OOBDFileHandler.getPreferedFirmwareInputStream(),OOBDFileHandler.getPreferedFirmwareName(), OOBDFileHandler.getPreferedFirmwareSize())){
							System.out.println("Dongle successfully Flashed!");
						}
						OOBDFileHandler.ClosePreferedFirmwareInputStream();
					}
				} else {
					System.out.println("Flash mode NOT reached!");
				}
				OOBDFlashHandler.close();
			}
		}
	}


}

