
package org.oobd.base;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.oobd.base.support.Onion;

/**
 *
 * \brief Message-Port to supply sending, reading and waiting for messages
 * \ingroup core
 */
public class MessagePort {

    final Vector myMsgs;
    int replyID = 0;
    int waitingforID = 0;

    public MessagePort() {
        myMsgs = new Vector();
    }

    /**
     * \brief gets a message from the core and sorts it in the waiting message quere
     *
     * This function is called by the core when a message for the messageport owner comes in.
     *
     * As it need to be sure, that an unexpected system message does not wake up a task who is waiting for an answer to a message he has sented, the messages are been
     * sorted into the reveive msq quere and the task is waked up then accourdingly.
     *
     * @param thisMessage
     */
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
/**
 * \brief get the next message in the quere
 * \ingroup core
 * @param wait if true, waits forever for a message, otherways returns immediadly, even without a message
 * @return message
 */
    protected Message getMsg(boolean wait) {
        if (wait == true) {
            return getMsg(-1);
        } else {
            return getMsg(0);
        }
    }

/**
 * \brief sends an answer to a message
 * \ingroup core
 * @param msg the original message
 * @param onion the content of the message
 * @return message
 */
    protected void  replyMsg(Message msg, Onion content) {
        String rec = msg.rec;
        msg.rec=msg.sender;
        msg.sender = rec;
        msg.content= content;
        Core.getSingleInstance().transferMsg(msg);
    }

    /**
     * \brief send a message and waits for the answer
     * \ingroup core
     * @param msg the message
     * @param timeout timeout in ms to wait for an answer. 0: wait forever, <0 : Don't wait (which would be a little bit senseless ;-)
     * @return
     */
    public Message sendAndWait(Message msg, int timeout) {
        replyID = (replyID > 10000) ? 1 : replyID + 1;
        waitingforID = replyID;
        Core.getSingleInstance().transferMsg(msg);
        return getMsg(timeout);
    }

/**
 * \brief get the next message in the quere
 * \ingroup core
 * @param timeout in ms to wait for an message  . 0: wait forever, <0 don't wait
 * @return message
 */
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
