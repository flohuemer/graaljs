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
    private ObjectLiteralMemberNode member;
    private Object value;
    private Object key;

    ClassMemberNode(ObjectLiteralMemberNode member, DecoratorNode[] decorators) {
        this.member = member;
        this.decorators = decorators;
    }

    public static ClassMemberNode create(ObjectLiteralMemberNode member, DecoratorNode[] decorators) {
        return new ClassMemberNode(member, decorators);
    }

    @ExplodeLoop
    public void executeDecorators(VirtualFrame frame, DynamicObject homeObject, JSContext context) {
        if (decorators == null) {
            return;
        }
        value = member.executeValue(frame, homeObject);
        key = member.executeKey(frame);
        DynamicObject e = buildElementDescriptor(frame, homeObject, context);
        for (DecoratorNode decorator : decorators) {
            DynamicObject curr = decorator.executeDecorator(frame, e);
            e = curr;
        }
        value = JSOrdinaryObject.get(e,"method");
        member.replace(ObjectLiteralNode.newDataMember(key,member.isStatic(),0, new DummyFunction(value)));
    }

    private DynamicObject buildElementDescriptor(VirtualFrame frame, DynamicObject homeObject, JSContext context) {
        DynamicObject obj = JSOrdinary.create(context);
        DynamicObject desc = JSOrdinary.create(context);

        //desc
        JSOrdinaryObject.set(desc,"value", "Descriptor");
        JSOrdinaryObject.set(desc,"writable",false);
        JSOrdinaryObject.set(desc,"enumerable", false);
        JSOrdinaryObject.set(desc, "configurable", true);

        JSOrdinaryObject.set(obj, "desc",desc);

        //kind
        if(member.isField()) {
            JSOrdinaryObject.set(obj,"kind","field");
        } /*else if(member.isMethod()) {
            JSOrdinaryObject.set(obj,"kind","method");
        } else {
            assert (member.isAccessor());
            JSOrdinaryObject.set(obj,"kind","accessor");
        }*/

        //value
        JSOrdinaryObject.set(obj, "method", value);
        /*if(member.getValue() != null) {
            JSOrdinaryObject.set(obj, "method", transformFunction(member.getValue()));
        }*/
        return obj;
    }


    public Object executeKey(VirtualFrame frame) {
        return key;
    }

    public Object executeValue(VirtualFrame frame, DynamicObject homeObject) {
        return value;
    }

    public void executeVoid(VirtualFrame frame, DynamicObject homeObject, JSContext context) {
        member.executeVoid(frame,homeObject, context);
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
        //Have a look into elementDescriptor
        return new ClassMemberNode(ObjectLiteralMemberNode.cloneUninitialized(member, materializedTags), DecoratorNode.cloneUninitialized(decorators,materializedTags));
    }

    public static ClassMemberNode[] cloneUninitialized(ClassMemberNode[] members, Set<Class<? extends Tag>> materializedTags) {
        ClassMemberNode[] copy = members.clone();
        for (int i = 0; i < copy.length; i++) {
            copy[i] = copy[i].copyUninitialized(materializedTags);
        }
        return copy;
    }
}
