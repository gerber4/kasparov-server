package org.kasparov;

import org.java_websocket.WebSocket;

import org.kasparov.message.Message;

import java.util.Collection;


public interface MessageHandler {
    void sendMessage(Message message, WebSocket player);

    void sendMessage(Message message, Collection<WebSocket> players);

    void endGame(Integer gameId, WebSocket kasparov, Collection<WebSocket> players);
}
