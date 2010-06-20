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

public abstract class Script {

    public abstract int Script();

    public abstract void doRun() throws IOException;

    public abstract void doScript(String fileName) throws IOException;

    public abstract String callFunction(String functionName, Object[] params);

    public abstract String GetVarString(String var);

    public abstract String GetArgAsString(int index);

    public abstract int Unload();
    /* Tja, leider hat das mit dem Kapseln des Script- Interpreters
     * zwar fast, aber nicht ganz funktioniert. MLDP scheint noch 
     * nicht Java 1.5 zu unterstützen, und damit auch keine variablen 
     * Übergabeparameter und kein Autoboxing für elementare Datentypen
     * wie Integer.
     * Und damit ist das Interface dann doch wieder Scriptengine-abhängig..
     */

    public abstract void register(Object key, Object value);

    public abstract void initRPC(Object key, int nArgs);

    public abstract void finishRPC(Object key, int nArgs);
    // Methoden zur Variablen-"Kommunikation"

    public abstract String getString(int index);

    public abstract boolean getBoolean(int index);

    public abstract int getInt(int index);
}

