package org.kasparov.message;

public class SetBoardMessage extends Message{
    private SetBoardContent msg;

    public SetBoardMessage(char[][] board){
        super("SetBoard");
        this.msg = new SetBoardContent(board);
    }

    public SetBoardContent getMsg() {
        return msg;
    }
}

class SetBoardContent{
    private final char[][] board;

    SetBoardContent(char[][] board){
        this.board = board;
    }

    public char[][] getBoard() {
        return board;
    }
}