package org.kasparov.message;

import org.kasparov.GameState;

public class SetStateMessage extends Message{
    private SetStateContent msg;

    public SetStateMessage(GameState state){
        super("SetState");
        this.msg = new SetStateContent(state);
    }

    public SetStateContent getMsg() {
        return msg;
    }
}

class SetStateContent {
    private final GameState state;

    SetStateContent(GameState state) {
        this.state = state;
    }

    public GameState getState() {
        return state;
    }
}