package org.kasparov;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;


/**
 * Server class managing the connection between the server and the players
 */
public class KasparovServer extends WebSocketServer {

    private KasparovEngine engine;

    private WebSocket kasparov;

    private Collection<WebSocket> players;

    KasparovServer(InetSocketAddress address, KasparovEngine engine) {
        super(address);
        this.engine = engine;
        this.players = new HashSet<>();
    }

    WebSocket getKasparov() {
        return kasparov;
    }

    Collection<WebSocket> getPlayers() {
        return players;
    }

    void setState(KasparovGameState state) {
        String message = String.format("{\"msgType\": \"SetState\", \"msg\": {\"state\": \"%s\"}}", state.toString());
        broadcast(message);
    }

    void setState(WebSocket conn, KasparovGameState state) {
        String message = String.format("{\"msgType\": \"SetState\", \"msg\": {\"state\": \"%s\"}}", state.toString());
        conn.send(message);
    }

    void setBoard(char[][] boardArray) {
        String boardJSON = new Gson().toJson(boardArray);
        String message = String.format("{\"msgType\": \"SetBoard\", \"msg\": {\"board\": %s}}", boardJSON);
        broadcast(message);
    }

    void setBoard(WebSocket conn, char[][] boardArray) {
        String boardJSON = new Gson().toJson(boardArray);
        String message = String.format("{\"msgType\": \"SetBoard\", \"msg\": {\"board\": %s}}", boardJSON);
        conn.send(message);
    }


    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        System.out.println("new connection to " + conn.getRemoteSocketAddress());
        if (kasparov == null) {
            kasparov = conn;
        } else {
            players.add(conn);
        }

        setState(conn, engine.getState());
        setBoard(conn, engine.getBoard());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
        if (kasparov == conn) {
            throw new IllegalStateException("RIP KASPAROV QUIT :,(");
        } else {
            players.remove(conn);
        }
    }

    @Override
    public void onMessage(WebSocket conn, String messageString) {
        System.out.println("received message from "	+ conn.getRemoteSocketAddress() + ": " + messageString);
        JsonObject message = new Gson().fromJson(messageString, JsonObject.class);

        String msgType = message.get("msgType").getAsString();
        switch (msgType) {

            case "SendMove":
                String san = message.get("msg").getAsJsonObject().get("move").getAsString();
                KasparovGameState state = engine.getState();
                KasparovMove move = new KasparovMove(conn, san, state);

                if (conn == kasparov) {
                    engine.makeMoveKasparov(move);
                } else {
                    engine.makeMoveWorld(conn, move);
                }
                break;

            default:
                throw new IllegalStateException("Unknown msgType: " + msgType);

        }

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("an error occurred on connection " + conn.getRemoteSocketAddress()  + ":" + ex);
    }

    @Override
    public void onStart() {
    }



}