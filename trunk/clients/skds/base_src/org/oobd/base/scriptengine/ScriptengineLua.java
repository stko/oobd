/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.scriptengine;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.oobd.base.*;
import org.oobd.base.support.Onion;

import java.io.InputStream;
import java.io.IOException;

import java.io.*;
import java.io.File.*;


import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.stdlib.CoroutineLib;
import se.krka.kahlua.stdlib.MathLib;
import se.krka.kahlua.stdlib.StringLib;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.LuaPrototype;
import se.krka.kahlua.vm.LuaState;
import org.json.JSONException;

/**
 *
 * @author steffen
 */
public class ScriptengineLua extends OobdScriptengine {

    private LuaState state;
    private LuaCallFrame callFrame;
    private int nArguments;

    public ScriptengineLua(String ID, Core myCore) {
        super(ID, myCore);
        Debug.msg("scriptenginelua", DEBUG_BORING, "Ich bin der ScriptengineLua...");



    }

    @Override
    public String getPluginName() {
        return "se:Lua";
    }

    public static String publicName() {
        return "Script";
    }

    /**
     * @todo open & write files MUST go through an generic stream supported by core to handle different sources and encrypted files
     */
    public void run() {
        state = new LuaState(System.out);
        BaseLib.register(state);
        MathLib.register(state);
        StringLib.register(state);
        CoroutineLib.register(state);

        InputStream resource = getClass().getResourceAsStream("/stdlib.lbc");
        try {
            state.call(LuaPrototype.loadByteCode(resource, state.getEnvironment()), null, null, null);
        } catch (IOException ex) {
            Logger.getLogger(ScriptengineLua.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            core.transferMsg(new Message(this, CoreMailboxName, new Onion("{\"type\":\"noaction\"}")));
            core.transferMsg(new Message(this, CoreMailboxName, new Onion(""
                    + "{'type':'" + CM_CANVAS + "',"
                    + "'owner':'" + this.id + "',"
                    + "'name':'Canvastest_1'}")));
            core.transferMsg(new Message(this, CoreMailboxName, new Onion(""
                    + "{'type':'" + CM_CANVAS + "',"
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
            Debug.msg("scriptengineterminal", DEBUG_BORING, "sleeping...");
            Message msg = getMsg(true);
            Onion on = msg.getContent();
            String vis = on.getOnionString("vis");
            Debug.msg("scriptengineterminal", DEBUG_BORING, "Msg received:" + msg.getContent().toString());
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
            Debug.msg("scriptengineterminal", DEBUG_BORING, "waked up after received msg...");

        }
    }

    public void doScript(String fileName) throws IOException {
        InputStream resource = new FileInputStream(fileName);
        LuaClosure callback = LuaPrototype.loadByteCode(resource, state.getEnvironment());
        state.call(callback, null, null, null);

    }

    public String callFunction(String functionName, Object[] params) {
        LuaClosure fObject = (LuaClosure) state.getEnvironment().rawget(functionName);
        System.err.println(fObject.getClass().toString());
        System.err.println(fObject.toString());
        Object[] results = state.pcall(fObject, params);
        if (results[0] != Boolean.TRUE) {
            Object errorMessage = results[1];
            System.out.println("Crash: " + errorMessage);
            System.out.println(results[2]);
            Throwable stacktrace = (Throwable) (results[3]);
            if (stacktrace != null) {
                stacktrace.printStackTrace();
            }
        }

        //String response = BaseLib.rawTostring(fObject.env.rawget(1));
        //fObject.push(response.intern());
        return (String) results[1];

        //return "-";
    }

    public String GetVarString(String var) {
        return "";
    }

    public String GetArgAsString(int index) {
        return "";
    }

    public int Unload() {
        return 0;
    }

    public void register(Object key, Object value) {
        state.getEnvironment().rawset(key, value);
    }
    /* Tja, leider hat das mit dem Kapseln des Script- Interpreters
     * zwar fast, aber nicht ganz funktioniert. MLDP scheint noch
     * nicht Java 1.5 zu unterstützen, und damit auch keinen variablen
     * Übergabeparameter und kein Autoboxing für elementare Datentypen
     * wie Integer.
     * Und damit ist das Interface dann doch wieder Scriptengine-abhängig..
     */

    public void initRPC(Object key, int nArgs) {
        callFrame = (LuaCallFrame) key;
        nArguments = nArgs;
    }

    public void finishRPC(Object key, int nArgs) {
        // für Lua passiert hier nix...
    }

    public String getString(int index) {
        System.out.println("Lua get string index " + Integer.toString(index));
        String response = BaseLib.rawTostring(callFrame.get(index));
        // callFrame.push(response.intern());
        return response;
    }

    public boolean getBoolean(int index) {
        Boolean response = (Boolean) callFrame.get(index);
        //callFrame.push(response);
        return response.booleanValue();
    }

    public int getInt(int index) {
        Double response = BaseLib.rawTonumber(callFrame.get(index));
        //callFrame.push(response);
        return response.intValue();
    }
}
