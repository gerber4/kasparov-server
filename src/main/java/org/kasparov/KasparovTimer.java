package org.kasparov;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class KasparovTimer implements Runnable {

    private final Lock lock;

    private KasparovEngine engine;

    KasparovTimer(KasparovEngine engine) {
        this.lock = new ReentrantLock();
        this.engine = engine;
    }

    @Override
    public void run() {

        while (!engine.startGame()) {
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                //squash
            }
        }



        System.out.println("We made it!");
    }
}
