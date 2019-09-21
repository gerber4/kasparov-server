package org.kasparov;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;

import com.google.gson.Gson;
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

    void setBoard(char[][] pieceArray) {
        String pieceJSON = new Gson().toJson(pieceArray);
        String message = String.format("{\"msgType\": \"SetBoard\", \"msg\": {\"board\": \"%s\"}}", pieceJSON);
        broadcast(message);
    }

    void setBoard(WebSocket conn, char[][] pieceArray) {
        String pieceJSON = new Gson().toJson(pieceArray);
        String message = String.format("{\"msgType\": \"SetBoard\", \"msg\": {\"board\": %s}}", pieceJSON);
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
    public void onMessage(WebSocket conn, String message) {
        System.out.println("received message from "	+ conn.getRemoteSocketAddress() + ": " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("an error occurred on connection " + conn.getRemoteSocketAddress()  + ":" + ex);
    }

    @Override
    public void onStart() {
    }



}