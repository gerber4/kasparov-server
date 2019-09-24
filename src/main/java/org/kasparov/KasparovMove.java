package org.kasparov;

import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import org.java_websocket.WebSocket;

/**
 * Class representing the desired move of a player
 */
class KasparovMove {
    private WebSocket player;

    private Move move;

    KasparovMove(WebSocket uuid, String SAN, KasparovGameState state) {
        this.player = uuid;

        Side side = (state == KasparovGameState.White) ? Side.WHITE : Side.BLACK;

        this.move = new Move(SAN, side);
    }

    WebSocket getPlayer() {
        return player;
    }

    Move getMove() {
        return move;
    }

}
