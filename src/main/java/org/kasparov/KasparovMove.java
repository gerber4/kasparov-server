package org.kasparov;

import com.github.bhlangonijr.chesslib.move.Move;
import org.java_websocket.WebSocket;

/**
 * Class representing the desired move of a player
 */
public class KasparovMove {
    private WebSocket uuid;

    private Move move;

    public KasparovMove(WebSocket uuid, Move move) {
        this.uuid = uuid;
        this.move = move;
    }

    public WebSocket getUuid() {
        return uuid;
    }

    public Move getMove() {
        return move;
    }
}
