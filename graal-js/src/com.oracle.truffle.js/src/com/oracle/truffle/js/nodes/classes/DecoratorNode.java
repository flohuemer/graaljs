package com.oracle.truffle.js.nodes.classes;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.js.nodes.JavaScriptNode;
import com.oracle.truffle.js.nodes.function.JSFunctionExpressionNode;

public class DecoratorNode extends JavaScriptNode {
    @Child private final JavaScriptNode expression;

    protected DecoratorNode(JavaScriptNode expression) {
        this.expression = expression;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return null;
    }
}
