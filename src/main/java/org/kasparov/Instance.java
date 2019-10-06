package org.kasparov;

import org.java_websocket.WebSocket;
import org.kasparov.message.*;

import java.util.Collection;
import java.util.HashSet;

/**
 * Class managing all the state of an instance of kasparov
 */
class Instance {

    private Integer gameId;

    private MessageHandler handler;

    private KasparovVsTheWorld engine;

    private WebSocket kasparov;

    private Collection<WebSocket> players;

    Instance(Integer gameId, MessageHandler handler) {
        this.gameId = gameId;
        this.handler = handler;
        this.players = new HashSet<>();

        this.engine = new KasparovVsTheWorld(this);
        Thread engineThread = new Thread(this.engine);
        engineThread.start();
    }

    WebSocket getKasparov() {
        return kasparov;
    }

    Collection<WebSocket> getPlayers() {
        return players;
    }

    void setState(GameState state) {
        Message message = new SetStateMessage(state);
        handler.sendMessage(message, kasparov);
        handler.sendMessage(message, players);
    }

    void setBoard(char[][] board) {
        Message message = new SetBoardMessage(board);
        handler.sendMessage(message, kasparov);
        handler.sendMessage(message, players);
    }

    void addPlayer(WebSocket player) {
        if (kasparov == null) {
            kasparov = player;
            handler.sendMessage(new SetPlayerMessage(PlayerType.Kasparov), player);
        } else {
            players.add(player);
            handler.sendMessage(new SetPlayerMessage(PlayerType.World), player);
        }

        handler.sendMessage(new InstanceConnectedMessage(), player);
        handler.sendMessage(new SetStateMessage(engine.getState()), player);
        handler.sendMessage(new SetBoardMessage(engine.getBoard()), player);
    }

    void makeMove(WebSocket player, String SAN) {
        GameState state = engine.getState();

        KasparovMove move = new KasparovMove(player, SAN, state);
        
        if (player == kasparov) {
            engine.makeMoveKasparov(move);
        } else {
            engine.makeMoveWorld(player, move);
        }
    }
    
    void endGame() {
        handler.endGame(gameId, kasparov, players);
    }
}
