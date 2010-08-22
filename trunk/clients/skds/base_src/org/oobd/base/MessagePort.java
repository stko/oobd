/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;

/**
 *
 * @author steffen
 */
public class MessagePort {

    final Vector myMsgs;
    int replyID = 0;
    int waitingforID = 0;

    public MessagePort() {
        myMsgs = new Vector();
    }

    public void receive(Message thisMessage) {
        int thisReplyID;
        try {
            thisReplyID = thisMessage.content.getInt("replyID");
        } catch (JSONException ex) {
            thisReplyID = -1;
        }
        if (thisReplyID > 0) { // the message contains a replyID
            if (thisReplyID == waitingforID) { //only if this is really the message we are waiting for, otherways just delete this obviously old reply
                synchronized (myMsgs) {
                    myMsgs.insertElementAt(thisMessage, 0); //put the message as the first one in the message quere
                    waitingforID = 0; // reset the waitingFor Flag
                    myMsgs.notify();
                }

            }
        } else {
            synchronized (myMsgs) {
                myMsgs.add(thisMessage);
                if (waitingforID == 0) { //if we not just waiting for a delicated reply message
                    myMsgs.notify();
                }
            }
        }

    }

    protected Message getMsg(boolean wait) {
        if (wait == true) {
            return getMsg(-1);
        } else {
            return getMsg(0);
        }
    }

    public Message sendAndWait(Message msg, int timeout) {
        replyID = (replyID > 10000) ? 1 : replyID + 1;
        waitingforID = replyID;
        Core.getSingleInstance().transferMsg(msg);
        return getMsg(timeout);
    }

    protected Message getMsg(int timeout) {

        if (myMsgs.isEmpty() || waitingforID != 0) {
            if (timeout < 0) { // wait forever
                try {
                    synchronized (myMsgs) {
                        myMsgs.wait();
                    }
                } catch (InterruptedException ex) {
                    return null;
                }
                waitingforID = 0; // reset the waitingFor Flag
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
            waitingforID = 0; // reset the waitingFor Flag
            return (Message) myMsgs.remove(0);
        }
    }
}
