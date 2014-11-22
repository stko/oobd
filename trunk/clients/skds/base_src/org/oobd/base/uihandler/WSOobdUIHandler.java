/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.oobd.base.uihandler;

import org.oobd.base.*;
import org.oobd.base.support.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.eclipse.jetty.server.Server;


import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
 
import javax.servlet.http.HttpServletRequest;
 
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;


/**
 * generic abstract for the implementation of scriptengines
 * @author steffen
 */
abstract public class WSOobdUIHandler extends OobdUIHandler{

    protected Onion myStartupParam;

    public static String publicName() {
        /* the abstract class also needs to have this method, because it's also loaded during dynamic loading, and the empty return string
         ** is the indicator for this abstract class
         */
        return "webUIHandler";
    }

    public WSOobdUIHandler(String myID, Core myCore, IFsystem mySystem, String name) {
        super( myID,  myCore,  mySystem,  name);
        id = myID;
        core = myCore;
        UISystem = mySystem;
        Logger.getLogger(WSOobdUIHandler.class.getName()).log(Level.CONFIG, "Web UIHandler  object created: {0}", id);

    }

    public void start() {
        System.err.println("Start WEB SERVER");
        try {
            // 1) Create a Jetty server with the 8091 port.
            Server server = new Server(8081);
            // 2) Register ChatWebSocketHandler in the Jetty server instance.
            ChatWebSocketHandler chatWebSocketHandler = new ChatWebSocketHandler();
            chatWebSocketHandler.setHandler(new DefaultHandler());
            server.setHandler(chatWebSocketHandler);
            // 2) Start the Jetty server.
            server.start();
            // Jetty server is stopped when the Thread is interruped.
            server.join();
        } catch (Throwable e) {
            e.printStackTrace();
        }
         // set userInterface here (somehow..)
    }

    public void handleMsg() {
        Message thisMsg;
        while ((thisMsg = this.getMsgPort().getMsg(0)) != null) { // if msg quere is not empty
/*
            if (actionRequest(thisMsg.getContent()) == true) {
            try {
            thisMsg.setContent(thisMsg.getContent().setValue("replyID",
            thisMsg.getContent().getInt("msgID")));
            } catch (JSONException ex) {
            Logger.getLogger(Core.class.getName()).log(
            Level.SEVERE, null, ex);
            }
            msgPort.replyMsg(thisMsg, thisMsg.getContent());
            }
             * 
             */
        }

    }
}


 class ChatWebSocketHandler extends WebSocketHandler {
 
    private final Set<ChatWebSocket> webSockets = new CopyOnWriteArraySet<ChatWebSocket>();
 
    public WebSocket doWebSocketConnect(HttpServletRequest request,
            String protocol) {
        return new ChatWebSocket();
    }
 
    private class ChatWebSocket implements WebSocket.OnTextMessage {
 
        private Connection connection;
 
        public void onOpen(Connection connection) {
            // Client (Browser) WebSockets has opened a connection.
            // 1) Store the opened connection
            this.connection = connection;
            // 2) Add ChatWebSocket in the global list of ChatWebSocket
            // instances
            // instance.
            webSockets.add(this);
        }
 
        public void onMessage(String data) {
            // Loop for each instance of ChatWebSocket to send message server to
            // each client WebSockets.
            try {
                for (ChatWebSocket webSocket : webSockets) {
                    // send a message to the current client WebSocket.
                    webSocket.connection.sendMessage(data);
                    webSocket.connection.sendMessage("OOBD replies:"+data);
                }
            } catch (IOException x) {
                // Error was detected, close the ChatWebSocket client side
                this.connection.disconnect();
            }
 
        }
 
        public void onClose(int closeCode, String message) {
            // Remove ChatWebSocket in the global list of ChatWebSocket
            // instance.
            webSockets.remove(this);
        }
    }
}