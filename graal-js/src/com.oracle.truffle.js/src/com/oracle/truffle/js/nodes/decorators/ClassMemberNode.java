package com.oracle.truffle.js.nodes.decorators;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.js.nodes.JavaScriptBaseNode;
import com.oracle.truffle.js.nodes.JavaScriptNode;
import com.oracle.truffle.js.nodes.access.ObjectLiteralNode;
import com.oracle.truffle.js.nodes.access.ObjectLiteralNode.ObjectLiteralMemberNode;
import com.oracle.truffle.js.runtime.JSContext;
import com.oracle.truffle.js.runtime.builtins.JSOrdinary;
import com.oracle.truffle.js.runtime.objects.JSOrdinaryObject;

import java.util.Set;

public class ClassMemberNode extends JavaScriptBaseNode {
    public static final ClassMemberNode[] EMPTY = {};

    @Children
    private DecoratorNode[] decorators;
    @Child
    private ObjectLiteralMemberNode member;
    @Child
    private ObjectLiteralNode elementDescriptor;

    ClassMemberNode(ObjectLiteralMemberNode member, DecoratorNode[] decorators, ObjectLiteralNode elementDescriptor) {
        this.member = member;
        this.decorators = decorators;
        this.elementDescriptor = elementDescriptor;
    }

    public static ClassMemberNode create(ObjectLiteralMemberNode member, DecoratorNode[] decorators, ObjectLiteralNode elementDescriptor) {
        return new ClassMemberNode(member, decorators, elementDescriptor);
    }

    @ExplodeLoop
    public void executeDecorators(VirtualFrame frame) {
        if (decorators == null) {
            return;
        }
        DynamicObject e = elementDescriptor.execute(frame);
        for (DecoratorNode decorator : decorators) {
            DynamicObject curr = decorator.executeDecorator(frame, e);
            if(e.getShape().equals(curr.getShape())) {
                e = curr;
            }
        }
    }

    public Object executeKey(VirtualFrame frame) {
        return member.executeKey(frame);
    }

    public Object executeValue(VirtualFrame frame, DynamicObject homeObject) {
        return member.executeValue(frame, homeObject);
    }

    public void executeVoid(VirtualFrame frame, DynamicObject homeObject, JSContext context) {
        member.executeVoid(frame, homeObject, context);
    }

    public boolean isField() {
        return member.isField();
    }

    public boolean isStatic() {
        return member.isStatic();
    }

    public boolean isAnonymousFunctionDefinition() {
        return member.isAnonymousFunctionDefinition();
    }

    private ClassMemberNode copyUninitialized(Set<Class<? extends  Tag>> materializedTags) {
        return new ClassMemberNode(ObjectLiteralMemberNode.cloneUninitialized(member, materializedTags), DecoratorNode.cloneUninitialized(decorators,materializedTags),ObjectLiteralNode.cloneUninitialized(elementDescriptor,materializedTags));
    }

    public static ClassMemberNode[] cloneUninitialized(ClassMemberNode[] members, Set<Class<? extends Tag>> materializedTags) {
        ClassMemberNode[] copy = members.clone();
        for (int i = 0; i < copy.length; i++) {
            copy[i] = copy[i].copyUninitialized(materializedTags);
        }
        return copy;
    }
}
