package com.oracle.truffle.js.builtins.tictactoe;

import com.oracle.truffle.js.builtins.JSBuiltinsContainer;
import com.oracle.truffle.js.nodes.function.JSBuiltin;
import com.oracle.truffle.js.runtime.JSContext;
import com.oracle.truffle.js.runtime.builtins.BuiltinEnum;
import com.oracle.truffle.js.runtime.builtins.tictactoe.JSTicTacToe;

public class TicTacToeBuiltins extends JSBuiltinsContainer.SwitchEnum<TicTacToeBuiltins.TicTacToe> {

    public static final JSBuiltinsContainer BUILTINS = new TicTacToeBuiltins();

    protected TicTacToeBuiltins() { super(JSTicTacToe.CLASS_NAME, TicTacToe.class); }

    public enum TicTacToe implements BuiltinEnum<TicTacToe> {
        move(1),
        check(1);

        private final int length;

        TicTacToe(int length) { this.length = length; }

        @Override
        public int getLength() { return length; }
    }

    @Override
    protected Object createNode(JSContext context, JSBuiltin builtin, boolean construct, boolean newTarget, TicTacToe builtinEnum) {
        switch (builtinEnum) {
            case move:
                return MoveNodeGen.create(context,builtin, args().fixedArgs(1).createArgumentNodes(context));
            case check:
                return CheckNodeGen.create(context, builtin, args().fixedArgs(1).createArgumentNodes(context));
        }
        return null;
    }
}
