package org.kasparov;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Timer implements Runnable {

    private final Lock lock;

    private KasparovVsTheWorld engine;

    Timer(KasparovVsTheWorld engine) {
        this.lock = new ReentrantLock();
        this.engine = engine;
    }

    Lock getLock() {
        return lock;
    }

    @Override
    public void run() {
        while (!engine.startGame()) {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                //squash
            }
        }

        //Loop containing logic for the cycle of black and white turns
        while (!engine.isEnded()) {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    //squash
                }
            }

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                //squash
            }

            try {
                engine.setTurnWhite();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            //squash
        }

        engine.endGame();
    }
}
