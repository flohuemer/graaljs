package com.oracle.truffle.js.nodes.decorators;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.js.nodes.JavaScriptBaseNode;
import com.oracle.truffle.js.nodes.JavaScriptNode;
import com.oracle.truffle.js.nodes.access.ObjectLiteralNode;
import com.oracle.truffle.js.nodes.access.ObjectLiteralNode.ObjectLiteralMemberNode;
import com.oracle.truffle.js.nodes.function.JSFunctionExpressionNode;
import com.oracle.truffle.js.runtime.JSContext;
import com.oracle.truffle.js.runtime.builtins.JSFunction;
import com.oracle.truffle.js.runtime.builtins.JSOrdinary;
import com.oracle.truffle.js.runtime.objects.JSOrdinaryObject;

import java.util.Set;

public class ClassMemberNode extends JavaScriptBaseNode {
    public static final ClassMemberNode[] EMPTY = {};

    @Children
    private DecoratorNode[] decorators;
    @Child
    private ClassElementDecoratorNode decoratorNode;

    ClassMemberNode(ClassElementDecoratorNode decoratorNode, DecoratorNode[] decorators) {
        this.decoratorNode = decoratorNode;
        this.decorators = decorators;
    }

    public static ClassMemberNode create(ClassElementDecoratorNode decoratorNode, DecoratorNode[] decorators) {
        return new ClassMemberNode(decoratorNode, decorators);
    }

    @ExplodeLoop
    public void executeDecorators(VirtualFrame frame, DynamicObject homeObject, JSContext context) {
        if (decorators == null) {
            return;
        }
        DynamicObject elementDescriptor = JSOrdinary.create(context);
        decoratorNode.execute(frame, homeObject);
        for (DecoratorNode decorator : decorators) {
            decoratorNode.updateElementDescriptor(elementDescriptor, context);
            DynamicObject replacement = decorator.executeDecorator(frame, elementDescriptor);
            if(decoratorNode.hasSameKind(replacement)) {
                decoratorNode.updateState(replacement);
            } else {
                String kind = (String) JSOrdinaryObject.get(replacement, "kind");
                switch (kind){
                    case "field":
                        decoratorNode.replace(ClassElementDecoratorNode.createFieldDecorator(replacement));
                        break;
                    case "method":
                        decoratorNode.replace(ClassElementDecoratorNode.createMethodDecorator(replacement));
                        break;
                    case "accessor":
                        decoratorNode.replace(ClassElementDecoratorNode.createAccessorDecorator(replacement));
                        break;
                }
                decoratorNode.execute(frame, homeObject);
            }
            elementDescriptor = replacement;
        }
        decoratorNode.createMember();
    }

    public Object executeKey(VirtualFrame frame) {
        return decoratorNode.executeKey(frame);
    }

    public Object executeValue(VirtualFrame frame, DynamicObject homeObject) {
        return decoratorNode.executeValue(frame, homeObject);
    }

    public void executeVoid(VirtualFrame frame, DynamicObject homeObject, JSContext context) {
        decoratorNode.executeVoid(frame,homeObject, context);
    }

    public boolean isField() {
        return decoratorNode.isField();
    }

    public boolean isStatic() {
        return decoratorNode.isStatic();
    }

    public boolean isAnonymousFunctionDefinition() {
        return decoratorNode.isAnonymousFunctionDefinition();
    }

    private ClassMemberNode copyUninitialized(Set<Class<? extends  Tag>> materializedTags) {
        //Have a look into elementDescriptor
        return new ClassMemberNode(ClassElementDecoratorNode.cloneUninitialized(decoratorNode, materializedTags), DecoratorNode.cloneUninitialized(decorators,materializedTags));
    }

    public static ClassMemberNode[] cloneUninitialized(ClassMemberNode[] members, Set<Class<? extends Tag>> materializedTags) {
        ClassMemberNode[] copy = members.clone();
        for (int i = 0; i < copy.length; i++) {
            copy[i] = copy[i].copyUninitialized(materializedTags);
        }
        return copy;
    }
}
