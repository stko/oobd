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
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import org.eclipse.jetty.http.HttpVersion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jetty.server.Handler;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.resource.FileResource;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;


/**
 * generic abstract for the implementation of scriptengines
 *
 * @author steffen
 */



// taken from http://amilamanoj.blogspot.de/2013/06/secure-websockets-with-jetty.html



abstract public class WSOobdUIHandler extends OobdUIHandler {

    protected Onion myStartupParam;

    public static String publicName() {
        /* the abstract class also needs to have this method, because it's also loaded during dynamic loading, and the empty return string
         ** is the indicator for this abstract class
         */
        return "webUIHandler";
    }

    public WSOobdUIHandler(String myID, Core myCore, IFsystem mySystem, String name) {
        super(myID, myCore, mySystem, name);
        id = myID;
        core = myCore;
        UISystem = mySystem;
        Logger.getLogger(WSOobdUIHandler.class.getName()).log(Level.CONFIG, "Web UIHandler  object created: {0}", id);

    }

    public void start() {
        System.err.println("Start WEB SERVER");

        WebSocketServer webSocketServer = new WebSocketServer();
        webSocketServer.setHost("localhost");
        webSocketServer.setPort(8443);
        try {
            webSocketServer.setKeyStoreResource(new FileResource(WebSocketServer.class.getResource("/keystore.jks")));
            webSocketServer.setKeyStorePassword("password");
            webSocketServer.setKeyManagerPassword("password");
            webSocketServer.addWebSocket(MyWebSocket.class, "/");
            webSocketServer.initialize();
            webSocketServer.start();
            // set userInterface here (somehow..)
        } catch (Exception ex) {
            Logger.getLogger(WSOobdUIHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
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

@WebSocket
class MyWebSocket {

    private RemoteEndpoint remote;

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket Opened");
        this.remote = session.getRemote();
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        System.out.println("Message from Client: " + message);
        try {
            remote.sendString("Hi Client:Here's my echo:" + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.println("WebSocket Closed. Code:" + statusCode);
    }
}

class WebSocketServer {

    private Server server;
    private String host;
    private int port;
    private Resource keyStoreResource;
    private String keyStorePassword;
    private String keyManagerPassword;
    private List<Handler> webSocketHandlerList = new ArrayList<Handler>();

    public static void main(String[] args) throws Exception {
        WebSocketServer webSocketServer = new WebSocketServer();
        webSocketServer.setHost("localhost");
        webSocketServer.setPort(8443);
        // the p12 keyfile needs to be in the same directory as this source file to be included into the jar through the build process
        webSocketServer.setKeyStoreResource(new FileResource(WebSocketServer.class.getResource("/keystore.jks")));
        webSocketServer.setKeyStorePassword("password");
        webSocketServer.setKeyManagerPassword("password");
        webSocketServer.addWebSocket(MyWebSocket.class, "/");
        webSocketServer.initialize();
        webSocketServer.start();
    }

    public void initialize() {
        server = new Server();
        // connector configuration
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStoreResource(keyStoreResource);
        sslContextFactory.setKeyStorePassword(keyStorePassword);
        sslContextFactory.setKeyManagerPassword(keyManagerPassword);
        SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString());
        HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(new HttpConfiguration());
        ServerConnector sslConnector = new ServerConnector(server, sslConnectionFactory, httpConnectionFactory);
        sslConnector.setHost(host);
        sslConnector.setPort(port);
        server.addConnector(sslConnector);
        // handler configuration
        HandlerCollection handlerCollection = new HandlerCollection();
        handlerCollection.setHandlers(webSocketHandlerList.toArray(new Handler[0]));
        server.setHandler(handlerCollection);
    }

    public void addWebSocket(final Class<?> webSocket, String pathSpec) {
        WebSocketHandler wsHandler = new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory webSocketServletFactory) {
                webSocketServletFactory.register(webSocket);
            }
        };
        ContextHandler wsContextHandler = new ContextHandler();
        wsContextHandler.setHandler(wsHandler);
        wsContextHandler.setContextPath(pathSpec);  // this context path doesn't work ftm
        webSocketHandlerList.add(wsHandler);
    }

    public void start() throws Exception {
        server.start();
        server.join();
    }

    public void stop() throws Exception {
        server.stop();
        server.join();
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setKeyStoreResource(Resource keyStoreResource) {
        this.keyStoreResource = keyStoreResource;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public void setKeyManagerPassword(String keyManagerPassword) {
        this.keyManagerPassword = keyManagerPassword;
    }

}
