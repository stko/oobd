/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package skdsswing;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileFilter;
import java.awt.event.ActionListener;
/**
 *
 * @author OOBD
 */
public class SDKSSwingFileExplorer extends javax.swing.JDialog{
    private JFileChooser chooser;
    private static String fileName;
    private static String filePath;
    private static File choosedFile;
    private static boolean openSelected = false;;

    public static boolean getOpenSelected(){
        return openSelected;
    }
    public static void setOpenSelected(boolean value){
        openSelected = value;
    }

    public static  String getFileName(){
        return fileName;
    }

    public static String getFilePath(){
        return filePath;
    }

    public static File getChoosedFile(){
        return choosedFile;
    }
    public SDKSSwingFileExplorer(){
        
    }
    public SDKSSwingFileExplorer(java.awt.Frame parent,boolean modal){
        super(parent,modal);
        chooser = new JFileChooser("File Explorer");

        FileFilter filter = new FileNameExtensionFilter("Hex File", "hex");
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
         openSelected = true;
        }
       else if(status ==JFileChooser.CANCEL_OPTION){
           openSelected = true;
       }
       
    }

   

}
