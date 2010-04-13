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

            XmlDocument xmlDocOut = new XmlDocument();
            // Create a memory-based stream. 
            byte[] storage = new byte[255];
            MemoryStream memstrm = new MemoryStream();

            // Wrap memstrm in a reader and a writer. 
            StreamWriter memwtr = new StreamWriter(memstrm);
            StreamReader memrdr = new StreamReader(memstrm);
            XmlTextWriter myWriter = new XmlTextWriter(memstrm, null);

            //Create a stream for output
            //using (XmlWriter xmlWriter =
            using (XmlWriter xmlWriter =
            xmlDocOut.CreateNavigator().AppendChild())
            {
                myXslTrans.Transform(fileNameTextBox.Text, null, xmlWriter);
            }

            // Create XPathNavigator object by calling CreateNavigator of XmlDocument
            XPathNavigator nav = xmlDocOut.CreateNavigator();


            //Move to root node
            nav.MoveToRoot();
            string name = nav.Name;
            textBox.Text+= "Root node info:\r\n ";
            textBox.Text += "Base URI" + nav.BaseURI.ToString()+"\r\n";
            textBox.Text += "Name:" + nav.Name.ToString() + "\r\n";
            textBox.Text += "Node Type: " + nav.NodeType.ToString() + "\r\n";
            //textBox.Text += "\r\nNode Value: " + nav.Value.ToString();

            if (nav.HasChildren)
            {
                nav.MoveToFirstChild();
                textBox.Text += "Child node info: " + "\r\n";
                textBox.Text += "Base URI" + nav.BaseURI.ToString() + "\r\n";
                textBox.Text += "Name:" + nav.Name.ToString() + "\r\n";
                textBox.Text += "Node Type: " + nav.NodeType.ToString()+"\r\n";
                //textBox.Text += "\r\nNode Value: " + nav.Value.ToString();
                
                // Security Bytes
                XPathNodeIterator iterator = nav.Select("/MDX/ECU_DATA/SECURITY_DATA/FIXED_BYTES/FIXED_BYTE");
                while (iterator.MoveNext())
                {
                    textBox.Text += (iterator.Current.Value.ToString().Substring(2,2));
                }
                //Module Address
                textBox.Text += getpath(nav, "PROTOCOL/PHYSICAL_AND_LINK_LAYER/PHYSICAL_ADDRESS") + "\r\n";
                //Module Short Name
                textBox.Text += getpath(nav, "ADMINISTRATION/SHORTNAME") + "\r\n";
                //Module Name
                textBox.Text += getpath(nav, "ADMINISTRATION/ECU_NAME") + "\r\n";
                //SSDS Part Number
                textBox.Text += getpath(nav, "ADMINISTRATION/SSDS_INFORMATION/SSDS_PART_NUMBER") + "\r\n";

                // es fehlt: "PROTOCOL/APPLICATION_LAYER/SECURITY_LEVELS_SUPPORTED/SECURITY_LEVEL"
                // es fehlt: "ECU_DATA/DATA_IDENTIFIERS/DID...."
                // Security Bytes
                iterator = nav.Select("/MDX/ECU_DATA/DATA_IDENTIFIERS/DID");
                while (iterator.MoveNext())
                {
                    textBox.Text += "Name:" + getpath(iterator.Current, "NAME") + "\r\n";
                    textBox.Text += "Description:" + getpath(iterator.Current, "DESCRIPTION") + "\r\n";
                    textBox.Text += "HiPID:" + getpath(iterator.Current, "NUMBER").Substring(2, 2) + "\r\n";
                    textBox.Text += "LoPID:" + getpath(iterator.Current, "NUMBER").Substring(4, 2) + "\r\n";
                    XPathNodeIterator iterator2 = iterator.Current.Select("SUB_FIELD");
                    while (iterator2.MoveNext())
                    {
                        //textBox.Text +=  iterator2.Current.Value.ToString() + "\r\n";
                    }
                }
 
            
            }



            //Perform the actual transformation
            xmlDocOut.WriteContentTo(myWriter);
            myWriter.Flush();
            memstrm.Seek(0, SeekOrigin.Begin);
            //textBox.Text += memrdr.ReadToEnd();


        }
        private String getpath(XPathNavigator nav, String path)
        {
            XPathNodeIterator iterator = (XPathNodeIterator)nav.Evaluate(path+"/text()");
            iterator.MoveNext();
            return iterator.Current.Value.ToString();
        }

        private void fileNameTextBox_TextChanged(object sender, EventArgs e)
        {
            startButton.Enabled = File.Exists(fileNameTextBox.Text);
        }

    }
}
