using System;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.ComponentModel;
using System.Data;
using System.Xml;

using System.Xml.XPath;
using System.Xml.Xsl;

namespace org.oobd.tools.OpenDiagX
{
    public class OpenDiagXCore
    {
        XmlNode root;
        XmlDocument xmlDocOut;

        public String transform(String inXSLTFileName, String outXSLTFileName, StreamReader inputStream)
        {
            XslCompiledTransform myXslTrans = new XslCompiledTransform();
            myXslTrans.Load(inXSLTFileName);
            XmlDocument xmlDoc = new XmlDocument();
            MemoryStream memstrm = new MemoryStream();
            // Wrap memstrm in a reader and a writer. 
            StreamWriter memwtr = new StreamWriter(memstrm);
            StreamReader memrdr = new StreamReader(memstrm);
            XmlTextWriter myWriter = new XmlTextWriter(memstrm, null);
            XmlTextReader myReader = new XmlTextReader(inputStream);
            myReader.XmlResolver = null; /* fix error of missing referenced to DTD file */
            //Create a stream for output
            using (XmlWriter xmlWriter =
            xmlDoc.CreateNavigator().AppendChild())
            {
                myXslTrans.Transform(myReader, xmlWriter);
            }
            xmlDocOut = openFile("test.sxml");
            // Create XPathNavigator object by calling CreateNavigator of XmlDocument
            XPathNavigator nav = xmlDoc.CreateNavigator();


            //Move to root node
            nav.MoveToRoot();
            string name = nav.Name;
            if (nav.HasChildren)
            {
                //nav.MoveToFirstChild();
                // Security Bytes
                XPathNodeIterator iterator = nav.Select("/MDX/ECU_DATA/SECURITY_DATA/FIXED_BYTES/FIXED_BYTE");
                String SecCode = "";
                while (iterator.MoveNext())
                {
                    SecCode += (iterator.Current.Value.ToString().Substring(2, 2));
                }
                if (!SecCode.Equals(""))
                {
                    addTextnode(root, "SecCode", SecCode);
                }
                //Module Address
                addTextnode(root, "PhysAdress", strRight(getpath(nav, "/MDX/PROTOCOL/PHYSICAL_AND_LINK_LAYER/PHYSICAL_ADDRESS"),3));
                addTextnode(root, "RespAdress", strRight(getpath(nav, "/MDX/PROTOCOL/PHYSICAL_AND_LINK_LAYER/RESPONSE_ADDRESS"),3));
                addTextnode(root, "FuncAdress", strRight(getpath(nav, "/MDX/PROTOCOL/PHYSICAL_AND_LINK_LAYER/FUNCTIONAL_ADDRESS"),3));
                addTextnode(root, "Bus", getpath(nav, "/MDX/PROTOCOL/PHYSICAL_AND_LINK_LAYER/NAME"));
                addTextnode(root, "BusSpeed", getpath(nav, "/MDX/PROTOCOL/PHYSICAL_AND_LINK_LAYER/DATA_RATE"));
                addTextnode(root, "PhysAdressShort", strRight(getpath(nav, "/MDX/PROTOCOL/PHYSICAL_AND_LINK_LAYER/PHYSICAL_ADDRESS"), 2));
                //Module Short Name
                addTextnode(root, "ShortName", getpath(nav, "/MDX/ADMINISTRATION/SHORTNAME"));
                //Module Name
                addTextnode(root, "Name", getpath(nav, "/MDX/ADMINISTRATION/ECU_NAME"));
                //SSDS Part Number
                addTextnode(root, "SSDSPartNo", getpath(nav, "/MDX/ADMINISTRATION/SSDS_INFORMATION/SSDS_PART_NUMBER"));

                // es fehlt: "PROTOCOL/APPLICATION_LAYER/SECURITY_LEVELS_SUPPORTED/SECURITY_LEVEL"
                // es fehlt: "ECU_DATA/DATA_IDENTIFIERS/DID...."
                // Security Bytes
                iterator = nav.Select("/MDX/ECU_DATA/DATA_IDENTIFIERS/DID");
                while (iterator.MoveNext())
                {
                    String dataType = getpath(iterator.Current, "DID_TYPE");
                    //textBox.Text += "Data Type: "+dataType+"\r\n";
                   if (dataType.Equals("bitmapped")) handleBitmap(iterator);
 
                    if (dataType.Equals("packeted"))
                    {
                        String subDataType = getpath(iterator.Current, "SUB_FIELD/DATA_DEFINITION/DATA_TYPE");
                        //textBox.Text += "Sub Data Type: " + subDataType + "\r\n";
    
                        /* not implemented yet
                         * if (subDataType.Equals("bcd")) handleBcd(iterator);
                         * if (subDataType.Equals("enumerated")) handleEnumerator(iterator);
                        */
                        
                        /* not maybe not used for packeted DID_TYPE, investigation ongoing
                        if (subDataType.Equals("ascii")) handleASCII(iterator);
                        if (subDataType.Equals("unsigned")) handleUnsigned(iterator);
                        if (dataType.Equals("bitmapped")) handleBitmap(iterator);
                        */
                    }
 
                    if (dataType.Equals("single_value"))
                    {
                        String subDataType = getpath(iterator.Current, "SUB_FIELD/DATA_DEFINITION/DATA_TYPE");
                        //textBox.Text += "Sub Data Type: " + subDataType + "\r\n";
                        if (subDataType.Equals("ascii")) handleASCII(iterator);
                        if (subDataType.Equals("unsigned")) handleUnsigned(iterator);
                    }
   }
                iterator = nav.Select("/MDX/ECU_DATA/DIAGNOSTIC_TROUBLE_CODES/DTC");
                XmlElement subtree = xmlDocOut.CreateElement("DTCS");
                root.AppendChild(subtree);
                while (iterator.MoveNext())
                {
                    XmlNode thisDTC = xmlDocOut.CreateElement("DTC");
                    subtree.AppendChild(thisDTC);
                    addTextnode(thisDTC, "ID", strRight(getpath(iterator.Current, "NUMBER"), 4));
                    addTextnode(thisDTC, "DESCRIPTION", getpath(iterator.Current, "DESCRIPTION"));
                }
                iterator = nav.Select("/MDX/ECU_DATA/ROUTINE_IDENTIFIERS/ROUTINE");
                subtree = xmlDocOut.CreateElement("ROUTINES");
                root.AppendChild(subtree);
                while (iterator.MoveNext())
                {
                    XmlNode thisDTC = xmlDocOut.CreateElement("ROUTINE");
                    subtree.AppendChild(thisDTC);
                    addTextnode(thisDTC, "ID", strRight(getpath(iterator.Current, "NUMBER"), 4));
                    addTextnode(thisDTC, "DESCRIPTION", getpath(iterator.Current, "NAME"));
                    addTextnode(thisDTC, "SESSION_REFS",iterator.Current.GetAttribute("SESSION_REFS", ""));
               }
            }
            //Perform the actual transformation
            myXslTrans.Load(outXSLTFileName);

            myXslTrans.Transform(xmlDocOut, myWriter);
            myWriter.Flush();
            memstrm.Seek(0, SeekOrigin.Begin);

            //            textBox.Text = memrdr.ReadToEnd();

            //seperate the result in single lines to add it to the textbox
            return memrdr.ReadToEnd();
        }
        private String strRight(String text, int len){
            return text.Substring(text.Length-len);
        }


        private String getpath(XPathNavigator nav, String path)
        {
            if (nav.SelectSingleNode(path + "/text()") != null)
            {
                XPathNodeIterator iterator = (XPathNodeIterator)nav.Evaluate(path + "/text()");
                iterator.MoveNext();
                return iterator.Current.Value.ToString().Replace("\"","'").Trim();
            }
            else
            {
                return null;
            }
        }
        private void handleBitmap(XPathNodeIterator iterator)
        {
            String HighPid = getpath(iterator.Current, "NUMBER").Substring(2, 2);
            String LowPid = getpath(iterator.Current, "NUMBER").Substring(4, 2);
            int byteSize = Convert.ToInt32(getpath(iterator.Current, "BYTE_SIZE"));
            XmlElement thisDIDEntry = addSubNode(root, "BMP");
            addTextnode(thisDIDEntry, "HighPID", HighPid);
            addTextnode(thisDIDEntry, "LowPID", LowPid);
            addTextnode(thisDIDEntry, "Group", getpath(iterator.Current, "NAME"));

            XPathNodeIterator iterator2 = iterator.Current.Select("SUB_FIELD");
            while (iterator2.MoveNext())
            {
                String Name = getpath(iterator2.Current, "NAME");
                String Bit = getpath(iterator2.Current, "LEAST_SIG_BIT");
                XPathNodeIterator iterator3 = iterator2.Current.Select("DATA_DEFINITION");
                while (iterator3.MoveNext())
                {
                    XPathNodeIterator iterator4 = iterator3.Current.Select("ENUMERATED_PARAMETERS");
                    while (iterator4.MoveNext())
                    {
                        String lowText = "";
                        String highText = "";
                        XPathNodeIterator iterator5 = iterator4.Current.Select("ENUM_MEMBER");
                        while (iterator5.MoveNext())
                        {
                            if (getpath(iterator5.Current, "ENUM_VALUE").Equals("0x00") || getpath(iterator5.Current, "ENUM_VALUE").Equals("0")) // here a correct numeric convertion would be nessecary...
                            {
                                lowText = getpath(iterator5.Current, "DESCRIPTION");
                            }
                            if (getpath(iterator5.Current, "ENUM_VALUE").Equals("0x01") || getpath(iterator5.Current, "ENUM_VALUE").Equals("1")) // here a correct numeric convertion would be nessecary...
                            {
                                highText = getpath(iterator5.Current, "DESCRIPTION");
                            }
                        }
                        XmlElement thisBitEntry = addSubNode(thisDIDEntry, "SingleBit");
                        addTextnode(thisBitEntry, "Name", Name);
                        addTextnode(thisBitEntry, "Bit", Bit);
                        addTextnode(thisBitEntry, "ByteNr", (byteSize - 1 - Convert.ToInt16(Bit) / 8).ToString());
                        addTextnode(thisBitEntry, "BitNr", (Convert.ToInt16(Bit) % 8).ToString());
                        addTextnode(thisBitEntry, "LowText", lowText);
                        addTextnode(thisBitEntry, "HighText", highText);
                    }
                }
            }
        }
        private void handleASCII(XPathNodeIterator iterator)
        {
            String HighPid = getpath(iterator.Current, "NUMBER").Substring(2, 2);
            String LowPid = getpath(iterator.Current, "NUMBER").Substring(4, 2);
            String parentLevelName = getpath(iterator.Current, "NAME");

            XPathNodeIterator iterator2 = iterator.Current.Select("SUB_FIELD");
            while (iterator2.MoveNext())
            {
                //String Name = getpath(iterator.Current, "NAME") + " - " + getpath(iterator2.Current, "NAME");
                String Name = getpath(iterator2.Current, "NAME");
                if (Name == null)
                {
                    Name = parentLevelName;
                }
                String LBit = getpath(iterator2.Current, "LEAST_SIG_BIT");
                String MBit = getpath(iterator2.Current, "MOST_SIG_BIT");
                XmlElement thisDIDEntry = addSubNode(root, "ASCII");
                addTextnode(thisDIDEntry, "HighPID", HighPid);
                addTextnode(thisDIDEntry, "LowPID", LowPid);
                addTextnode(thisDIDEntry, "Name", Name);
                addTextnode(thisDIDEntry, "Len", ((Convert.ToInt16(MBit) + 1) / 8).ToString());
            }
        }
        private void handleUnsigned(XPathNodeIterator iterator)
        {
            String HighPid = getpath(iterator.Current, "NUMBER").Substring(2, 2);
            String LowPid = getpath(iterator.Current, "NUMBER").Substring(4, 2);
            String parentLevelName = getpath(iterator.Current, "NAME");
            XPathNodeIterator iterator2 = iterator.Current.Select("SUB_FIELD");
            while (iterator2.MoveNext())
            {
                //String Name = getpath(iterator.Current, "NAME") + " - " + getpath(iterator2.Current, "NAME");
                String Name = getpath(iterator2.Current, "NAME");
                if (Name == null)
                {
                    Name = parentLevelName;
                }
                String LBit = getpath(iterator2.Current, "LEAST_SIG_BIT");
                String MBit = getpath(iterator2.Current, "MOST_SIG_BIT");
                XmlElement thisDIDEntry = addSubNode(root, "NUM");
                addTextnode(thisDIDEntry, "HighPID", HighPid);
                addTextnode(thisDIDEntry, "LowPID", LowPid);
                addTextnode(thisDIDEntry, "Name", Name);
                addTextnode(thisDIDEntry, "Len", ((Convert.ToInt16(MBit) + 1) / 8).ToString());
                if (iterator2.Current.SelectSingleNode("DATA_DEFINITION/NUMERIC_PARAMETERS") != null)
                {
                    //addTextnode(thisDIDEntry, "Resolution", getpath(iterator2.Current, "DATA_DEFINITION/NUMERIC_PARAMETERS/RESOLUTION"));
                    try2addTextnode(thisDIDEntry, iterator2.Current, "Resolution", "DATA_DEFINITION/NUMERIC_PARAMETERS/RESOLUTION", "1");
                    //addTextnode(thisDIDEntry, "Offset", getpath(iterator2.Current, "DATA_DEFINITION/NUMERIC_PARAMETERS/OFFSET"));
                    try2addTextnode(thisDIDEntry, iterator2.Current, "Offset", "DATA_DEFINITION/NUMERIC_PARAMETERS/OFFSET", "0");
                    //addTextnode(thisDIDEntry, "Units", getpath(iterator2.Current, "DATA_DEFINITION/NUMERIC_PARAMETERS/UNITS"));
                    try2addTextnode(thisDIDEntry, iterator2.Current, "Units", "DATA_DEFINITION/NUMERIC_PARAMETERS/UNITS", "");
                }
                else
                {
                    addTextnode(thisDIDEntry, "Resolution", "1");
                    addTextnode(thisDIDEntry, "Offset", "0");
                    addTextnode(thisDIDEntry, "Units", "");
                }
            }
        }
        private Boolean try2addTextnode(XmlElement outputNode, XPathNavigator currentNode, String tagName, String path, String defaultValue)
        {
            if (currentNode.SelectSingleNode(path + "/text()") != null)
            {
                addTextnode(outputNode, tagName, getpath(currentNode, path));
                return true;
            }
            else
            {
                addTextnode(outputNode, tagName, defaultValue);
                return false;
            }
        }
        private void addTextnode(XmlNode parent, String name, String content)
        {
            XmlText newTextNode = xmlDocOut.CreateTextNode(content);
            XmlElement newElement = xmlDocOut.CreateElement(name);
            newElement.AppendChild(newTextNode);
            parent.AppendChild(newElement);
        }
        private XmlElement addSubNode(XmlNode parent, String name)
        {
            XmlElement newElement = xmlDocOut.CreateElement(name);
            parent.AppendChild(newElement);
            return newElement;
        }
        private XmlDocument openFile(String fileName)
        {
            XmlDocument xmlDoc = new XmlDocument();
            //Create the root element and 
            //add it to the document.
            root = xmlDoc.CreateElement("oobdobx");
            xmlDoc.AppendChild(root);
            return xmlDoc;
        }
        private XmlText newTextNode(String name, String value)
        {
            XmlText xmlNode = xmlDocOut.CreateTextNode(name);
            xmlNode.Value = value;
            return xmlNode;
        }
    }
}
