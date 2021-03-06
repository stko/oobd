package org.oobd.mobile;



import java.io.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import javax.wireless.messaging.*;

public class SendMMS extends Form implements CommandListener, ItemCommandListener {

    private Form parent; //Where this form was started from
    private Command backCommand = null;
    private Command okCommand = null;
    private Command sendToCmd;
    private Command subjectCmd;
    private TextField sendTo;
    private TextField subject;
    private ChoiceGroup sendAs;
//    Button scriptSelect = null;
//    final TextField addressField = new TextField();
//    final TextField subjectField = new TextField("OOBD Mobile Message");
    String mmsBody = null;
    OOBD_MEv2 mainMidlet = null;
//    ButtonGroup group = new ButtonGroup();


    public SendMMS(String mmsBody, Form parent, final OOBD_MEv2 mainMidlet) {
        super("Send MMS");
        this.parent = parent;
        this.mainMidlet = mainMidlet;
        this.mmsBody = mmsBody;

        sendTo = new TextField("Send text to:",null,32,TextField.EMAILADDR);
//        sendToCmd = new Command("Select", Command.ITEM,0);
//        sendTo.addCommand(sendToCmd);
//        sendTo.setItemCommandListener(this);

        subject = new TextField("Subject:","OOBD Mobile Message",32,TextField.ANY);
//        subjectCmd = new Command("Change", Command.ITEM,0);
//        subject.addCommand(subjectCmd);
//        subject.setItemCommandListener(this);

        sendAs = new ChoiceGroup("Send text as:",Choice.EXCLUSIVE);
        sendAs.append("Text", null);
        sendAs.append("XML", null);
        sendAs.append("Binary", null);
        sendAs.setSelectedIndex(0, true);

        this.append(sendTo);
        this.append(subject);
        this.append(sendAs);

        backCommand = new Command("Cancel",Command.BACK,0);
        this.addCommand(backCommand);
        okCommand = new Command("Send",Command.OK,0);
        this.addCommand(okCommand);
        this.setCommandListener(this);

        mainMidlet.getDisplay().setCurrent(this);
    }

    public void commandAction(Command c, Displayable d) {

        
        if (c == backCommand) {
            mainMidlet.getDisplay().setCurrent(parent);
        }
        if (c == okCommand) {
            String address = sendTo.getString();
            if (address != null && !address.equals("")) {
                //String appID = getAppProperty("MMS-ApplicationID");
                //String address = "mms://+5550000:" + appID;
                mainMidlet.setMmsAddress(address);
                address = "mms://" + address;
                MessageConnection mmsconn = null;
                try {
                    /** Open the message connection. */
                    mmsconn = (MessageConnection) Connector.open(address);

                    MultipartMessage mmmessage = (MultipartMessage) mmsconn.newMessage(
                            MessageConnection.MULTIPART_MESSAGE);
                    mmmessage.setAddress(address);
                    int fileFormat = sendAs.getSelectedIndex();
                    byte[] textMsgBytes = "This message was generated by OOBD-ME\n\nOOBD.org - the new diagnostics".getBytes("UTF-8");
                    MessagePart textPart = new MessagePart(textMsgBytes, 0, textMsgBytes.length, "text/plain",
                            "message", "message text", "UTF-8");
                    mmmessage.addMessagePart(textPart);
                    String fileName = null;
                    String mimeType = null;
                    String coding = null;
                    if (fileFormat == 0) {
                        fileName = "OOBD-log.txt";
                        mimeType = "text/plain";
                        coding = "UTF-8";
                        textMsgBytes = mmsBody.getBytes(coding);
                    }
                    if (fileFormat == 1) {
                        fileName = "OOBD-log.xml";
                        mimeType = "text/xml";
                        coding = "UTF-8";
                        textMsgBytes = mmsBody.getBytes(coding);
                    }
                    if (fileFormat == 2) {
                        fileName = "OOBD-log.bin";
                        mimeType = "application/octet-stream";
                        // coding is nil   coding="UTF-8";
                        textMsgBytes = mmsBody.getBytes();
                    }
                    textPart = new MessagePart(textMsgBytes, 0, textMsgBytes.length, mimeType, "file", fileName, coding);
                    mmmessage.addMessagePart(textPart);
                    mmmessage.setSubject(subject.getString());
                    mmmessage.setStartContentId("message");
                    mmsconn.send(mmmessage);
                    mainMidlet.showAlert("MMS has been sent");
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                if (mmsconn != null) {
                    try {
                        mmsconn.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                mainMidlet.getDisplay().setCurrent(this);
            } else {
                mainMidlet.showAlert("Please enter a valid receiver");
            }
        }
    }

    public void commandAction(Command c, Item item) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
