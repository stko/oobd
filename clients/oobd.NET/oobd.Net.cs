using System;
using System.IO;
using org.oobd.core;
using org.oobd;
using org.oobd.core.support;
using org.oobd.core.port;
 
public class HelloWorld : IFsystem, OOBDConstants
{
	static string settings;
	
	static public void Main (string[] args)
	{
		Console.WriteLine ("Hello Mono World");
		settings=args[0];
		Console.WriteLine ("Settings file"+settings);
		OOBD oobd= new OOBD(); 
		Core core = new Core(new HelloWorld(),"Core");
	}

	public String getSystemDefaultDirectory(bool privateDir, string fileName){
		if (privateDir) {
			//return System.getProperty("user.home") + "/" + fileName;
			// how csharp find it's users home??
			return  fileName;
		} else {
			if (File.Exists(fileName)){
				return Path.GetFullPath(fileName);
			} else {
				//string myFile = Settings.getString(OOBDConstants.PropName_ScriptDir, "") + "/" + fileName;
				string myFile = Settings.getString("ScriptDir", "") + "/" + fileName;
				if (File.Exists(myFile)){
					return Path.GetFullPath(myFile);
				}
				return fileName;
			}

		}
	}

	public void openXCVehicleData(Onion onion){
	}


	public String loadPreferences(){
		return File.ReadAllText(settings);
	}


	public Object supplyHardwareHandle(Onion onion){
		return new ComPort_Win();
	}

	public bool savePreferences(string json){
		return true;
	}


	public string doFileSelector(string path, string extension, string message, bool Save){
		return "";
	}



}
