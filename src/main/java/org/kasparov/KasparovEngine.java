package org.kasparov;

import com.github.bhlangonijr.chesslib.move.*;
import org.java_websocket.WebSocket;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Main class of the Kasparov vs. the world game.
 */
public class KasparovEngine implements Runnable {

    private KasparovBoard board;

    private KasparovServer server;

    private KasparovTimer timer;

    private KasparovGameState state;

    private BlockingQueue<KasparovMove> moveQueue;

    private HashMap<WebSocket, KasparovMove> moves;

    private KasparovEngine() {
        this.state = KasparovGameState.Setup;
        this.board = new KasparovBoard();
        this.moveQueue = new LinkedBlockingQueue<>();
        this.moves = new HashMap<>();
    }

    KasparovGameState getState() {
        return state;
    }

    char[][] getBoard() {
        return board.getAsArray();
    }

    boolean isEnded() {
        return state == KasparovGameState.WhiteWins || state == KasparovGameState.BlackWins || state == KasparovGameState.Stalemate;
    }

    /**
     * Method for selection a move for kasparov. Immediately ends turn if move is legal
     */
    void makeMoveKasparov(KasparovMove move) {
        if (state == KasparovGameState.White) {
            if (board.isMoveLegal(move.getMove())) {
                setTurnBlack(move);
            } else {
                System.out.println("Kasparov sent illegal move: " + move.getMove());
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
     * @return true if game started, false if game not ready to start
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
            System.out.println("THIS SHOULD BE HIT?!?!?");
            setWhiteWins();
        } else if (board.isDraw()) {
            setStalemate();
        } else {
            state = KasparovGameState.Black;
            server.setState(state);
            server.setBoard(board.getAsArray());
        }

        this.moveQueue = new LinkedBlockingQueue<>();
        this.moves = new HashMap<>();

        synchronized (timer.getLock()) {
            timer.getLock().notify();
        }
    }

    /**
     * Transition from the black turn to the white turn. If the most popular move is illegal, then a random move is performed
     */
    void setTurnWhite() {
        List<Move> moves = this.moves.values()
                .stream()
                .map(KasparovMove::getMove)
                .collect(Collectors.toList());

        Optional<Map.Entry<Move,Long>> optionalMove = moves.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Comparator.comparing(Map.Entry::getValue));

        Move move;
        if (optionalMove.isPresent()) {
            if (board.isMoveLegal(optionalMove.get().getKey())) {
                move = optionalMove.get().getKey();
            } else {
                move = board.getRandomMove();
            }
        } else {
            move = board.getRandomMove();
        }

        board.doMove(move);

        if (board.isMated()) {
            System.out.println("THIS SHOULD BE HIT?!?!?");
            setBlackWins();
        } else if (board.isDraw()) {
            setStalemate();
        } else {
            state = KasparovGameState.White;
            server.setState(state);
            server.setBoard(board.getAsArray());
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
        server.setState(state);
        server.setBoard(board.getAsArray());
    }

    /**
     * Transition from black turn to black wins
     */
    private void setBlackWins() {
        if (state != KasparovGameState.Black) {
            throw new IllegalStateException("setBlackWins called when state is not black");
        }
        state = KasparovGameState.BlackWins;
        server.setState(state);
        server.setBoard(board.getAsArray());
    }

    /**
     * Transition to stalemate state
     */
    private void setStalemate() {
        state = KasparovGameState.Stalemate;
        server.setState(state);
        server.setBoard(board.getAsArray());
    }


    /**
     * Sets up all the required threads, and then manages all incoming moves
     */
    @Override
    public void run() {
        String host = "localhost";
        int port = 8887;
        server = new KasparovServer(new InetSocketAddress(host, port), this);

        Thread serverThread = new Thread(server);
        serverThread.start();

        timer = new KasparovTimer(this);

        Thread timerThread = new Thread(timer);
        timerThread.start();

        try {
            while (!isEnded()) {
                KasparovMove move = moveQueue.take();

                if (state == KasparovGameState.Black) {
                    if (board.isMoveLegal(move.getMove())) {
                        moves.put(move.getUuid(), move);
                    }
                }
            }
        } catch (InterruptedException e) {
            //squash
        }
    }

    public static void main(String[] args) {
        KasparovEngine engine = new KasparovEngine();
        Thread thread = new Thread(engine);
        thread.start();
    }
}
