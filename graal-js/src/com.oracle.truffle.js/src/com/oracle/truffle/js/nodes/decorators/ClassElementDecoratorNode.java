package com.oracle.truffle.js.nodes.decorators;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.js.nodes.JavaScriptBaseNode;
import com.oracle.truffle.js.nodes.JavaScriptNode;
import com.oracle.truffle.js.nodes.access.ObjectLiteralNode;
import com.oracle.truffle.js.nodes.access.ObjectLiteralNode.ObjectLiteralMemberNode;
import com.oracle.truffle.js.runtime.JSContext;
import com.oracle.truffle.js.runtime.builtins.JSFunctionObject;
import com.oracle.truffle.js.runtime.builtins.JSOrdinary;
import com.oracle.truffle.js.runtime.objects.JSOrdinaryObject;
import org.graalvm.compiler.nodes.ValueNode;

import java.util.Set;

public abstract class ClassElementDecoratorNode extends JavaScriptBaseNode {
    public static final ClassElementDecoratorNode[] EMPTY = {};

    protected static final int CONFIGURABLE_STATE_TRUE = 1 << 1;
    protected static final int CONFIGURABLE_STATE_FALSE = 1 << 2;
    protected static final int ENUMERABLE_STATE_TRUE = 1 << 4;
    protected static final int ENUMERABLE_STATE_FALSE = 1 << 5;
    protected static final int WRITABLE_STATE_TRUE = 1 << 7;
    protected static final int WRITABLE_STATE_FALSE = 1 << 8;

    protected static final int PLACEMENT_STATIC = 1 << 0;
    protected static final int PLACEMENT_PROTOTYPE = 1 << 1;
    protected static final int PLACEMENT_OWN = 1 << 2;

    protected final int placement;
    protected final int propertyDescriptor;

    protected ClassElementDecoratorNode(int placement, int propertyDescriptor) {
        this.placement = placement;
        this.propertyDescriptor = propertyDescriptor;
    }

    public static ClassElementDecoratorNode createFieldDecorator(DynamicObject elementDescriptor){
        Object key = JSOrdinaryObject.get(elementDescriptor, "key");
        if(key == null) {
            //throw runtime error
        }
        DynamicObject enumerable = (DynamicObject) JSOrdinaryObject.get(elementDescriptor, "desc");
        return FieldDecoratorNode.create("test", PLACEMENT_STATIC,0,elementDescriptor);
    }

    public static ClassElementDecoratorNode createFieldDecorator(ObjectLiteralMemberNode member, int placement, int propertyDescriptor, JavaScriptNode initialize) {
        return new FieldDecoratorNode(placement,propertyDescriptor,initialize,member);
    }

    public static ClassElementDecoratorNode createMethodDecorator(DynamicObject elementDescriptor){
        return null;
    }

    public static ClassElementDecoratorNode createMethodDecorator(ObjectLiteralMemberNode member, int placement, int propertyDescriptor, JavaScriptNode value) {
        return new MethodDecoratorNode(placement,propertyDescriptor,value,member);
    }

    public static ClassElementDecoratorNode createAccessorDecorator(DynamicObject elementDescriptor) {
        return null;
    }

    public static ClassElementDecoratorNode createAccessorDecorator(ObjectLiteralMemberNode member, int placement, int propertyDescriptor, JavaScriptNode getter, JavaScriptNode setter) {
        return new AccessorDecoratorNode(placement, propertyDescriptor, getter, setter, member);
    }

    private int getPlacement(String p) {
        if(p.equals("static")) {
            return PLACEMENT_STATIC;
        }
        if(p.equals("own")){
            return PLACEMENT_OWN;
        }
        assert p.equals("prototype");
        return PLACEMENT_PROTOTYPE;
    }

    private int setEnumerable(boolean enumerable, int propertyDescriptor){
        return enumerable ? propertyDescriptor | ENUMERABLE_STATE_TRUE : propertyDescriptor | ENUMERABLE_STATE_FALSE;
    }

    private int setConfigurable(boolean configurable, int propertyDescriptor) {
        return configurable ? propertyDescriptor | CONFIGURABLE_STATE_TRUE : propertyDescriptor | CONFIGURABLE_STATE_FALSE;
    }

    private int setWritable(boolean writeable, int propertyDescriptor) {
        return writeable ? propertyDescriptor | WRITABLE_STATE_TRUE : propertyDescriptor | WRITABLE_STATE_FALSE;
    }

    protected void updateState(DynamicObject newElementDescriptor) {

    }

    protected abstract ClassElementDecoratorNode copyUninitialized(Set<Class<? extends Tag>> materializedTags);

    public void updateElementDescriptor(DynamicObject obj, JSContext context) {
        DynamicObject desc = JSOrdinary.create(context);
        JSOrdinaryObject.set(desc,"value", "Descriptor");
        JSOrdinaryObject.set(desc,"writable",false);
        JSOrdinaryObject.set(desc,"enumerable", false);
        JSOrdinaryObject.set(desc, "configurable", true);

        JSOrdinaryObject.set(obj, "desc",desc);
    }

    public boolean isStatic() {
        return (placement & PLACEMENT_STATIC) != 0;
    }

    public abstract void execute(VirtualFrame frame, DynamicObject homeObject);

    public abstract void createMember();
    public abstract boolean isField();
    public abstract boolean isAnonymousFunctionDefinition();
    public abstract boolean hasSameKind(DynamicObject elementDescriptor);
    public abstract Object executeKey(VirtualFrame frame);
    public abstract void executeVoid(VirtualFrame frame, DynamicObject homeObject, JSContext context);
    public abstract Object executeValue(VirtualFrame frame, DynamicObject homeObject);

    public static ClassElementDecoratorNode cloneUninitialized(ClassElementDecoratorNode decoratorNode, Set<Class<? extends Tag>>materializedTags){
        return decoratorNode.copyUninitialized(materializedTags);
    }

    private static class FieldDecoratorNode extends ClassElementDecoratorNode {
        @Child ObjectLiteralMemberNode member;
        @Child JavaScriptNode initialize;
        private Object initializationFunction;

        FieldDecoratorNode(int placement, int propertyDescriptor, JavaScriptNode initialize, ObjectLiteralMemberNode member) {
            super(placement,propertyDescriptor);
            this.initialize = initialize;
            this.member = member;
        }

        public static FieldDecoratorNode create(String key, int placement, int propertyDescriptor, DynamicObject elementDescriptor) {
            Object initializeFunction = JSOrdinaryObject.get(elementDescriptor,"initialize");
            return new FieldDecoratorNode(placement, propertyDescriptor,new DummyFunction(initializeFunction),null);
        }

        @Override
        protected void updateState(DynamicObject newElementDescriptor) {
            super.updateState(newElementDescriptor);
            initializationFunction = JSOrdinaryObject.get(newElementDescriptor,"initialize");
        }

        @Override
        public void updateElementDescriptor(DynamicObject obj, JSContext context) {
            super.updateElementDescriptor(obj, context);
            if(initializationFunction != null) {
                JSOrdinaryObject.set(obj, "initialize", initializationFunction);
            }
        }

        @Override
        protected ClassElementDecoratorNode copyUninitialized(Set<Class<? extends Tag>> materializedTags) {
            //TODO: clone initialize
            return new FieldDecoratorNode(placement, propertyDescriptor, initialize, ObjectLiteralMemberNode.cloneUninitialized(member,materializedTags));
        }

        @Override
        public void createMember() {
            member = ObjectLiteralNode.newDataMember("test",false, true, new DummyFunction(initializationFunction),true);
        }

        @Override
        public boolean isField() {
            return true;
        }

        @Override
        public boolean isAnonymousFunctionDefinition() {
            return member.isAnonymousFunctionDefinition();
        }

        @Override
        public boolean hasSameKind(DynamicObject elementDescriptor) {
            return JSOrdinaryObject.get(elementDescriptor, "kind").equals("field");
        }

        @Override
        public Object executeKey(VirtualFrame frame) {
            return member.executeKey(frame);
        }

        @Override
        public void executeVoid(VirtualFrame frame, DynamicObject homeObject, JSContext context) {
            member.executeVoid(frame, homeObject, context);
        }

        @Override
        public Object executeValue(VirtualFrame frame, DynamicObject homeObject) {
            return member.executeValue(frame, homeObject);
        }

        @Override
        public void execute(VirtualFrame frame, DynamicObject homeObject) {
            initializationFunction = initialize.execute(frame);
        }
    }

    private static class MethodDecoratorNode extends ClassElementDecoratorNode {
        @Child ObjectLiteralMemberNode member;
        @Child JavaScriptNode value;
        private Object valueFunction;

        MethodDecoratorNode(int placement, int propertyDescriptor, JavaScriptNode value, ObjectLiteralMemberNode member) {
            super(placement, propertyDescriptor);
            this.value = value;
            this.member = member;
        }

        @Override
        protected void updateState(DynamicObject newElementDescriptor) {
            super.updateState(newElementDescriptor);
        }

        @Override
        public void updateElementDescriptor(DynamicObject obj, JSContext context) {
            super.updateElementDescriptor(obj, context);
            if(valueFunction != null) {
                JSOrdinaryObject.set(obj,"method",valueFunction);
            }
        }

        @Override
        protected ClassElementDecoratorNode copyUninitialized(Set<Class<? extends Tag>> materializedTags) {
            return new MethodDecoratorNode(placement,propertyDescriptor,value, ObjectLiteralMemberNode.cloneUninitialized(member, materializedTags));
        }

        @Override
        public void createMember() {

        }

        @Override
        public boolean isField() {
            return false;
        }

        @Override
        public boolean isAnonymousFunctionDefinition() {
            return member.isAnonymousFunctionDefinition();
        }

        @Override
        public boolean hasSameKind(DynamicObject elementDescriptor) {
            return JSOrdinaryObject.get(elementDescriptor, "kind").equals("method");
        }

        @Override
        public Object executeKey(VirtualFrame frame) {
            return member.executeKey(frame);
        }

        @Override
        public void executeVoid(VirtualFrame frame, DynamicObject homeObject, JSContext context) {
            member.executeVoid(frame, homeObject, context);
        }

        @Override
        public Object executeValue(VirtualFrame frame, DynamicObject homeObject) {
            return member.executeValue(frame,homeObject);
        }

        @Override
        public void execute(VirtualFrame frame, DynamicObject homeObject) {
            if(value instanceof ObjectLiteralNode.MakeMethodNode) {
                valueFunction = ((ObjectLiteralNode.MakeMethodNode) value).executeWithObject(frame, homeObject);
            } else {
                valueFunction = value.execute(frame);
            }
        }
    }

    private static class AccessorDecoratorNode extends ClassElementDecoratorNode {
        @Child ObjectLiteralMemberNode member;
        @Child JavaScriptNode getter;
        @Child JavaScriptNode setter;
        private Object getterFunction;
        private Object setterFunction;

        AccessorDecoratorNode(int placement, int propertyDescriptor, JavaScriptNode getter, JavaScriptNode setter, ObjectLiteralMemberNode member) {
            super(placement, propertyDescriptor);
            this.getter = getter;
            this.setter = setter;
            this.member = member;
        }

        @Override
        protected ClassElementDecoratorNode copyUninitialized(Set<Class<? extends Tag>> materializedTags) {
            return null;
        }

        @Override
        public void createMember() {

        }

        @Override
        public boolean isField() {
            return false;
        }

        @Override
        public boolean isAnonymousFunctionDefinition() {
            return member.isAnonymousFunctionDefinition();
        }

        @Override
        public boolean hasSameKind(DynamicObject elementDescriptor) {
            return false;
        }

        @Override
        public Object executeKey(VirtualFrame frame) {
            return member.executeKey(frame);
        }

        @Override
        public void executeVoid(VirtualFrame frame, DynamicObject homeObject, JSContext context) {
            member.executeVoid(frame, homeObject, context);
        }

        @Override
        public Object executeValue(VirtualFrame frame, DynamicObject homeObject) {
            return member.executeValue(frame, homeObject);
        }

        @Override
        public void execute(VirtualFrame frame, DynamicObject homeObject) {
            if(getter instanceof ObjectLiteralNode.MakeMethodNode) {
                getterFunction = ((ObjectLiteralNode.MakeMethodNode) getter).executeWithObject(frame, homeObject);
            } else {
                getterFunction = getter.execute(frame);
            }
            if(setter instanceof ObjectLiteralNode.MakeMethodNode) {
                setterFunction = ((ObjectLiteralNode.MakeMethodNode) setter).executeWithObject(frame, homeObject);
            } else {
                setterFunction = setter.execute(frame);
            }
        }
    }
}
