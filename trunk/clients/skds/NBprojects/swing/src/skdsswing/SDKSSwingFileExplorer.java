/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * A Class to show the file explorer on the java
 */

package skdsswing;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import skdsswing.transfer.InitPacket;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import skdsswing.transfer.FileSpecification;

/**
 *
 * @author OOBD
 */
public class SDKSSwingFileExplorer extends javax.swing.JDialog{
    private JFileChooser chooser;
    private static String fileName;
    private static String filePath;
    private static File choosedFile;
    private static boolean openSelected = false;
    private FileSpecification fileSpec;
    private FileInputStream input;
    private SKDSSwingUpload swingUpload;
    private static int fileSize;
    public static boolean getOpenSelected(){
        return openSelected;
    }
    /*
     * to show that the frame i sopen or not
     */
    public static void setOpenSelected(boolean value){
        openSelected = value;
    }
    /*
     * get the selecte file name
     */

    public static  String getFileName(){
        return fileName;
    }
    /*
     * get the selecte file path
     */

    public static String getFilePath(){
        return filePath;
    }
    /*
     * get the selecte file
     */
    public static File getChoosedFile(){
        return choosedFile;
    }
    public static String getFileSize(){
        return Integer.toString(fileSize);
    }
    public SDKSSwingFileExplorer(){
        
        
    }

    public SDKSSwingFileExplorer(java.awt.Frame parent,boolean modal){
        super(parent,modal);
        chooser = new JFileChooser("File Explorer");
        chooser.setApproveButtonText("Open");
        chooser.setApproveButtonMnemonic('O');
        chooser.setDialogTitle("File Explorer");
        FileFilter filter = new FileNameExtensionFilter("Bin File", "bin");
        chooser.setMultiSelectionEnabled(false);
        chooser.addChoosableFileFilter(filter);
        chooser.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e) {
          System.out.println("Action");
            }
        });
       int status = chooser.showDialog(parent, "Open");
       if(status==JFileChooser.APPROVE_OPTION){
         fileName = chooser.getSelectedFile().getName();
         filePath = chooser.getSelectedFile().getPath();

         choosedFile = chooser.getSelectedFile();
            try {


                input = new FileInputStream(choosedFile);
                fileSize =input.available()/1024;

            } catch (FileNotFoundException ex) {
                Logger.getLogger(SDKSSwingFileExplorer.class.getName()).log(Level.SEVERE, null, ex);
            }catch(IOException ex){};

       
         fileSpec = new FileSpecification(fileName,input);
          
         openSelected = true;
        }
       else if(status ==JFileChooser.CANCEL_OPTION){
           openSelected = true;
       }
       
    }


     

   

}
