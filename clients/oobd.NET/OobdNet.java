import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.oobd.core.Core;
import org.oobd.core.IFsystem;
import org.oobd.core.OOBDConstants;
import org.oobd.core.Settings;
import org.oobd.core.port.ComPort_Win;
import org.oobd.core.support.Onion;
 
public class OobdNet implements IFsystem, OOBDConstants
{
	static String settings;
	
	public static void main(String[] args)
	{
		System.out.println ("Hello Mono World");
		settings=args[0];
		System.out.println  ("Settings file"+settings);
		try{
			Core core = new Core(new OobdNet(),"Core");
		 } catch (Settings.IllegalSettingsException ex) {
		    Logger.getLogger(OobdNet.class.getName()).log(Level.SEVERE, "Illegal preferences", ex);
		}
	}

    @Override
    public String getSystemDefaultDirectory(boolean privateDir, String fileName) {
        if (privateDir) {
            return System.getProperty("user.home") + "/" + fileName;
        } else {
            File myFile = new File(fileName);
            if (myFile.exists()) {
                return myFile.getAbsolutePath();
            } else {
                myFile = new File(Settings.getString(OOBDConstants.PropName_ScriptDir, "") + "/" + fileName);
                if (myFile.exists()) {
                    return myFile.getAbsolutePath();
                }
                return fileName;
            }

        }

    }

    @Override
    public Object supplyHardwareHandle(Onion typ) {

        try {
            return new ComPort_Win();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void openXCVehicleData(Onion onion) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String doFileSelector(String path, final String extension, String message, boolean save) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
	public String loadPreferences(){
            try {
                 byte[] encoded = Files.readAllBytes(Paths.get(settings));
                System.out.println("Preferences String:\n"+new String(encoded, "UTF8")+"\n");
                return new String(encoded, "UTF8");
            } catch (IOException ex) {
                return null;
            }
	}


	public boolean savePreferences(String json){
		return true;
	}





}
