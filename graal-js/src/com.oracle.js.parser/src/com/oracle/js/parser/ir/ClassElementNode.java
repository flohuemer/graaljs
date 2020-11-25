package com.oracle.js.parser.ir;

import com.oracle.js.parser.ir.visitor.NodeVisitor;
import com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor;

import java.util.List;

/**
 * This Node does not represent a hook.
 */
public class ClassElementNode extends Node {

    /**
     * One of "method", "accessor",or "field"
     */
    private final int kind;
    private static final int KIND_METHOD = 1 << 0;
    private static final int KIND_ACCESSOR = 1 << 1;
    private static final int KIND_FIELD = 1 << 2;

    /**
     * A PropertyKey or %PrivateName% object
     */
    private final Expression key;

    /**
     * PropertyDescriptor fields
     */
    private final Expression value;
    private final boolean writeable;
    private final FunctionNode get;
    private final FunctionNode set;
    private final boolean enumerable;
    private final boolean configurable;

    /**
     * One of "static", "prototype",or "own"
     */
    private final int placement;
    public static final int PLACEMENT_STATIC = 1 << 0;
    public static final int PLACEMENT_PROTOTYPE = 1 << 1;
    public static final int PLACEMENT_OWN = 1 << 2;

    /**
     * A function or empty. This field can be absent
     */
    private final FunctionNode initialize;

    /**
     * A List of ECMAScript language values. This field can be absent.
     */
    private final List<Expression> decorators;

    private ClassElementNode(long token, int finish, int kind, Expression key, int placement, Expression value,
                             boolean writeable, FunctionNode get, FunctionNode set, boolean enumerable, boolean configurable,
                             FunctionNode initialize, List<Expression> decorators) {
        super(token, finish);
        this.kind = kind;
        this.key = key;
        this.placement = placement;
        this.value = value;
        this.writeable = writeable;
        this.get = get;
        this.set = set;
        this.enumerable = enumerable;
        this.configurable = configurable;
        this.initialize = initialize;
        this.decorators = decorators;
    }

    public static ClassElementNode createMethod(long token, int finish, Expression key, int placement, Expression value, boolean writeable,
                               boolean enumerable, boolean configurable, List<Expression> decorators) {
        return new ClassElementNode(token, finish, KIND_METHOD, key, placement, value, writeable, null, null, enumerable,
                configurable, null, decorators);
    }

    public static ClassElementNode createAccessor(long token, int finish, Expression key, int placement, FunctionNode get, FunctionNode set,
                                                  boolean enumerable, boolean configurable, List<Expression> decorators){
        return new ClassElementNode(token, finish, KIND_ACCESSOR, key, placement, null, false, get, set, enumerable,
                configurable, null, decorators);
    }

    public static ClassElementNode createField(long token, int finish, Expression key, int placement, boolean writeable, boolean enumerable,
                                               boolean configurable, FunctionNode initialize, List<Expression> decorators) {
        return new ClassElementNode(token, finish, KIND_FIELD, key, placement, null, writeable, null, null, enumerable,
                configurable, initialize, decorators);
    }

    @Override
    public Node accept(NodeVisitor<? extends LexicalContext> visitor) {
        return null;
    }

    @Override
    public <R> R accept(TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return null;
    }

    @Override
    public void toString(StringBuilder sb, boolean printType) {

    }
}
