package com.oracle.truffle.js.nodes.decorators;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.js.nodes.JavaScriptBaseNode;
import com.oracle.truffle.js.nodes.JavaScriptNode;
import com.oracle.truffle.js.nodes.function.JSFunctionCallNode;
import com.oracle.truffle.js.runtime.JSArguments;
import com.oracle.truffle.js.runtime.builtins.JSFunctionObject;

import java.util.Set;

public class DecoratorNode extends JavaScriptBaseNode {
    @Child
    private JavaScriptNode expressionNode;
    @Child
    private JSFunctionCallNode functionNode;

    DecoratorNode(JavaScriptNode expressionNode, JSFunctionCallNode functionNode) {
        this.expressionNode = expressionNode;
        this.functionNode = functionNode;
    }

    public static DecoratorNode create(JavaScriptNode expression, JSFunctionCallNode function) {
        return new DecoratorNode(expression, function);
    }

    public DynamicObject executeDecorator(VirtualFrame frame, DynamicObject elementDescriptor) {
        JSFunctionObject function = (JSFunctionObject) expressionNode.execute(frame);
        return (DynamicObject) functionNode.executeCall(JSArguments.createOneArg(null, function, elementDescriptor));
    }

    public static DecoratorNode[] cloneUninitialized(DecoratorNode[] decorators, Set<Class<? extends Tag>> materializedTags) {
        DecoratorNode[] copy = decorators.clone();
        for(int i = 0; i < copy.length; i++) {
            copy[i] = copy[i].copyUninitialized(materializedTags);
        }
        return copy;
    }

    private DecoratorNode copyUninitialized(Set<Class<? extends Tag>> materializedTags) {
        return new DecoratorNode(JavaScriptNode.cloneUninitialized(expressionNode, materializedTags), JSFunctionCallNode.cloneUninitialized(functionNode, materializedTags));
    }
}
