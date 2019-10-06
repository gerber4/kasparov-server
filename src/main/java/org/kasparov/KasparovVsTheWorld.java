package org.kasparov;

import com.github.bhlangonijr.chesslib.move.Move;
import org.java_websocket.WebSocket;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Logic Kasparov vs. the world game.
 */
public class KasparovVsTheWorld implements Runnable {

    private Instance instance;

    private Timer timer;

    private GameState state;

    private KasparovBoard board;

    private BlockingQueue<KasparovMove> moveQueue;

    private HashMap<WebSocket, KasparovMove> moves;

    KasparovVsTheWorld(Instance instance) {
        this.instance = instance;
        this.state = GameState.Setup;
        this.board = new KasparovBoard();
        this.moves = new HashMap<>();
        this.moveQueue = new LinkedBlockingQueue<>();
    }

    boolean isEnded() {
        return state == GameState.WhiteWins || state == GameState.BlackWins || state == GameState.Stalemate;
    }

    GameState getState() {
        return state;
    }

    char[][] getBoard() {
        return board.getAsArray();
    }

    /**
     * Method for selection a move for kasparov. Immediately ends turn if move is legal
     */
    void makeMoveKasparov(KasparovMove move) {
        if (state == GameState.White) {
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
        if (state == GameState.Black) {
            moves.put(uuid, move);
        }
    }

    /**
     * Transitions from the setup state to the White state
     *
     * @return true if game started, false if game not ready to start
     */
    boolean startGame() {
        if (instance.getKasparov() == null) {
            return false;
        } else if (instance.getPlayers().isEmpty()) {
            return false;
        } else {
            state = GameState.White;
            instance.setState(state);
            return true;
        }
    }

    /**
     * Transition from the white turn to the black turn
     */
    private void setTurnBlack(KasparovMove move) {
        if (state != GameState.White) {
            throw new IllegalStateException("setTurnBlack called when state is not white");
        }

        board.doMove(move.getMove());

        if (board.isMated()) {
            setWhiteWins();
        } else if (board.isDraw()) {
            setStalemate();
        } else {
            state = GameState.Black;
            instance.setState(state);
            instance.setBoard(board.getAsArray());
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

        Optional<Map.Entry<Move, Long>> optionalMove = moves.stream()
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
            setBlackWins();
        } else if (board.isDraw()) {
            setStalemate();
        } else {
            state = GameState.White;
            instance.setState(state);
            instance.setBoard(board.getAsArray());
        }

    }

    /**
     * Transition from white turn to white wins
     */
    private void setWhiteWins() {
        if (state != GameState.White) {
            throw new IllegalStateException("setWhiteWins called when state is not white");
        }
        state = GameState.WhiteWins;
        instance.setState(state);
        instance.setBoard(board.getAsArray());
    }

    /**
     * Transition from black turn to black wins
     */
    private void setBlackWins() {
        if (state != GameState.Black) {
            throw new IllegalStateException("setBlackWins called when state is not black");
        }
        state = GameState.BlackWins;
        instance.setState(state);
        instance.setBoard(board.getAsArray());
    }

    /**
     * Transition to the stalemate state
     */
    private void setStalemate() {
        state = GameState.Stalemate;
        instance.setState(state);
        instance.setBoard(board.getAsArray());
    }


    void endGame() {
        instance.endGame();
    }


    /**
     * Start the timer thread, and then consume all incoming moves
     */
    @Override
    public void run() {
        timer = new Timer(this);

        Thread timerThread = new Thread(timer);
        timerThread.start();

        try {
            while (!isEnded()) {
                KasparovMove move = moveQueue.take();

                if (state == GameState.Black) {
                    if (board.isMoveLegal(move.getMove())) {
                        moves.put(move.getPlayer(), move);
                    }
                }
            }
        } catch (InterruptedException e) {
            //squash
        }
    }
}
