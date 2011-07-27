package org.oobd.ui.android.bus;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.oobd.base.Base64Coder;
import org.oobd.base.Core;
import org.oobd.base.Debug;
import org.oobd.base.Message;
import org.oobd.base.OOBDConstants;
import org.oobd.base.bus.OobdBus;
import org.oobd.base.support.Onion;

import android.util.Log;

/**
 *
 * @author steffen
 */
public class BusEchoAndroid extends OobdBus implements OOBDConstants{

    public BusEchoAndroid() {
            super("Buscom");
        Debug.msg("busecho",DEBUG_BORING,"Ich bin BusEchoAndroid...");
    }

    @Override
    public void registerCore(Core thisCore) {
        super.registerCore(thisCore);
        Debug.msg("busecho",DEBUG_BORING,"Core registered...");
    }

    @Override
    public String getPluginName() {
        return "b:Echo";
    }

    public void run() {
    	
    	while (keepRunning == true) {
        	System.out.println ("--- naechste Runde ---");
            Debug.msg("buscom", DEBUG_BORING, "sleeping...");
            Message msg = getMsg(true);
            Onion on = msg.getContent();
            Debug.msg("buscom", DEBUG_BORING, "Msg received:" + msg.getContent().toString());
            String command = on.getOnionString("command");
            if ("serWrite".equalsIgnoreCase(command)) {
                try {
                	/*// Old Version: sun.misc.*
                    reader.write(new String(new BASE64Decoder().decodeBuffer(on.getOnionString("data"))));
                    replyMsg(msg, new Onion(""
                            + "{'type':'" + CM_RES_BUS + "',"
                            + "'owner':"
                            + "{'name':'" + getPluginName() + "'},"
                            + "'result':'" + "" + "'}"));
                    */
                	//reader.write(new String(Base64Coder.decodeLines(on.getOnionString("data"))));
                    replyMsg(msg, new Onion(""
                            + "{'type':'" + CM_RES_BUS + "',"
                            + "'owner':"
                            + "{'name':'" + getPluginName() + "'},"
                            + "'result':'" + "" + "'}"));
                } catch (JSONException ex) {
                    Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE, null, ex);
                } 

            } else if ("serFlush".equalsIgnoreCase(command)) {
                try {
                    //reader.flush();
                    replyMsg(msg, new Onion(""
                            + "{'type':'" + CM_RES_BUS + "',"
                            + "'owner':"
                            + "{'name':'" + getPluginName() + "'},"
                            + "'result':'" + "" + "'}"));
                } catch (JSONException ex) {
                    Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if ("serWait".equalsIgnoreCase(command)) {
                try {
                	// Old Version: sun.misc.*
                    //Integer result = reader.wait(new String(new BASE64Decoder().decodeBuffer(on.getOnionString("data"))), on.getInt("timeout"));
                	//Integer result = reader.wait(new String(Base64Coder.decodeLines(on.getOnionString("data"))), on.getInt("timeout"));
                    //System.out.println("busCom serWait: " + result);
                    replyMsg(msg, new Onion(""
                            + "{'type':'" + CM_RES_BUS + "',"
                            + "'owner':"
                            + "{'name':'" + getPluginName() + "'},"
                            + "'result':" + '0' +","
                            + "'replyID':"+on.getInt("replyID")
                            + "}"));
                } catch (JSONException ex) {
                    Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE, null, ex);
                } 


            } else if ("serReadLn".equalsIgnoreCase(command)) {
                try {
                    //String result = reader.readln(on.getInt("timeout"), on.optBoolean("ignore"));
                    //System.out.println("busCom readline: " + result);
                    /*//Old Version: sun.misc.*
                    replyMsg(msg, new Onion(""
                            + "{'type':'" + CM_RES_BUS + "',"
                            + "'owner':"
                            + "{'name':'" + getPluginName() + "'},"
                            + "'replyID':"+on.getInt("replyID")+ ","
                            + "'result':'" + new BASE64Encoder().encode(result.getBytes()) + "'}"));
                    */
                    replyMsg(msg, new Onion(""
                            + "{'type':'" + CM_RES_BUS + "',"
                            + "'owner':"
                            + "{'name':'" + getPluginName() + "'},"
                            + "'replyID':"+on.getInt("replyID")+ ","
                            + "'result':'" + String.valueOf(Base64Coder.encode(new String ("41 0C 66 E0").getBytes())) + "'}"));
                } catch (JSONException ex) {
                    Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                try {

                    replyMsg(msg, new Onion(""
                            + "{'type':'" + CM_RES_BUS + "',"
                            + "'owner':"
                            + "{'name':'" + getPluginName() + "'},"
                            + "'result':'" + "" + "'}"));
                } catch (JSONException ex) {
                    Logger.getLogger(BusCom.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            Debug.msg("buscom", DEBUG_BORING, "waked up after received msg...");

        }
    }
}
