package org.kasparov;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;


/**
 * Server class managing the connection between the server and the players
 */
public class KasparovServer extends WebSocketServer {

    private HashMap<Integer, KasparovInstance> instances;

    private KasparovServer(InetSocketAddress address) {
        super(address);
        this.instances = new HashMap<>();
    }

    public static void main(String[] args) {
        String host = "0.0.0.0";
        int port = 8887;
        KasparovServer server = new KasparovServer(new InetSocketAddress(host, port));

        Thread thread = new Thread(server);
        thread.start();
    }

    void setConnected(WebSocket player) {
        player.send("{\"msgType\": \"InstanceConnected\"}");
    }

    void setAsKasparov(WebSocket kasparov) {
        kasparov.send("{\"msgType\": \"SetPlayer\", \"msg\": {\"player\": \"Kasparov\"}}");
    }

    void setAsWorld(WebSocket player) {
        player.send("{\"msgType\": \"SetPlayer\", \"msg\": {\"player\": \"World\"}}");
    }

    void setState(KasparovGameState state, WebSocket player) {
        String message = String.format("{\"msgType\": \"SetState\", \"msg\": {\"state\": \"%s\"}}", state.toString());
        player.send(message);
    }

    void setState(KasparovGameState state, Collection<WebSocket> players) {
        String message = String.format("{\"msgType\": \"SetState\", \"msg\": {\"state\": \"%s\"}}", state.toString());
        broadcast(message, players);
    }

    void setBoard(char[][] boardArray, WebSocket player) {
        String boardJSON = new Gson().toJson(boardArray);
        String message = String.format("{\"msgType\": \"SetBoard\", \"msg\": {\"board\": %s}}", boardJSON);
        player.send(message);
    }

    void setBoard(char[][] boardArray, Collection<WebSocket> players) {
        String boardJSON = new Gson().toJson(boardArray);
        String message = String.format("{\"msgType\": \"SetBoard\", \"msg\": {\"board\": %s}}", boardJSON);
        broadcast(message, players);
    }

    void endGame(Integer gameId, WebSocket kasparov, Collection<WebSocket> players) {
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
                KasparovInstance instance = instances.get(gameID);
                instance.addPlayer(conn);
            } else {
                KasparovInstance instance = new KasparovInstance(gameID, this);
                instances.put(gameID, instance);

                instance.addPlayer(conn);
            }
        } else if (msgType.equals("Move")) {
            KasparovInstance instance = instances.get(gameID);

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