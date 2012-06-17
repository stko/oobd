/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ymodem.transfer;

/**
 *
 * @author maziar
 */
/**!
 * Describe the YModem Parametr wich is been used in this Programm
 * @author OOBD.org
 * @version 1.0
 */
public final class TransferSpecification {
    public static final int poly = 0x1021;
    public static final int initValueCRCModem = 0x0;
    public static final byte SOH=0x01;
    public static final byte STX=0x02;
    public static final byte EOT=0x04;
    public static final byte ACK=0x06;
    public static final byte NAK=0x15;
    public static final byte CAN=0x18;
    public static final byte G=0x47;
    public static final byte C=0x43;
    public static final byte CPMEOF = 26; //0x1a
    public static final int waitForBootloaderTimeout = 500;
    public static final String DMX_Bluetooth_init_Mode = "1";
    public static final String DMX_RESTART="3";
    public static final String FirmwareLevel ="p 0 0";
    public static final String BootloaderLetter = "f";
    public static final String DXM_Bluetooth_reset="p 99 2\r\n";
    public static final String DMX_Bluetooth_readFirmware ="p 0 0\r\n";
    public static final String BootloaderLetter_help="h";
    public static final int sizeSectorZero = 128;  //<soh><00><ff><128 data><crchigh><crclow>
    public static final int sizeSectorNonZero = 1024;
    public static final int MAXERRORS = 90;
    public static final long MAXSENDTIMEOUT=300;
    public static final byte[] END = new byte[]{0x04,0x04,0x04,0x04,0x04,0x04};

}
