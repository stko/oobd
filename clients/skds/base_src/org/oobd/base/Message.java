

package org.oobd.base;

import org.oobd.base.support.Onion;

/**
 * \brief Message Object to handle the internal data exchange
 * \ingroup core
 */

public class Message {

    String sender, rec;
    Onion content;
    OobdPlugin sendObject;
/**
 * \brief Generates a new message object
 * @param sender the sending object
 * @param rec the string id, means the name of the receipient
 * @param content an Onion containing the data
 */
    public Message(OobdPlugin sender, String rec, Onion content){

        this.sendObject=sender;
        this.sender=sender.getId();
        this.rec=rec;
        this.content=content;

    }
    /**
     * \brief returns the message content
     * @return the message content
     */
    public Onion getContent(){
        return content;
    }

}
