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
        if (myMsgs.isEmpty()) {
            if (wait == true) {
                try {
                    synchronized (myMsgs) {
                        myMsgs.wait();
                    }
                } catch (InterruptedException ex) {
                    return null;
                }
                return (Message) myMsgs.remove(0);
            } else {
                return null;
            }
        } else {
            return (Message) myMsgs.remove(0);
        }
    }
}
