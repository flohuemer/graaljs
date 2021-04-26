package com.oracle.truffle.js.runtime.builtins.tictactoe;

import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.js.builtins.tictactoe.TicTacToeBuiltins;
import com.oracle.truffle.js.runtime.JSRealm;
import com.oracle.truffle.js.runtime.builtins.JSOrdinary;
import com.oracle.truffle.js.runtime.objects.JSObjectUtil;

public class JSTicTacToe {

    public static final String CLASS_NAME = "TicTacToe";

    private JSTicTacToe() { }

    public static DynamicObject create(JSRealm realm) {
        DynamicObject obj = JSOrdinary.createInit(realm);
        JSObjectUtil.putToStringTag(obj, CLASS_NAME);

        JSObjectUtil.putFunctionsFromContainer(realm, obj, TicTacToeBuiltins.BUILTINS);
        return obj;
    }
}
