import java.util.*;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.io.file.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

public class FileDialog extends MIDlet implements CommandListener
{
  private String currDirName;

  private Command view = new Command("View", Command.ITEM, 1);
  private Command back = new Command("Back", Command.BACK, 2);
  private Command exit = new Command("Exit", Command.EXIT, 3);

  private final static String UP_DIRECTORY = "..";
  private final static String MEGA_ROOT = "/";
  private final static String SEP_STR = "/";
  private final static char   SEP = '/';

  public FileDialog()
  {
    currDirName = MEGA_ROOT;
  }

  public void startApp()
  {
    boolean isAPIAvailable = false;
    if (System.getProperty(
      "microedition.io.file.FileConnection.version") != null)
    {
      isAPIAvailable = true;
      try
      {
        showCurrDir();
      }
      catch (SecurityException e)
      {}
      catch (Exception e) {}
      }
    else
    {
      StringBuffer splashText = new StringBuffer(
        getAppProperty("MIDlet-Name")).append("\n").append(
        getAppProperty("MIDlet-Vendor")).
        append(isAPIAvailable?"":"\nFileConnection API not available");
      Alert splashScreen = new Alert(null,splashText.toString(),
        null,AlertType.INFO);
      splashScreen.setTimeout(3000);
      Display.getDisplay(this).setCurrent(splashScreen);
    }

  }

  public void pauseApp() {}

  public void destroyApp(boolean cond)
  {
    notifyDestroyed();
  }

  public void commandAction(Command c, Displayable d)
  {
    if (c == view)
    {
      List curr = (List)d;
      final String currFile = curr.getString(curr.getSelectedIndex());
      new Thread(new Runnable()
      {
        public void run()
        {
          if (currFile.endsWith(SEP_STR) ||
            currFile.equals(UP_DIRECTORY))
          {
            traverseDirectory(currFile);
          } else
          {
            showFile(currFile);
          }
        }
      }).start();
    }
    else if (c == back)
    {
      showCurrDir();
    }
    else if (c == exit)
    {
      destroyApp(false);
    }
  }

  void showCurrDir()
  {
    Enumeration e;
    FileConnection currDir = null;
    List browser;
    try
    {
      if (MEGA_ROOT.equals(currDirName))
      {
        e = FileSystemRegistry.listRoots();
        browser = new List(currDirName, List.IMPLICIT);
      }
      else
      {
        currDir = (FileConnection)Connector.open(
          "file://localhost/" + currDirName);
        e = currDir.list();
        browser = new List(currDirName, List.IMPLICIT);
        // öö browser.append(UP_DIRECTORY);
      }
      while (e.hasMoreElements())
      {
        String fileName = (String)e.nextElement();
        if (fileName.charAt(fileName.length()-1) == SEP)
        {
          // öö browser.append(fileName);
        }
        else
        {
          // öö browser.append(fileName);
        }
      }
      browser.setSelectCommand(view);
      browser.addCommand(exit);
      browser.setCommandListener(this);
      if (currDir != null)
      {
        currDir.close();
      }
      Display.getDisplay(this).setCurrent(browser);
    }
    catch (IOException ioe)
    {}
  }

  void traverseDirectory(String fileName)
  {
    if (currDirName.equals(MEGA_ROOT))
    {
      if (fileName.equals(UP_DIRECTORY))
      {
        // can not go up from MEGA_ROOT
        return;
      }
      currDirName = fileName;
    }
    else if (fileName.equals(UP_DIRECTORY))
    {
      // Go up one directory
      // TODO use setFileConnection when implemented
      int i = currDirName.lastIndexOf(SEP, currDirName.length()-2);
      if (i != -1)
      {
        currDirName = currDirName.substring(0, i+1);
      }
      else
      {
        currDirName = MEGA_ROOT;
      }
    }
    else
    {
      currDirName = currDirName + fileName;
    }
    showCurrDir();
  }

  void showFile(String fileName)
  {
    try
    {
      FileConnection fc = (FileConnection)
      Connector.open("file://localhost/" + currDirName + fileName);
      if (!fc.exists())
      {
        throw new IOException("File does not exists");
      }
      InputStream fis = fc.openInputStream();
      byte[] b = new byte[1024];
      int length = fis.read(b, 0, 1024);
      fis.close();
      fc.close();

      TextBox tb = new TextBox("View File: " + fileName, null, 1024,
          TextField.ANY | TextField.UNEDITABLE);

      tb.addCommand(back);
      tb.addCommand(exit);
      tb.setCommandListener(this);

      if (length > 0)
      {
        tb.setString(new String(b, 0, length));
      }
      Display.getDisplay(this).setCurrent(tb);
    }
    catch (Exception e) {}
  }
}

