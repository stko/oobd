/*
 * Define the YModem protocol Constant
 * 
 */

package skdsswing.transfer;

/**
 *
 * @author OOBD.org
 */
public final class TransferSpecification {
    public static final byte SOH=0x01;
    public static final byte STX=0x02;
    public static final byte EOT=0x04;
    public static final byte ACK=0x06;
    public static final byte NAK=0x15;
    public static final byte CAN=0x18;
    public static final byte G=0x47;
    public static final byte C=0x43;
    public static final int waitForBootloaderTimeout = 500;
    public static final String FirmwareLevel ="p 0 0";
    public static final String CupReset ="p 99 2";
    public static final String BootloaderLetter = "f";



}
