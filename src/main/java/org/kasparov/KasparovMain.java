package org.kasparov;

import java.net.InetSocketAddress;

public class KasparovMain {
    public static void main(String[] args) {
        String host = "0.0.0.0";
        int port = 8887;
        KasparovServer server = new KasparovServer(new InetSocketAddress(host, port));

        Thread thread = new Thread(server);
        thread.start();
    }
}
