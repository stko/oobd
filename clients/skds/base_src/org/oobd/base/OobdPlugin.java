
package org.oobd.base;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.oobd.base.support.Onion;


/**
 * Base class for all oobd classes loadable during runtime
 * 
 */
public abstract class OobdPlugin implements Runnable {

    protected static Core core;
    protected MessagePort msgPort;
    protected boolean keepRunning = true;

    public void registerCore(Core thisCore) {
        core = thisCore;
    }

    public OobdPlugin() {
        msgPort = new MessagePort();
    }

    public MessagePort getMsgPort() {
        return msgPort;
    }

    public void cancel() {
        keepRunning = false;
    }

     public abstract String getPluginName();

    protected Message getMsg(boolean wait) {
        return (Message) msgPort.getMsg(wait);
    }

    public void sendMsg(Message msg){
        msgPort.receive(msg);
    }
    public void replyMsg(Message msg, Onion content){
        msgPort.replyMsg(msg, content);
    }
}
