/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for all oobd classes loadable during runtime
 * @author steffen
 */
public abstract class OobdPlugin implements Runnable {

    static Core core;
    MessagePort msgPort;
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
}
