package org.kasparov;

import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import org.java_websocket.WebSocket;

/**
 * Class representing the desired move of a player
 */
public class KasparovMove {
    private WebSocket uuid;

    private Move move;

    public KasparovMove(WebSocket uuid, String san, KasparovGameState state) {
        this.uuid = uuid;

        //This statement allows an illegal state, but in cases of illegal state, the results are dropped
        Side side = (state == KasparovGameState.White) ? Side.WHITE : Side.BLACK;

        this.move = new Move(san, side);
    }

    public WebSocket getUuid() {
        return uuid;
    }

    public Move getMove() {
        return move;
    }

}
