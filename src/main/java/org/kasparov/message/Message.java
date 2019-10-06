package org.kasparov.message;

public abstract class Message {
    private final String msgType;

    //TODO create factory method for messages

    Message(String msgType){
        this.msgType = msgType;
    }

    public String getMsgType() {
        return msgType;
    }
}







