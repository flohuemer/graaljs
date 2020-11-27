package com.oracle.truffle.js.nodes.decorators;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.js.nodes.JavaScriptNode;

public class DecoratorNode extends JavaScriptNode {
    @Child private JavaScriptNode expression;

    protected DecoratorNode(JavaScriptNode expression) {
        this.expression = expression;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return null;
    }
}
