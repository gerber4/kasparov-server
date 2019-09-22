package org.kasparov;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveList;
import org.java_websocket.WebSocket;

/**
 * Class representing the desired move of a player
 */
public class KasparovMove {
    private WebSocket uuid;

    private Move move;

    KasparovMove(WebSocket uuid, String san, KasparovGameState state) {
        this.uuid = uuid;

        //This statement allows an illegal state, but in cases of illegal state, the results are dropped
        Side side = (state == KasparovGameState.White) ? Side.WHITE : Side.BLACK;

        this.move = new Move(san, side);
    }

    public static void main(String[] args) {
        Board board = new Board();
        Move move = new Move("a2b3", Side.WHITE);

        try {
            MoveList moveList = MoveGenerator.generateLegalMoves(board);
            System.out.println(moveList.contains(move));
            System.out.println(moveList);
            System.out.println(board.isMoveLegal(move, false));
        } catch (Exception e) {
            //squash
        }

    }

    WebSocket getUuid() {
        return uuid;
    }

    Move getMove() {
        return move;
    }

}
