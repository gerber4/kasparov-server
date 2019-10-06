package org.kasparov;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.kasparov.message.Message;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;


/**
 * Server class managing the connection between the server and the players
 */
class Server extends WebSocketServer implements MessageHandler {

    private HashMap<Integer, Instance> instances;

    private final Gson gson = new Gson();

    Server(InetSocketAddress address) {
        super(address);
        this.instances = new HashMap<>();
    }

    @Override
    public void sendMessage(Message message, WebSocket player) {
        player.send(gson.toJson(message));
    }

    @Override
    public void sendMessage(Message message, Collection<WebSocket> players) {
        broadcast(gson.toJson(message), players);
    }

    @Override
    public void endGame(Integer gameId, WebSocket kasparov, Collection<WebSocket> players) {
        instances.remove(gameId);
        kasparov.close();
        for (WebSocket player : players) {
            player.close();
        }
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("new connection to " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
       System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("received message from " + conn.getRemoteSocketAddress() + ": " + message);
        JsonObject json = new Gson().fromJson(message, JsonObject.class);

        String msgType = json.get("msgType").getAsString();
        Integer gameID = json.get("gameID").getAsInt();

        if (msgType.equals("ConnectInstance")) {
            if (instances.containsKey(gameID)) {
                Instance instance = instances.get(gameID);
                instance.addPlayer(conn);
            } else {
                Instance instance = new Instance(gameID, this);
                instances.put(gameID, instance);

                instance.addPlayer(conn);
            }
        } else if (msgType.equals("Move")) {
            Instance instance = instances.get(gameID);

            String SAN = json.get("msg").getAsJsonObject().get("move").getAsString();

            instance.makeMove(conn, SAN);

        } else {
            throw new IllegalStateException("Unknown msgType: " + msgType);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
    }
}