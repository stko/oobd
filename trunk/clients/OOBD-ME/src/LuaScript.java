/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author steffen
 */
import java.io.InputStream;
import java.io.IOException;
import javax.microedition.io.*;
import javax.microedition.io.file.*;


import se.krka.kahlua.stdlib.BaseLib;
import se.krka.kahlua.stdlib.CoroutineLib;
import se.krka.kahlua.stdlib.MathLib;
import se.krka.kahlua.stdlib.StringLib;
import se.krka.kahlua.vm.JavaFunction;
import se.krka.kahlua.vm.LuaCallFrame;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.LuaPrototype;
import se.krka.kahlua.vm.LuaState;

public class LuaScript extends Script {

    private LuaState state;
    private LuaCallFrame callFrame;
    private int nArguments;

    public int Script() {
        state = new LuaState(System.out);
        BaseLib.register(state);
        MathLib.register(state);
        StringLib.register(state);
        CoroutineLib.register(state);
        return 0;
    }

    public void doRun() throws IOException {
        InputStream resource = getClass().getResourceAsStream("/stdlib.lbc");
        state.call(LuaPrototype.loadByteCode(resource, state.getEnvironment()), null, null, null);
    }

    public void doScript(String fileName) throws IOException {
        InputStream resource;
        if (fileName.startsWith("file:")) {
            FileConnection fc = (FileConnection) Connector.open(fileName);
            if (!fc.exists()) {
                throw new IOException("File does not exists");
            }
            resource = fc.openInputStream();
        } else {
            resource = getClass().getResourceAsStream(fileName);
        }
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
