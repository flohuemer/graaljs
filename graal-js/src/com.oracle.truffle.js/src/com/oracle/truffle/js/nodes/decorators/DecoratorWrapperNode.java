package com.oracle.truffle.js.nodes.decorators;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.js.nodes.JavaScriptNode;

public class DecoratorWrapperNode extends JavaScriptNode {
    private Object arg0;

    public DecoratorWrapperNode(Object arg0) {
        this.arg0 = arg0;
    }

    @Override
    public Object execute(VirtualFrame frame) {
        return arg0;
    }

    public void setArg0(Object arg0) {
        this.arg0 = arg0;
    }
}
