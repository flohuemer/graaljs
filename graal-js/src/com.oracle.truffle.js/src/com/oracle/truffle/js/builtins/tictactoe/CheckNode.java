package com.oracle.truffle.js.builtins.tictactoe;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.js.nodes.function.JSBuiltin;
import com.oracle.truffle.js.runtime.JSContext;

public abstract class CheckNode extends TicTacToeNode{
    public CheckNode(JSContext context, JSBuiltin builtin) {
        super(context, builtin);
    }

    @Specialization
    protected int check(Object boardState) {
        int[] board = getBoard(boardState);
        return SearchTree.getWinner(board);
    }
}
