using System;
using System.Collections.Generic;
using System.Text;
using System.IO;

namespace org.oobd.tools.OpenDiagX
{
    class Program
    {
        static void Main(string[] args)
        {
            if (args.Length == 4)
            {
                OpenDiagXCore odxTransformer = new OpenDiagXCore();
                StreamReader inputStream = new StreamReader(args[2]);
                StreamWriter outStream = new StreamWriter(args[3]);
                
                outStream.Write(odxTransformer.transform(args[0], args[1], inputStream));
                outStream.Close();
            }
            else
            {
                TextWriter errorWriter = Console.Error;
                errorWriter.WriteLine("OpenDiagXCL - transforms ODX files (a part of the OOBD tool set (www.oobd.org)");
                errorWriter.WriteLine("Usage: "+System.IO.Path.GetFileName(System.Diagnostics.Process.GetCurrentProcess().MainModule.FileName)+" xslt-Inputfile xslt-outputfile inputfile outputfile");
            }
        }
    }
}
