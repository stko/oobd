/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.port;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.oobd.base.*;
import org.oobd.base.support.Onion;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.oobd.base.bus.OobdBus;


@WebSocket
public class OobdBusWebSocket {

    Session session;
    OobdBus msgReceiver;

    private final CountDownLatch closeLatch = new CountDownLatch(1);

    public OobdBusWebSocket(OobdBus msgReceiver) {
        this.msgReceiver = msgReceiver;
        session=null;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        this.session=session;
        System.out.println("WebSocket Opened in client side");
    }

    void send(String message, String channel) {
        if (session!=null){
            try {
            System.out.println("Sending message: Hi server");
            session.getRemote().sendString(new Onion("{'msg':'" + Base64Coder.encodeString(message) + "','channel': '" + channel + "'}").toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException ex) {
            Logger.getLogger(OobdBusWebSocket.class.getName()).log(Level.SEVERE, null, ex);
        }
        }else{
            System.out.println("try to send "+message + "but session not opened yet");
        }

    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        System.out.println("Message from Server: " + message);
        try {
            Onion myOnion = new Onion(message);
            msgReceiver.receiveString(myOnion.getOnionBase64String("reply"));
        System.out.println("reply message received: " + myOnion.getOnionBase64String("reply"));
       } catch (JSONException ex) {
            Logger.getLogger(ComPort_Kadaver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.println("WebSocket Closed. Code:" + statusCode+" Reason:"+reason);
    }

    public boolean awaitClose(int duration, TimeUnit unit) throws InterruptedException {
        return this.closeLatch.await(duration, unit);
    }
}
