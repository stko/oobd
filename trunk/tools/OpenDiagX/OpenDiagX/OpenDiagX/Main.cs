using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Xml;
using System.IO;
using System.Xml.XPath;
using System.Xml.Xsl;

namespace OpenDiagX
{
    public partial class Main : Form
    {
        XmlNode root;
        XmlDocument xmlDocOut;

        public Main()
        {
            InitializeComponent();
        }

        private void fileButton_Click(object sender, EventArgs e)
        {
            openFileDialog.FileName = fileNameTextBox.Text;
            if (openFileDialog.ShowDialog() == DialogResult.OK)
            {
                fileNameTextBox.Text = openFileDialog.FileName;
            }
        }

        private void startButton_Click(object sender, EventArgs e)
        {
            //XPathDocument myXPathDoc = new XPathDocument(fileNameTextBox.Text);


            XslCompiledTransform myXslTrans = new XslCompiledTransform();
            //XsltSettings settings = new XsltSettings(true, false);
            //XmlUrlResolver resolver = new XmlUrlResolver();
            //transformer.Load(xsl, settings, resolver);
            myXslTrans.Load("input.xslt");

            XmlDocument xmlDoc = new XmlDocument();
            // Create a memory-based stream. 
            //byte[] storage = new byte[255];
            MemoryStream memstrm = new MemoryStream();

            // Wrap memstrm in a reader and a writer. 
            StreamWriter memwtr = new StreamWriter(memstrm);
            StreamReader memrdr = new StreamReader(memstrm);
            XmlTextWriter myWriter = new XmlTextWriter(memstrm, null);

            //Create a stream for output
            //using (XmlWriter xmlWriter =
            using (XmlWriter xmlWriter =
            xmlDoc.CreateNavigator().AppendChild())
            {
                myXslTrans.Transform(fileNameTextBox.Text, xmlWriter);
            }
            xmlDocOut = openFile("test.sxml");
            // Create XPathNavigator object by calling CreateNavigator of XmlDocument
            XPathNavigator nav = xmlDoc.CreateNavigator();


            //Move to root node
            nav.MoveToRoot();
            string name = nav.Name;
            textBox.Text += "Root node info:\r\n ";
            textBox.Text += "Base URI" + nav.BaseURI.ToString() + "\r\n";
            //root.AppendChild(xmlDoc.CreateElement("name").AppendChild(newTextNode("name",nav.Name.ToString())));
            //root.AppendChild(xmlDoc.CreateElement("name").AppendChild(xmlDoc.CreateTextNode( nav.Name.ToString())));
            //XmlText newTextNode=xmlDoc.CreateTextNode(nav.Name.ToString());
            /*
            XmlText newTextNode = xmlDocOut.CreateTextNode("blabla");
            XmlElement newElement = xmlDocOut.CreateElement("name");
            newElement.AppendChild(newTextNode);
            root.AppendChild(newElement);
            addTextnode(root, "name", "blabla");
            */
            

            textBox.Text += "Name:" + nav.Name.ToString() + "\r\n";
            textBox.Text += "Node Type: " + nav.NodeType.ToString() + "\r\n";
            //textBox.Text += "\r\nNode Value: " + nav.Value.ToString();

            if (nav.HasChildren)
            {
                nav.MoveToFirstChild();
                textBox.Text += "Child node info: " + "\r\n";
                textBox.Text += "Base URI" + nav.BaseURI.ToString() + "\r\n";
                textBox.Text += "Name:" + nav.Name.ToString() + "\r\n";
                textBox.Text += "Node Type: " + nav.NodeType.ToString() + "\r\n";
                //textBox.Text += "\r\nNode Value: " + nav.Value.ToString();

                // Security Bytes
                XPathNodeIterator iterator = nav.Select("/MDX/ECU_DATA/SECURITY_DATA/FIXED_BYTES/FIXED_BYTE");
                String SecCode = "";
                while (iterator.MoveNext())
                {
                    textBox.Text += (iterator.Current.Value.ToString().Substring(2, 2));
                    SecCode += (iterator.Current.Value.ToString().Substring(2, 2));

                }
                if (!SecCode.Equals(""))
                {
                    addTextnode(root, "SecCode", SecCode);
                }
                //Module Address
                textBox.Text += getpath(nav, "PROTOCOL/PHYSICAL_AND_LINK_LAYER/PHYSICAL_ADDRESS") + "\r\n";
                addTextnode(root, "PhysAdress", getpath(nav, "PROTOCOL/PHYSICAL_AND_LINK_LAYER/PHYSICAL_ADDRESS"));
                addTextnode(root, "PhysAdressShort", getpath(nav, "PROTOCOL/PHYSICAL_AND_LINK_LAYER/PHYSICAL_ADDRESS").Substring(4,2));
                //Module Short Name
                textBox.Text += getpath(nav, "ADMINISTRATION/SHORTNAME") + "\r\n";
                addTextnode(root, "ShortName", getpath(nav, "ADMINISTRATION/SHORTNAME"));
                //Module Name
                textBox.Text += getpath(nav, "ADMINISTRATION/ECU_NAME") + "\r\n";
                addTextnode(root, "Name", getpath(nav, "ADMINISTRATION/ECU_NAME"));
                //SSDS Part Number
                textBox.Text += getpath(nav, "ADMINISTRATION/SSDS_INFORMATION/SSDS_PART_NUMBER") + "\r\n";
                addTextnode(root, "SSDSPartNo", getpath(nav, "ADMINISTRATION/SSDS_INFORMATION/SSDS_PART_NUMBER"));

                // es fehlt: "PROTOCOL/APPLICATION_LAYER/SECURITY_LEVELS_SUPPORTED/SECURITY_LEVEL"
                // es fehlt: "ECU_DATA/DATA_IDENTIFIERS/DID...."
                // Security Bytes
                iterator = nav.Select("/MDX/ECU_DATA/DATA_IDENTIFIERS/DID");
                while (iterator.MoveNext())
                {
                    String dataType = getpath(iterator.Current, "DID_TYPE");
                    if (dataType.Equals("bitmapped")) handleBitmap(iterator);

                }


            }
            //closeFile(xmlDoc, "test.sxml");


            //Perform the actual transformation
            myXslTrans.Load("output.xslt");

            //xmlDocOut.WriteContentTo(myWriter);
            myXslTrans.Transform(xmlDocOut, myWriter);
            myWriter.Flush();
            memstrm.Seek(0, SeekOrigin.Begin);
            textBox.Text += memrdr.ReadToEnd();


        }
        private String getpath(XPathNavigator nav, String path)
        {
            XPathNodeIterator iterator = (XPathNodeIterator)nav.Evaluate(path + "/text()");
            iterator.MoveNext();
            return iterator.Current.Value.ToString();
        }

        private void fileNameTextBox_TextChanged(object sender, EventArgs e)
        {
            startButton.Enabled = File.Exists(fileNameTextBox.Text);
        }
        private void handleBitmap(XPathNodeIterator iterator)
        {
            String HighPid= getpath(iterator.Current, "NUMBER").Substring(2, 2);
            String LowPid= getpath(iterator.Current, "NUMBER").Substring(4, 2);

            textBox.Text += "HiPID:" + getpath(iterator.Current, "NUMBER").Substring(2, 2) +
        " LoPID:" + getpath(iterator.Current, "NUMBER").Substring(4, 2) + "\r\n";
            XPathNodeIterator iterator2 = iterator.Current.Select("SUB_FIELD");
            while (iterator2.MoveNext())
            {
                String Name = getpath(iterator.Current, "NAME") + " - " + getpath(iterator2.Current, "NAME");
                
                textBox.Text += "Name:" + getpath(iterator.Current, "NAME") + " - " + getpath(iterator2.Current, "NAME") + "\r\n";
                String Bit=getpath(iterator2.Current, "LEAST_SIG_BIT");
                textBox.Text += "Bit:" + getpath(iterator2.Current, "LEAST_SIG_BIT") + "\r\n";
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
                            if (getpath(iterator5.Current, "ENUM_VALUE").Equals("0x00"))
                            {
                                lowText = getpath(iterator5.Current, "DESCRIPTION");
                            }
                            if (getpath(iterator5.Current, "ENUM_VALUE").Equals("0x01"))
                            {
                                highText = getpath(iterator5.Current, "DESCRIPTION");
                            }
                        }
                        XmlElement thisDIDEntry = addSubNode(root, "BMP");
                        addTextnode(thisDIDEntry, "HighPID", HighPid);
                        addTextnode(thisDIDEntry, "LowPID", LowPid);
                        addTextnode(thisDIDEntry, "Name", Name);
                        addTextnode(thisDIDEntry, "Bit", Bit);
                        addTextnode(thisDIDEntry, "LowText", lowText);
                        addTextnode(thisDIDEntry, "HighText", highText);
                        textBox.Text += "Bit-Discription:" + lowText + "(L) " + highText + "(H)" + "\r\n";
                    }
                }
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
            

                
            /*
            XmlTextWriter xmlWriter = new XmlTextWriter(fileName, System.Text.Encoding.UTF8);
            xmlWriter.Formatting = Formatting.Indented;
            xmlWriter.WriteProcessingInstruction("xml", "version='1.0' encoding='UTF-8'");
            xmlWriter.WriteStartElement("oobdobx");
            //If WriteProcessingInstruction is used as above,
            //Do not use WriteEndElement() here
            //xmlWriter.WriteEndElement();
            //it will cause the &ltRoot></Root> to be &ltRoot />
            xmlWriter.Close();
            xmlDoc.Load(fileName);
            */
 
       //Create a document type node and  
        //add it to the document.
        /*
        XmlDocumentType doctype;
        doctype = xmlDoc.CreateDocumentType("book", null, null, "<!ELEMENT book ANY>");
        xmlDoc.AppendChild(doctype);
        */
    //Create the root element and 
    //add it to the document.
            root = xmlDoc.CreateElement("oobdobx");
            
    xmlDoc.AppendChild(root);


            //root = xmlDoc.DocumentElement;
            /*
            bitmapNode = xmlDoc.CreateElement("bitmap");
            root.AppendChild(bitmapNode);
            */

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
