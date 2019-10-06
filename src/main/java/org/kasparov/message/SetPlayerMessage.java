package org.kasparov.message;

import org.kasparov.PlayerType;

public class SetPlayerMessage extends Message{
    private SetPlayerContent msg;

    public SetPlayerMessage(PlayerType player){
        super("SetPlayer");
        this.msg = new SetPlayerContent(player);
    }

    public SetPlayerContent getMsg() {
        return msg;
    }
}

class SetPlayerContent{
    private final PlayerType player;

    SetPlayerContent(PlayerType player){
        this.player = player;
    }

    public PlayerType getPlayer() {
        return player;
    }
}