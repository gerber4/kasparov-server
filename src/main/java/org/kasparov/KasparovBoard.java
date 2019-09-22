package org.kasparov;

import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;
import com.github.bhlangonijr.chesslib.move.MoveGenerator;
import com.github.bhlangonijr.chesslib.move.MoveGeneratorException;
import com.github.bhlangonijr.chesslib.move.MoveList;

import java.util.HashSet;

class KasparovBoard {

    private Board board;

    private HashSet<Move> legalMoves;

    KasparovBoard() {
        this.board = new Board();

        try {
            MoveList moveList = MoveGenerator.generateLegalMoves(board);
            this.legalMoves = new HashSet<>(moveList);
        } catch (MoveGeneratorException e) {
            //squash
        }
    }

    /**
     * Get a simplified SAN string representation of the current board stored as a 2D array
     */
    char[][] getAsArray() {
        char[] sanArray = board.toString().replace("\n", "").replace("\r", "").substring(0, 64).toCharArray();
        char[][] resultArray = new char[8][8];

        for (int row = 0; row < 8; row++) {
            System.arraycopy(sanArray, 8 * (7 - row), resultArray[row], 0, 8);
        }

        return resultArray;
    }

    Move getRandomMove() {
        return legalMoves.iterator().next();
    }

    boolean isMoveLegal(Move move) {
        return legalMoves.contains(move);
    }

    boolean isMated() {
        return board.isMated();
    }

    boolean isDraw() {
        return board.isDraw();
    }

    void doMove(Move move) {
        board.doMove(move);

        try {
            MoveList moveList = MoveGenerator.generateLegalMoves(board);
            this.legalMoves = new HashSet<>(moveList);
        } catch (MoveGeneratorException e) {
            //squash
        }
    }
}
