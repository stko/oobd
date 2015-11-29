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
    String portName = "";

    /**
     * setup a new message port
     * \param name Name of the port, just for debugging
     */
    public MessagePort(String name) {
        myMsgs = new Vector();
        portName = name;
    }

    /**
     * \brief gets a message from the core and sorts it in the waiting message quere
     *
     * This function is called by the core when a message for the messageport owner comes in.
     *
     * As it need to be sure, that an unexpected system message does not wake up a task who is waiting for an answer to a message he has sented, the messages with a reply ID are been
     * sorted into the reveive msq quere as first and the task is waked up then accourdingly.
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
        //Logger.getLogger(MessagePort.class.getName()).log(Level.INFO, "Port " + portName + " Msg reply id:" + Integer.toString(thisReplyID) + " Waiting ID:" + Integer.toString(waitingforID));
        if (thisReplyID > 0) { // this is a reply for something
            //Logger.getLogger(MessagePort.class.getName()).log(Level.INFO, "A reply message " + thisReplyID);
            if (thisReplyID == waitingforID) { //only if this is really the message we are waiting for, otherways just forget this obviously old reply
                synchronized (myMsgs) {
                    myMsgs.insertElementAt(thisMessage, 0); //put the message as the first one in the message quere
                    waitingforID = 0; // reset the waitingFor Flag
                    myMsgs.notify();
                    //Logger.getLogger(MessagePort.class.getName()).log(Level.INFO, "saved and notified");
                }
            } else {
                Logger.getLogger(MessagePort.class.getName()).log(Level.WARNING, "wrong reply id (" + thisReplyID + "<>" + waitingforID + ") message deleted");
            }
        } else { // this is a normal, non replied msg
            //Logger.getLogger(MessagePort.class.getName()).log(Level.INFO, "A non reply message " + thisReplyID);
            synchronized (myMsgs) {
                myMsgs.add(thisMessage);
               // Logger.getLogger(MessagePort.class.getName()).log(Level.INFO, "msg saved");
                if (waitingforID == 0) { //if we not just waiting for a delicated reply message
                    Logger.getLogger(MessagePort.class.getName()).log(Level.INFO, "msg notified");
                    myMsgs.notify();
                } else {
                    Logger.getLogger(MessagePort.class.getName()).log(Level.INFO, "Msg NOT notified, as we wait for " + waitingforID);
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
            Message msg = null;
            while (msg == null) {
                msg = getMsg(-1);
            }
            return msg;
        } else {
            return getMsg(0);
        }
    }

    /**
     * \brief get the name of that message port
     * \ingroup core
     * @return message port name
     */
    public String getMsgPortName( ) {
        return portName;
    }

    /**
     * \brief sends an answer to a message
     * \ingroup core
     * @param msg the original message
     * @param onion the content of the message
     * @return message
     */
    public void replyMsg(Message msg, Onion content) {
        String rec = msg.rec;
        msg.rec = msg.sender;
        msg.sender = rec;
        msg.content = content;
        Core.getSingleInstance().transferMsg(msg);
    }

    /**
     * \brief send a message and waits for the answer
     * \ingroup core
     * @param msg the message
     * @param timeout timeout in ms to wait for an answer. <0: wait forever, =0 : Don't wait (which would be a little bit senseless ;-)
     * @return
     */
    public Message sendAndWait(Message msg, int timeout) {
        replyID = (replyID > 10000) ? 1 : replyID + 1;
        waitingforID = replyID;
        try {
            msg.content.put("msgID", replyID);
        } catch (JSONException ex) {
            Logger.getLogger(MessagePort.class.getName()).log(Level.SEVERE, null, ex);
        }
        Core.getSingleInstance().transferMsg(msg);
        return getMsg(timeout);
    }

    /**
     * \brief get the next message in the quere
     * \ingroup core
     * @param timeout in ms to wait for an message  . 0: wait forever, <0 don't wait
     * @return message
     */
    public Message getMsg(int timeout) {

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
                    waitingforID = 0; // reset the waitingFor Flag
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
