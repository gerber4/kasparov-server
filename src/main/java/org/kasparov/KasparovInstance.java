package org.kasparov;

import org.java_websocket.WebSocket;

import java.util.Collection;
import java.util.HashSet;

/**
 * Class managing all the state of an instance of kasparov
 */
class KasparovInstance {

    private Integer gameId;

    private KasparovServer server;

    private KasparovEngine engine;

    private WebSocket kasparov;

    private Collection<WebSocket> players;

    KasparovInstance(Integer gameId, KasparovServer server) {
        this.gameId = gameId;
        this.server = server;
        this.players = new HashSet<>();

        this.engine = new KasparovEngine(this);
        Thread engineThread = new Thread(this.engine);
        engineThread.start();
    }

    WebSocket getKasparov() {
        return kasparov;
    }

    Collection<WebSocket> getPlayers() {
        return players;
    }

    void setState(KasparovGameState state) {
        server.setState(state, kasparov);
        server.setState(state, players);
    }

    void setBoard(char[][] board) {
        server.setBoard(board, kasparov);
        server.setBoard(board, players);
    }

    void addPlayer(WebSocket player) {
        if (kasparov == null) {
            kasparov = player;
            server.setAsKasparov(kasparov);
        } else {
            players.add(player);
            server.setAsWorld(player);
        }

        server.setConnected(player);
        server.setState(engine.getState(), player);
        server.setBoard(engine.getBoard(), player);
    }

    void makeMove(WebSocket player, String SAN) {
        KasparovGameState state = engine.getState();

        KasparovMove move = new KasparovMove(player, SAN, state);
        
        if (player == kasparov) {
            engine.makeMoveKasparov(move);
        } else {
            engine.makeMoveWorld(player, move);
        }
    }
    
    void endGame() {
        server.endGame(gameId, kasparov, players);
    }
}
