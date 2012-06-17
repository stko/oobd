/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ymodemguijava;
import java.io.FileNotFoundException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 * @author maziar
 */
public class YModemFileExplorer extends javax.swing.JDialog {
    private JFileChooser chooser;
    private static String fileName;
    private static String filePath;
    private static File choosedFile;
    private static boolean openSelected = false;
    private FileInputStream input;
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
     public YModemFileExplorer(java.awt.Frame parent,boolean modal){
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
                

            } catch (FileNotFoundException ex) {

            }catch(IOException ex){};




         openSelected = true;
        }
       else if(status ==JFileChooser.CANCEL_OPTION){
           openSelected = true;
       }

    }
    
}
