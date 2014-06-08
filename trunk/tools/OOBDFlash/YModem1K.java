/*
 * To change this template, choose Tools | Templates
 * and open the template lastInByte the editor.
 */

//package ymodem.transfer;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.Thread;
/**
 *
 * @author maziar
 */

/**!
ymodem protocoll YMODEM 1K

SENDER                                  RECEIVER
							"sb -k foo.*<CR>"
"sending in batch mode etc."
							C (command:rb)
SOH 00 FF foo.c NUL "fileLength" NUL[xxx] CRC CRC
							ACK
							C
STX 02 FD Data[1024] CRC CRC
							ACK
SOH 03 FC Data[128] CRC CRC
							ACK
SOH 04 FB Data[100] CPMEOF[28] CRC CRC
							ACK
EOT
							NAK
EOT
							ACK
							C
SOH 00 FF NUL[128] CRC CRC
							ACK
* @author OOBD.org
* @version 1.0
*/
public class YModem1K {
	private boolean isFirstSector=true;
	static boolean isEnd = false;
	static int counter =0;
	private OutputStream outputStream;
	private InputStream inputStream;
	private InputStream fileInputStream;
	private static String feedback;
	private byte lastInByte;
	byte blockNumber = 1;
	public static boolean  read= false;
	private CRC16 crc = new CRC16();

	public static String getFeedback() {
		return feedback;
	}


	private void sendBytes(byte[] data) {
		try {
			outputStream.write(data);
		} catch(IOException ex) {
			System.err.println("Send error");
		}
	}


	public boolean readByte(Integer timeout) {
		long starttime = System.currentTimeMillis();
		lastInByte=0;
		try {
			while(inputStream.available() < 1) {
				long time = System.currentTimeMillis();
				if((time - starttime) > timeout) {
					return false;
				}
				try {
					Thread.sleep(10); //sleep for 100ms
				} catch (InterruptedException e) {
				}
			}
			int readInt=inputStream.read();
			if (readInt >= 0) {
				lastInByte=(byte)readInt;
				System.err.println("Char recv.: 0x" + Integer.toHexString(lastInByte));
				return true;
			} else {
				lastInByte=(byte)0;
				return false;
			}
		} catch(IOException ex) {
			return false;
		}
	}


	public void flush() {
		try {
			while(inputStream.available() >0) {
				inputStream.read();
			}
		} catch(IOException ex) {
		}
	}


	private boolean waitForReply(Integer timeout, byte expectedReply) {

		long starttime = System.currentTimeMillis();
		readByte(10);
		while(expectedReply!=lastInByte) {
			long time = System.currentTimeMillis();
			if((time - starttime) > timeout) {
				System.err.println("WaitForReply ended with timeout");
				return false;
			}
			readByte(10);
		}
		System.err.println("WaitForReplay ended"+new Boolean(expectedReply==lastInByte).toString());
		return expectedReply==lastInByte;
	}


	public boolean ymodemtransfer(InputStream inputStream, OutputStream outputStream, InputStream fileInputStream,String fileName, long fileLength) {
		this.inputStream=inputStream;
		this.outputStream=outputStream;
		this.fileInputStream = fileInputStream;
		boolean retvalue=true;
		int actState=Constants.STATE_FIRSTSECTOR;
		while (actState!=Constants.STATE_END) {
			if (actState==Constants.STATE_FIRSTSECTOR) actState= SendFileNameSector(fileName,fileLength);
			if (actState==Constants.STATE_NEXTSECTOR) actState= sendSector();
			if (actState==Constants.STATE_EOT1) {
				flush();
				System.err.println("SEND EOT 1");
				sendBytes(new byte[] {Constants.EOT});
				if (readByte(1000) && lastInByte==Constants.ACK) {
					actState=Constants.STATE_LASTSECTOR;
				} else {
					if (lastInByte==Constants.NAK) {
						actState=Constants.STATE_EOT2;
					} else {
						actState=Constants.STATE_ABORT;
					}
				}
			}
			if (actState==Constants.STATE_EOT2) {
				flush();
				System.err.println("SEND EOT 2");
				sendBytes(new byte[] {Constants.EOT});
				if (readByte(1000) && lastInByte==Constants.ACK) {
					actState=Constants.STATE_LASTSECTOR;
				} else {
					actState=Constants.STATE_ABORT;
				}
			}
			if (actState==Constants.STATE_LASTSECTOR) actState= SendFileNameSector(null,0); // signals EOT
			if (actState==Constants.STATE_ABORT) {
				System.err.println("Aborted");
				retvalue=false;
				actState=Constants.STATE_END;
			}
		}
		return retvalue;
	}


	private int sendSector() {
		System.err.println("Start normal sectors");
		byte[] sector = new byte[Constants.sizeSectorNonZero];
		counter=Constants.MAXERRORS;
		int bytesRead=0;
		byte[] packet= new byte[1029];
		byte[] innerData;
		byte[] header = new byte[3];
		while (bytesRead !=-1 && counter >0) {
			counter=Constants.MAXERRORS;
			try {
				bytesRead = fileInputStream.read(sector);
			} catch(IOException ex) {
				System.err.println("Error while reading filestream");
				//fileInputStream.close();
				return Constants.STATE_ABORT;
			}
			if(bytesRead > 0) {
				System.err.println("Preparing block "+Integer.toString(blockNumber));
				lastInByte=Constants.EMPTY;
				if(bytesRead < Constants.sizeSectorNonZero) {
					while (bytesRead < Constants.sizeSectorNonZero) {
						sector[bytesRead]= Constants.CPMEOF;
						bytesRead++;
					}
				}
				header[0] = Constants.STX;
				header[1] = (byte)blockNumber;
				header[2] = (byte)(255-blockNumber);
				innerData = new byte[Constants.sizeSectorNonZero];
				System.arraycopy(sector, 0, innerData, 0, sector.length);
				crc.resetInitValue();
				crc.CRCCalculate(innerData);
				System.arraycopy(innerData, 0, packet, 3, innerData.length);
				System.arraycopy(header, 0, packet, 0, header.length);
				packet[1027] = crc.getCRCHighByte();
				packet[1028] = crc.getCRCLowByte();

				while(lastInByte!=Constants.ACK && counter >0) {
					System.err.println("Sending block "+Integer.toString(blockNumber));
					flush();
					sendBytes(packet);
					readByte(1000);
					if (lastInByte!=Constants.ACK) {
						if (lastInByte==Constants.CAN) {
							try {
								fileInputStream.close();
							} catch(IOException ex) {
							}
							return Constants.STATE_ABORT;
						} else {
							System.err.println("Block "+Integer.toString(blockNumber)+" failed, try again");
							counter--;
						}
					} else {
						System.err.println("block "+Integer.toString(blockNumber)+" done, do next");
					}
				}
				blockNumber = blockNumber > 126 ? (byte)-128 : (byte)(blockNumber+1);
			}
		}
		try {
			fileInputStream.close();
		} catch(IOException ex) {
		}

		if (lastInByte==Constants.ACK) {
			return Constants.STATE_EOT1;
		} else {
			return Constants.STATE_ABORT;
		}
	}


	private int SendFileNameSector(String fileName, long fileSize) {

		lastInByte=Constants.EMPTY;
		counter=Constants.MAXERRORS;
		flush();
		while (lastInByte!=Constants.C && counter >0) {
			waitForReply(10000,Constants.C);
			System.err.println("Wait for C");
			counter--;
		}
		if (lastInByte!=Constants.C) {
			return Constants.STATE_ABORT;
		}
		System.err.println("Initial \"C\" received");
		byte[] packet = new byte[133];
		byte[] innerData;
		byte[] header = new byte[3];
		header[0] = Constants.SOH;
		header[1] = 0;
		header[2] = (byte)255;
		crc.resetInitValue();
		innerData = new byte[Constants.sizeSectorZero];  //128
		// copy the file name, if not empty
		// if empty, then send it empty as indication of transfer end
		if (fileName!=null && fileName.length()>0) {
			System.arraycopy(fileName.toLowerCase().getBytes(),0, innerData, 0,fileName.toLowerCase().getBytes().length);
			// copy the file size
			System.arraycopy(Long.toString(fileSize).getBytes(),0, innerData, fileName.toLowerCase().getBytes().length+1, Long.toString(fileSize).getBytes().length);
		} else {
			System.err.println("Send empty Sector");
		}
		System.arraycopy(innerData, 0, packet, 3, innerData.length);
		System.arraycopy(header, 0, packet, 0, header.length);
		// Calculate the CRC
		crc.CRCCalculate(innerData);
		packet[131]= crc.getCRCHighByte();
		packet[132]= crc.getCRCLowByte();

		lastInByte=Constants.EMPTY;
		counter=Constants.MAXERRORS;
		while (lastInByte!=Constants.ACK && counter >0) {
			flush();
			sendBytes(packet);
			readByte(1000); //waiting for ACK
			counter--;
		}
		readByte(1000); //waiting for C
		if (lastInByte==Constants.C) {
			if (fileName!=null && fileName.length()>0) {
				System.err.println("First Sector sended successfully sended");
				return Constants.STATE_NEXTSECTOR;
			} else {
				System.err.println("Last (empty) Sector sended successfully sended");
				return Constants.STATE_END;
			}
		} else {
			if (lastInByte==Constants.CAN) {
				System.err.println("Canceled by remote");
				return Constants.STATE_EOT1;
			} else {
				System.err.println("Error during First Sector");
				return Constants.STATE_ABORT;
			}
		}
	}

	public static void main (String[] args) {
		FileInputStream fileInputStream=null;
		File file=null;
		if (args.length!=1) {
			System.err.println("Usage: ymodem filename");
			System.exit(0);
		}
		try {
			fileInputStream= new FileInputStream(args[0]);
			file= new File(args[0]);

		} catch (IOException ex) {
			System.err.println("cant read input file");
			System.exit(1);
		}
		YModem1K myYModem=new YModem1K();
		myYModem.ymodemtransfer(System.in, System.out, fileInputStream,file.getName(), file.length());
	}

}





final class Constants {
	public static final int poly = 0x1021;
	public static final int initValueCRCModem = 0x0;
	public static final byte EMPTY=0x00;
	public static final byte SOH=0x01;
	public static final byte STX=0x02;
	public static final byte EOT=0x04;
	public static final byte ACK=0x06;
	public static final byte NAK=0x15;
	public static final byte CAN=0x18;
	public static final byte G=0x47;
	public static final byte C=0x43;
	public static final byte CPMEOF = 26; //0x1a
	public static final int sizeSectorZero = 128;  //<soh><00><ff><128 data><crchigh><crclow>
	public static final int sizeSectorNonZero = 1024;
	public static final int MAXERRORS = 5;
	public static final int STATE_FIRSTSECTOR=0;
	public static final int STATE_NEXTSECTOR=1;
	public static final int STATE_EOT1=2;
	public static final int STATE_EOT2=3;
	public static final int STATE_LASTSECTOR=4;
	public static final int STATE_ABORT=99;
	public static final int STATE_END=100;

}



/**
 *
 * @author maziar
 */
/**
 * RReads in a sequence of bytes and prints out its 16 bit
 * Cylcic Redundancy Check
 * 1 + x + x^5 + x^12 + x^16 is irreducible polynomial.
 * For CRC-CCITT  polynomial = 0x1021, Initial Value = 0xffff;
 * For CRC-16  polynomial = 0xa001, Initial Value = 0xffff;
 * For CRC-XModem  polynomial = 0x1021, Initial Value = 0x0; is used here
 * @author OOBD.org
 * @version 1.0
 */
class CRC16 {

	private int intCrcLow;
	private int intCrcHigh; // get the
	private int initValue=Constants.initValueCRCModem;  //0x0
	/**!
	  * Get the low CRC-Bit
	  * @return low CRC-Bit
	  */
	public byte getCRCLowByte() {
		return (byte)intCrcLow;
	}
	/**! get the high crc bit
	 *
	 * @return HIgh CRC-Bit
	 */
	public byte getCRCHighByte() {
		return (byte)intCrcHigh;
	}
	/**! give the calculate crc
	 * Gives the Calculate the CRC
	 * @return  CRC as integer
	 */
	public int getCRC() {
		return initValue;
	}
	/**!
	 * Reset the initValue before claculate the crc
	 */
	public void resetInitValue() {
		initValue = Constants.initValueCRCModem;  //0x0
	}
	/**!
	 * Calculate the CRC from a Byte array
	 * @param value The byte value which has to ben calculate
	 */
	public void CRCCalculate(byte[] value) {
		for (byte b : value) {
			for (int i = 0; i < 8; i++) {
				boolean bit = ((b   >> (7-i) & 1) == 1);
				boolean c15 = ((initValue >> 15    & 1) == 1);
				initValue <<= 1;
				if (c15 ^ bit)
					initValue ^= Constants.poly;
			}
		}
		initValue &= 0xffff;
		intCrcHigh= (initValue>>8)& 0x00ff;
		//intCrcHigh= (initValue>>8);
		intCrcLow = initValue&0xff;


		/*
		System.err.println("CRC16 = " +Integer.toHexString(initValue));
		System.err.println("Highbit   " + intCrcHigh);
		System.err.println("Lowbit   " + intCrcLow);
		System.err.println("Highbit   " + Integer.toHexString(getCRCHighByte()));
		System.err.println("Lowbit   " + Integer.toHexString(getCRCLowByte()));
		*/

	}

}
