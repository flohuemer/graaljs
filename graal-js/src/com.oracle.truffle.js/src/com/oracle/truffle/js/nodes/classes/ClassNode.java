package com.oracle.truffle.js.nodes.classes;

import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.js.nodes.JavaScriptBaseNode;
import com.oracle.truffle.js.nodes.JavaScriptNode;
import com.oracle.truffle.js.nodes.cast.JSToPropertyKeyNode;
import com.oracle.truffle.js.nodes.function.SetFunctionNameNode;
import com.oracle.truffle.js.nodes.instrumentation.JSTags;

public class ClassNode extends JavaScriptNode {
    @Override
    public Object execute(VirtualFrame frame) {
        return null;
    }

    public abstract static class ClassMemberBaseNode extends JavaScriptBaseNode {
        public abstract Object executeKey(VirtualFrame frame);
    }

    public abstract static class DecoratedClassMemberNode extends ClassMemberBaseNode {
        @Child private final ClassMemberBaseNode baseNode;
        @Children private final DecoratorNode[] decorators;

        public DecoratedClassMemberNode(ClassMemberBaseNode baseNode, DecoratorNode[] decorators) {
            this.baseNode = baseNode;
            this.decorators = decorators;
        }
    }

    public abstract static class ClassMemberNode extends ClassMemberBaseNode {
        protected final int placement;
        protected final int descriptorInfo;

        public ClassMemberNode(int placement, int descriptorInfo) {
            this.placement = placement;
            this.descriptorInfo = descriptorInfo;
        }

        @Override
        public Object executeKey(VirtualFrame frame) {
            return null;
        }
    }

    public static class ComputedClassMemberNode extends ClassMemberBaseNode {
        @Child private final ClassMemberBaseNode baseNode;
        @Child private final JavaScriptNode propertyKey;
        @Child private final JSToPropertyKeyNode toPropertyKey;

        ComputedClassMemberNode(ClassMemberBaseNode baseNode, JavaScriptNode key) {
            this.baseNode = baseNode;
            this.propertyKey = key;
            this.toPropertyKey = JSToPropertyKeyNode.create();
        }

        @Override
        public Object executeKey(VirtualFrame frame) {
            Object key = propertyKey.execute(frame);
            return  toPropertyKey.execute(key);
        }
    }

    public
}
