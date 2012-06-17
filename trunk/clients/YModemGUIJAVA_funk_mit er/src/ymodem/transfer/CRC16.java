/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ymodem.transfer;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/*
 * RReads in a sequence of bytes and prints out its 16 bit
 *  Cylcic Redundancy Check (CRC-CCIIT 0xFFFF).
 *
 *  1 + x + x^5 + x^12 + x^16 is irreducible polynomial. 0x1021
 */
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
public class CRC16 {

     private int intCrcLow;
    private int intCrcHigh; // get the
    private int initValue=TransferSpecification.initValueCRCModem;  //0x0
   /**!
     * Get the low CRC-Bit
     * @return low CRC-Bit
     */
    public int getCRCLow(){
        return intCrcLow;
    }
    /**! get the high crc bit
     *
     * @return HIgh CRC-Bit
     */
    public int getCRCHigh(){
        return intCrcHigh;
    }
    /**! give the calculate crc
     * Gives the Calculate the CRC
     * @return  CRC as integer
     */
    public int getCRC(){
        return initValue;
    }
    /**!
     * Reset the initValue before claculate the crc
     */
    public void resetInitValue(){
        initValue = TransferSpecification.initValueCRCModem;  //0x0
    }
    /**!
     * Calculate the CRC from a Byte array
     * @param value The byte value which has to ben calculate
     */
     public void CRCCalculate(byte[] value){
        for (byte b : value) {
			for (int i = 0; i < 8; i++) {
				boolean bit = ((b   >> (7-i) & 1) == 1);
				boolean c15 = ((initValue >> 15    & 1) == 1);
				initValue <<= 1;
				if (c15 ^ bit)
                        initValue ^= TransferSpecification.poly;
               }
			}
        initValue &= 0xffff;
        intCrcHigh= (initValue>>8)& 0x00ff;
        //intCrcHigh= (initValue>>8);
        intCrcLow = initValue&0xff;

        /*
        System.out.println("CRC16 = " +Integer.toHexString(initValue));
        System.out.println("Highbit   " + intCrcHigh);
        System.out.println("Lowbit   " + intCrcLow);
		*/

     }

}

