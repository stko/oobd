/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base;

import java.util.Vector;

/**
 *
 * @author steffen
 */
public class MessagePort {

    final Vector myMsgs;

    public MessagePort() {
        myMsgs = new Vector();
    }

    public void receive(Message thisMessage) {
        synchronized (myMsgs) {
            myMsgs.add(thisMessage);
            myMsgs.notify();
        }

    }

    protected Message getMsg(boolean wait) {
        if (wait == true) {
            return getMsg(-1);
        } else {
            return getMsg(0);
        }
    }

    protected Message getMsg(int timeout) {
        if (myMsgs.isEmpty()) {
            if (timeout < 0) { // wait forever
                try {
                    synchronized (myMsgs) {
                        myMsgs.wait();
                    }
                } catch (InterruptedException ex) {
                    return null;
                }
                return (Message) myMsgs.remove(0);
            } else {
                if (timeout == 0) {
                    return null;
                } else {
                    try {
                        synchronized (myMsgs) {
                            myMsgs.wait(timeout);
                        }
                    } catch (InterruptedException ex) {
                        return null;
                    }
                    if (myMsgs.isEmpty()) {
                        return null;
                    } else {
                        return (Message) myMsgs.remove(0);
                    }
                }
            }
        } else {
            return (Message) myMsgs.remove(0);
        }
    }
}
