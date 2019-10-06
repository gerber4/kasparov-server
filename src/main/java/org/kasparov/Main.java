package org.kasparov;

import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) {
        String host = "0.0.0.0";
        int port = 8080;
        Server server = new Server(new InetSocketAddress(host, port));
        server.setConnectionLostTimeout( 0 );

        Thread thread = new Thread(server);
        thread.start();
    }
}
