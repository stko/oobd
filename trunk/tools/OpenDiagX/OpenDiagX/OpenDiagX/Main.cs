using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
//using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Xml;
using System.IO;
using System.Xml.XPath;
using System.Xml.Xsl;


namespace org.oobd.tools.OpenDiagX
{
    public partial class Main : Form
    {

        public Main()
        {
           InitializeComponent();
            fillCombo(inComboBox, "input");
            fillCombo(outComboBox, "output");
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
            OpenDiagXCore odxTransformer = new OpenDiagXCore();
           String inXSLTFileName=getConfigDirPath("input") + inComboBox.SelectedItem.ToString() + ".xslt";
            String outXSLTFileName=getConfigDirPath("output") + outComboBox.SelectedItem.ToString() + ".xslt";
            StreamReader inputStream =new StreamReader(fileNameTextBox.Text);

//            textBox.Text = memrdr.ReadToEnd();

            //seperate the result in single lines to add it to the textbox
            String[] lines = odxTransformer.transform(inXSLTFileName, outXSLTFileName, inputStream).Split(new char[] { '\n' });
            foreach (string line in lines)
            {
                textBox.Text += line + "\r\n"; ;
            }


        }

        private void fileNameTextBox_TextChanged(object sender, EventArgs e)
        {
            startButton.Enabled = File.Exists(fileNameTextBox.Text);
        }


        private String getConfigDirPath(String subdir)
        {
            return Path.GetDirectoryName(Application.ExecutablePath) + "\\" + subdir + "\\";
        }
        private void fillCombo(ComboBox cbox, String dir)
        {
            string appPath = Path.GetDirectoryName(Application.ExecutablePath);
            try
            {
                DirectoryInfo MyRoot = new DirectoryInfo(getConfigDirPath(dir));
                FileInfo[] MyFiles = MyRoot.GetFiles("*.xslt");
                foreach (FileInfo F in MyFiles)
                {
                    Console.WriteLine(F.Name);
                    cbox.Items.Add(F.Name.Remove(F.Name.LastIndexOf(".xslt")));
                }
                cbox.SelectedIndex = 0;
            }
            catch (Exception ex)
            {
            }
        }
        private void saveAsButton_Click(object sender, EventArgs e)
        {
            if (textBox.Text.Length > 0)
            {
                if (saveFileDialog.ShowDialog() == DialogResult.OK)
                {
                    StreamWriter writer = new StreamWriter(saveFileDialog.FileName.ToString());
                    writer.Write(textBox.Text);
                    writer.Close();

                }
            }
        }
    }
}
