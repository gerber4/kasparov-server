package org.kasparov;

import com.github.bhlangonijr.chesslib.Board;

import java.net.InetSocketAddress;
import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Main class of the Kasparov vs. the world game.
 */
public class KasparovEngine implements Runnable {

    private Board board;

    private KasparovServer server;

    private KasparovTimer timer;

    private KasparovGameState state;

    private BlockingQueue<KasparovMove> moveQueue;

    private HashMap<WebSocket, KasparovMove> moves;

    private KasparovEngine() {
        this.state = KasparovGameState.Setup;
        this.board = new Board();
        this.moveQueue = new LinkedBlockingQueue<>();
        this.moves = new HashMap<>();
    }

    KasparovGameState getState() {
        return state;
    }

    /**
     * Get a simplified SAN string representing the current board stored in a 2D array
     */
    char[][] getBoard() {
        String board = this.board.toString();
        board = board.replace("\n", "").replace("\r", "");
        board = board.substring(0, 64);
        char[] boardArray = board.toCharArray();
        char[][] resultArray = new char[8][8];

        for (int row = 0; row < 8; row++) {
            System.arraycopy(boardArray, 8 * (7 - row), resultArray[row], 0, 8);
        }

        return resultArray;
    }

    /**
     * Method for selection a move for kasparov. Immediately ends turn if move is legal
     */
    void makeMoveKasparov(KasparovMove move) {
        if (state == KasparovGameState.White) {
            boolean success = false;

            try {
                success = board.isMoveLegal(move.getMove(), true);
            } catch (RuntimeException e) {
                //squash
            }

            if (success) {
                setTurnBlack(move);
            }
        }
    }

    /**
     * Method for selecting a move for a non-kasparov player
     */
    void makeMoveWorld(WebSocket uuid, KasparovMove move) {
        if (state == KasparovGameState.Black) {
            moves.put(uuid, move);
        }
    }


    /**
     * Transitions from the setup state to the White state
     *
     * @return true if game started
     */
    boolean startGame() {
        if (server.getKasparov() == null) {
            return false;
        } else if (server.getPlayers().isEmpty()) {
            return false;
        } else {
            state = KasparovGameState.White;
            server.setState(state);

            return true;
        }
    }

    /**
     * Transition from the white turn to the black turn
     */
    private void setTurnBlack(KasparovMove move) {
        if (state != KasparovGameState.White) {
            throw new IllegalStateException("setTurnBlack called when state is not white");
        }

        board.doMove(move.getMove());

        if (board.isMated()) {
            setWhiteWins();
        } else if (board.isDraw()) {
            setStalemate();
        } else {
            state = KasparovGameState.Black;
            server.setState(state);
            server.setBoard(getBoard());
            moveQueue = new LinkedBlockingQueue<>();
            moves = new HashMap<>();
        }
    }

    /**
     * Transition from white turn to white wins
     */
    private void setWhiteWins() {
        if (state != KasparovGameState.White) {
            throw new IllegalStateException("setWhiteWins called when state is not white");
        }

        state = KasparovGameState.WhiteWins;
    }

    /**
     * Transition to stalemate state
     */
    private void setStalemate() {
        state = KasparovGameState.Stalemate;
    }

    /**
     * Transition from black turn to black wins
     */
    private void setBlackWins() {
        if (state != KasparovGameState.Black) {
            throw new IllegalStateException("setBlackWins called when state is not black");
        }

        state = KasparovGameState.BlackWins;
    }

    @Override
    public void run() {
        String host = "localhost";
        int port = 8887;
        server = new KasparovServer(new InetSocketAddress(host, port), this);

        Thread serverThread = new Thread(server);
        serverThread.start();

        timer  = new KasparovTimer(this);

        Thread timerThread = new Thread(timer);
        timerThread.start();

    }

    public static void main(String[] args) {
        KasparovEngine engine = new KasparovEngine();
        Thread thread = new Thread(engine);
        thread.start();
    }
}
