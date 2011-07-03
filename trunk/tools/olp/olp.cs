/*


As http://lua-users.org/lists/lua-l/2000-08/msg00034.html describes the problem allready most
perfectly, this is a try of a solution 

olp scans the input file for dofile() statements and recursively write all found files to stdout.

*/




using System;
using System.IO;
using System.Text;
using System.Text.RegularExpressions;


namespace org.oobd.tools.olp
{
	class olp
	{
		static bool listMode = false;

		static void Main(string[] args)
		{	
		  	Console.OutputEncoding=System.Text.Encoding.UTF8;
			if (args.Length==1){
				System.Environment.Exit (doFile(args[0],args[0],0));
			}
			if (args.Length == 2 && args[0].Equals("-l")){
				listMode = true;			
				System.Environment.Exit (doFile(args[1],args[1],0));
			}  
			Console.WriteLine(String.Format("Nr. of Args:{0}",args.Length));
			Console.WriteLine(@"olp - the OOBD Lua preprozessor
(Rev. {0} {1})

takes a lua file as input and writes the file and recursive all contained
include files as a continous text stream to stdout

Usage: olp [-l] inputfile

Options:
-l : does not output the text, just list all contained include file names
as a single line of output. Useful for automatic makefile generation

olp is part of the OOBD toolset (www.oobd.org)","$Rev$","$Date$");
		}

		static int doFile(string incFileName,String parentFileName, int lineNr)
		{
			// create reader & open file
			try{
				//Console.Error.Write("{0} ",incFileName);
				TextReader tr = new StreamReader(incFileName,System.Text.Encoding.UTF8);
				// no exception? Fine, then we can continue..
				// save actual directory
				String myCurrDir=Directory.GetCurrentDirectory();
				//Console.Error.WriteLine("current Dir:{0}",myCurrDir);
				//get the full path of the file to read
				String fullPath=Path.GetFullPath(incFileName);
				if (listMode) Console.Write("{0} ",fullPath);
				// set Directory to file directory
				String fileDir=fullPath.Substring(0,fullPath.Length-Path.GetFileName(fullPath).Length);
				//Console.Error.WriteLine("file Dir:{0}",fileDir);
				Directory.SetCurrentDirectory(fileDir);
				// read a line of text

				String line;
				int lineCount=0;
				Regex regex_dofile=new Regex("(?i)^\\s*dofile\\(\"(?<file>.*)\"\\)");
				while( (line = tr.ReadLine()) != null) {
					lineCount++;
					bool isIncludeLine=false;
					try{
						MatchCollection myMatchCollection = regex_dofile.Matches(line);
						foreach (Match aMatch in myMatchCollection){
							if (aMatch.Length != 0){
								isIncludeLine=true;
								//Console.WriteLine("File: {0}",aMatch.Groups["file"]);
								if (doFile(aMatch.Groups["file"].Value,incFileName,lineCount)>0){
									return 1;
								}
							}
						}
					}
					catch{
						Console.Error.WriteLine("Invalid Regex");
					}
					if (!isIncludeLine && !listMode){
						Console.WriteLine(line);
					}
				}
				// close the stream
				tr.Close();
				// restore current Directory
				Directory.SetCurrentDirectory(myCurrDir);
				if (listMode && lineNr==0) Console.WriteLine(); // lineNr==0 signals the top level file
				return 0;
			}
			catch
			{
				Console.Error.WriteLine("{0} [{1}]: File not found: {2}",parentFileName,lineNr,incFileName);
				//Console.Error.WriteLine("Stacktrace: {0}",e.StackTrace);
				return 1;
			}
		}
	}
}
