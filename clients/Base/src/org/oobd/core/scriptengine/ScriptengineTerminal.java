/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.core.scriptengine;

import org.oobd.core.OOBDConstants;
import org.oobd.core.Message;
import org.oobd.core.IFsystem;
import org.oobd.core.Core;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.oobd.core.support.Onion;

/**
 * generic abstract for the implementation of protocols
 * @author steffen
 */
public class ScriptengineTerminal extends OobdScriptengine implements OOBDConstants {

    public ScriptengineTerminal(String id, Core myCore, IFsystem mySystem) {
        super(id, myCore, mySystem, "ScriptengineTerminal id " + id);
        Logger.getLogger(ScriptengineTerminal.class.getName()).log(Level.CONFIG,  "Construct ScriptengineTerminal instance "+id);

    }

    @Override
    public String getPluginName() {
        return "se:Terminal";
    }

    public static String publicName() {
        return "Terminal";
    }

    /**
     * @todo: Acion canvas wir der Owner noch als reiner String übergeben, nicht als Sub-Onion
     */
    public void run() {
        try {
            core.transferMsg(new Message(this, CoreMailboxName, new Onion("{\"type\":\"noaction\"}")));
            core.transferMsg(new Message(this, CoreMailboxName, new Onion(""
                    + "{'type':'" + CM_PAGE + "',"
                    + "'owner':'" + this.id + "',"
                    + "'name':'Canvastest_1'}")));
            core.transferMsg(new Message(this, CoreMailboxName, new Onion(""
                    + "{'type':'" + CM_PAGE + "',"
                    + "'owner':'" + this.id + "',"
                    + "'name':'Canvastest_2'}")));
            core.transferMsg(new Message(this, CoreMailboxName, new Onion(""
                    + "{'type':'" + CM_VISUALIZE + "',"
                    + "'owner':"
                    + "{'name':'" + this.id + "'},"
                    + "'canvas':'Canvastest_1',"
                    + "'tooltip':'erste Worte...',"
                    + "'name':'table_1'}")));
            core.transferMsg(new Message(this, CoreMailboxName, new Onion(""
                    + "{'type':'" + CM_VISUALIZE + "',"
                    + "'owner':"
                    + "{'name':'" + this.id + "'},"
                    + "'canvas':'Canvastest_1',"
                    + "'tooltip':'Wort 2',"
                    + "'name':'table_2'}")));
            core.transferMsg(new Message(this, CoreMailboxName, new Onion(""
                    + "{'type':'" + CM_VALUE + "',"
                    + "'owner':"
                    + "{'name':'" + this.id + "'},"
                    + "'to':"
                    + "{'name':'table_2'},"
                    + "'ValueString':'uups..'}")));
        } catch (JSONException ex) {
            Logger.getLogger(ScriptengineTerminal.class.getName()).log(Level.SEVERE, null, ex);
        }
        int i = 0;
        while (keepRunning == true) {
            Message msg = getMsg(true);
            Onion on = msg.getContent();
            String vis = on.getOnionString("vis","");
            Logger.getLogger(ScriptengineTerminal.class.getName()).log(Level.SEVERE,"Msg received:" + msg.getContent().toString());
            try {

                core.transferMsg(new Message(this, CoreMailboxName, new Onion(""
                        + "{'type':'" + CM_VALUE + "',"
                        + "'owner':"
                        + "{'name':'" + this.id + "'},"
                        + "'to':"
                        + "{'name':'" + vis + "'},"
                        + "'ValueString':'" + Integer.toString(i) + "'}")));
            } catch (JSONException ex) {
                Logger.getLogger(ScriptengineTerminal.class.getName()).log(Level.SEVERE, null, ex);
            }
            i++;

        }
    }
}
