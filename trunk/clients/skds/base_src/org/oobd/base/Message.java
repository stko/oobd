/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.oobd.base;

/**
 *
 * @author steffen
 */

import org.oobd.base.support.Onion;

public class Message {

    String sender, rec;
    Onion content;
    OobdPlugin sendObject;

    public Message(OobdPlugin sender, String rec, Onion content){

        this.sendObject=sender;
        this.sender=sender.getPluginName();
        this.rec=rec;
        this.content=content;

    }

}
