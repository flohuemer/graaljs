package com.oracle.truffle.js.nodes.decorators;

import com.oracle.js.parser.ir.CallNode;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.js.builtins.FunctionPrototypeBuiltinsFactory;
import com.oracle.truffle.js.nodes.JavaScriptBaseNode;
import com.oracle.truffle.js.nodes.JavaScriptNode;
import com.oracle.truffle.js.nodes.access.ObjectLiteralNode;
import com.oracle.truffle.js.nodes.access.ObjectLiteralNode.ObjectLiteralMemberNode;
import com.oracle.truffle.js.nodes.function.JSFunctionCallNode;
import com.oracle.truffle.js.nodes.interop.JSInteropCallNode;
import com.oracle.truffle.js.runtime.AbstractJavaScriptLanguage;
import com.oracle.truffle.js.runtime.JSContext;
import com.oracle.truffle.js.runtime.builtins.JSFunction;
import com.oracle.truffle.js.runtime.builtins.JSFunctionObject;
import com.oracle.truffle.js.runtime.objects.JSOrdinaryObject;

import java.util.Set;

public class ClassMemberNode extends JavaScriptBaseNode {
    public static final ClassMemberNode[] EMPTY = {};

    @Children
    JavaScriptNode[] decorators;
    @Child
    ObjectLiteralMemberNode member;
    @Child
    ObjectLiteralNode memberInfo;

    ClassMemberNode(ObjectLiteralMemberNode member, JavaScriptNode[] decorators, ObjectLiteralNode memberInfo) {
        this.member = member;
        this.decorators = decorators;
        this.memberInfo = memberInfo;
    }

    public static ClassMemberNode create(ObjectLiteralMemberNode member, JavaScriptNode[] decorators, ObjectLiteralNode memberInfo) {
        return new ClassMemberNode(member, decorators, memberInfo);
    }

    @ExplodeLoop
    public void executeDecorators(VirtualFrame frame) {
        if (decorators == null) {
            return;
        }
        JavaScriptNode target = null;
        JavaScriptNode[] arguments = new JavaScriptNode[1];
        arguments[0] = memberInfo;
        for (int i = decorators.length - 1; i >= 0; i--) {
            JavaScriptNode d = decorators[i];
            JSFunctionCallNode call = JSFunctionCallNode.createCall(d,target,arguments,false,false);
            JSOrdinaryObject o = (JSOrdinaryObject)call.execute(frame);
            int j = 0;
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
        return new ClassMemberNode(ObjectLiteralMemberNode.cloneUninitialized(member, materializedTags), JavaScriptNode.cloneUninitialized(decorators,materializedTags),ObjectLiteralNode.cloneUninitialized(memberInfo,materializedTags));
    }

    public static ClassMemberNode[] cloneUninitialized(ClassMemberNode[] members, Set<Class<? extends Tag>> materializedTags) {
        ClassMemberNode[] copy = members.clone();
        for (int i = 0; i < copy.length; i++) {
            copy[i] = copy[i].copyUninitialized(materializedTags);
        }
        return copy;
    }
}
