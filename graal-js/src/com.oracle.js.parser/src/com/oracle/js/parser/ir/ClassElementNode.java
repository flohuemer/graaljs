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
    private final FunctionNode value;
    private final int writeable;
    private final FunctionNode get;
    private final FunctionNode set;
    private final int enumerable;
    private final int configurable;

    public static final int STATE_ABSENT = 1 << 0;
    public static final int STATE_TRUE = 1 << 1;
    public static final int STATE_FALSE = 1 << 2;

    /**
     * One of "static", "prototype",or "own"
     */
    private final int placement;
    public static final int PLACEMENT_STATIC = 1 << 0;
    public static final int PLACEMENT_PROTOTYPE = 1 << 1;
    public static final int PLACEMENT_OWN = 1 << 2;
    public static final int PLACEMENT_NONE = 1 << 3;

    /**
     * A function or empty. This field can be absent
     */
    private final FunctionNode initialize;

    /**
     * A List of ECMAScript language values. This field can be absent.
     */
    private final List<Expression> decorators;

    private final boolean isPrivate;

    private final boolean isComputed;

    private ClassElementNode(long token, int finish, int kind, Expression key, int placement, FunctionNode value,
                             int writeable, FunctionNode get, FunctionNode set, int enumerable, int configurable,
                             FunctionNode initialize, List<Expression> decorators, boolean isPrivate, boolean isComputed) {
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
        this.isPrivate = isPrivate;
        this.isComputed = isComputed;
    }

    private ClassElementNode(ClassElementNode classElementNode, int kind, Expression key, int placement, FunctionNode value,
                             int writeable, FunctionNode get, FunctionNode set, int enumerable, int configurable,
                             FunctionNode initialize, List<Expression> decorators, boolean isPrivate, boolean isComputed) {
        super(classElementNode);
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
        this.isPrivate = isPrivate;
        this.isComputed = isComputed;
    }

    public static ClassElementNode createMethod(long token, int finish, Expression key, int placement, FunctionNode value, List<Expression> decorators, boolean isPrivate, boolean isComputed) {
        if (isPrivate) {
            if ((placement & PLACEMENT_PROTOTYPE) != 0) {
                placement = PLACEMENT_OWN;
            }
            return new ClassElementNode(token, finish, KIND_METHOD, key, placement, value, STATE_FALSE, null, null, STATE_FALSE,
                    STATE_FALSE, null, decorators, true, isComputed);
        } else {
            return new ClassElementNode(token, finish, KIND_METHOD, key, placement, value, STATE_TRUE, null, null, STATE_ABSENT,
                    STATE_TRUE, null, decorators, false, isComputed);
        }
    }

    public static ClassElementNode createGetter(long token, int finish, Expression key, int placement, FunctionNode get, List<Expression> decorators, boolean isPrivate, boolean isComputed){
        if(isPrivate) {
            if ((placement & PLACEMENT_PROTOTYPE) != 0) {
                placement = PLACEMENT_OWN;
            }
            return  new ClassElementNode(token, finish, KIND_ACCESSOR, key, placement, null, STATE_ABSENT, get, null, STATE_FALSE,
                    STATE_FALSE,null, decorators, true, isComputed);
        } else {
            return new ClassElementNode(token, finish, KIND_ACCESSOR, key, placement, null, STATE_ABSENT, get, null, STATE_ABSENT,
                    STATE_TRUE, null, decorators, false, isComputed);
        }
    }

    public static ClassElementNode createSetter(long token, int finish, Expression key, int placement, FunctionNode set, List<Expression> decorators, boolean isPrivate, boolean isComputed) {
        if(isPrivate) {
            if ((placement & PLACEMENT_PROTOTYPE) != 0) {
                placement = PLACEMENT_OWN;
            }
            return  new ClassElementNode(token, finish, KIND_ACCESSOR, key, placement, null, STATE_ABSENT, null, set, STATE_FALSE,
                    STATE_FALSE,null, decorators, true, isComputed);
        } else {
            return new ClassElementNode(token, finish, KIND_ACCESSOR, key, placement, null, STATE_ABSENT, null, set, STATE_ABSENT,
                    STATE_TRUE, null, decorators, false, isComputed);
        }
    }

    public static ClassElementNode createField(long token, int finish, Expression key, int placement, FunctionNode initialize, List<Expression> decorators, boolean isPrivate, boolean isComputed) {
        if(isPrivate) {
            return new ClassElementNode(token, finish, KIND_FIELD, key, placement, null, STATE_FALSE, null, null, STATE_FALSE, STATE_FALSE,initialize, decorators, true, isComputed);
        } else {
            return new ClassElementNode(token, finish, KIND_FIELD, key, placement, null, STATE_TRUE, null, null, STATE_TRUE, STATE_TRUE,initialize, decorators, false, isComputed);
        }
    }

    public boolean isStatic() {
        return (placement & PLACEMENT_STATIC) != 0;
    }

    public boolean isPrototype() {
        return (placement & PLACEMENT_PROTOTYPE) != 0;
    }

    public boolean isOwn() {
        return (placement & PLACEMENT_OWN) != 0;
    }

    /**
     * Get the name of the property key
     *
     * @return key name
     */
    public String getKeyName() {
        return key instanceof PropertyKey ? ((PropertyKey) key).getPropertyName() : null;
    }

    public Expression getKey() { return key; }

    public int getKind() {
        return kind;
    }

    public int getPlacement() {
        return placement;
    }

    public boolean isAccessor() {
        return (kind & KIND_ACCESSOR) != 0;
    }

    public boolean isField() {
        return (kind & KIND_FIELD) != 0;
    }

    public boolean isMethod() {
        return (kind & KIND_METHOD) != 0;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean isEnumerable() { return (enumerable & STATE_TRUE) != 0; }

    public String getPrivateName() {
        assert isPrivate();
        return ((IdentNode) key).getName();
    }

    public boolean isComputed() { return isComputed; }

    public List<Expression> getDecorators() {
        return decorators;
    }

    public ClassElementNode setDecorators(List<Expression> decorators) {
        return new ClassElementNode(this,kind,key,placement,value,writeable,get,set,enumerable,configurable,initialize,decorators,isPrivate,isComputed);
    }

    public FunctionNode getGetter(){
        return get;
    }

    public ClassElementNode setGetter(FunctionNode get) {
        return new ClassElementNode(this,kind,key,placement,value,writeable,get,set,enumerable,configurable,initialize,decorators,isPrivate,isComputed);
    }

    public FunctionNode getSetter() {
        return set;
    }

    public ClassElementNode setSetter(FunctionNode set) {
        return new ClassElementNode(this,kind,key,placement,value,writeable,get,set,enumerable,configurable,initialize,decorators,isPrivate,isComputed);
    }

    public FunctionNode getValue() { return value; }

    public ClassElementNode setValue(FunctionNode value) {
        return  new ClassElementNode(this, kind, key, placement,value, writeable, get,set, enumerable,configurable,initialize,decorators,isPrivate,isComputed);
    }

    public int isWriteable() {
        return writeable;
    }

    public int isConfigurable() {
        return configurable;
    }

    public int getDescriptorInfo() {
        return writeable << 6 + enumerable << 3 + configurable;
    }

    public FunctionNode getInitialize() { return initialize; }

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
        if(isStatic()) {
            sb.append("static ");
        }

        if (isField()) {
            toStringKey(sb, printType);
            initialize.toString(sb, printType);
        }
        if(isMethod()) {
            toStringKey(sb, printType);
            value.toStringTail(sb, printType);
        }
        if(isAccessor()) {
            if (get != null) {
                sb.append("get ");
                toStringKey(sb, printType);
                get.toStringTail(sb, printType);
            }
            if (set != null) {
                sb.append("set ");
                toStringKey(sb, printType);
                set.toStringTail(sb, printType);
            }
        }
    }

    private void toStringKey(final StringBuilder sb, final boolean printType) {
        if (isComputed) {
            sb.append('[');
        }
        key.toString(sb, printType);
        if (isComputed) {
            sb.append(']');
        }
    }
}
