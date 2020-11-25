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

    /**
     * A function or empty. This field can be absent
     */
    private final FunctionNode initialize;

    /**
     * A List of ECMAScript language values. This field can be absent.
     */
    private final List<Expression> decorators;

    private final boolean isPrivate;

    private ClassElementNode(long token, int finish, int kind, Expression key, int placement, Expression value,
                             int writeable, FunctionNode get, FunctionNode set, int enumerable, int configurable,
                             FunctionNode initialize, List<Expression> decorators, boolean isPrivate) {
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
    }

    private ClassElementNode(ClassElementNode classElementNode, int kind, Expression key, int placement, Expression value,
                             int writeable, FunctionNode get, FunctionNode set, int enumerable, int configurable,
                             FunctionNode initialize, List<Expression> decorators, boolean isPrivate) {
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
    }

    public static ClassElementNode createMethod(long token, int finish, Expression key, int placement, Expression value, List<Expression> decorators, boolean isPrivate) {
        if (isPrivate) {
            if ((placement & PLACEMENT_PROTOTYPE) != 0) {
                placement = PLACEMENT_OWN;
            }
            return new ClassElementNode(token, finish, KIND_METHOD, key, placement, value, STATE_FALSE, null, null, STATE_FALSE,
                    STATE_FALSE, null, decorators, isPrivate);
        } else {
            return new ClassElementNode(token, finish, KIND_METHOD, key, placement, value, STATE_TRUE, null, null, STATE_ABSENT,
                    STATE_TRUE, null, decorators, isPrivate);
        }
    }

    public static ClassElementNode createGetter(long token, int finish, Expression key, int placement, FunctionNode get, List<Expression> decorators, boolean isPrivate){
        if(isPrivate) {
            if ((placement & PLACEMENT_PROTOTYPE) != 0) {
                placement = PLACEMENT_OWN;
            }
            return  new ClassElementNode(token, finish, KIND_ACCESSOR, key, placement, null, STATE_ABSENT, get, null, STATE_FALSE,
                    STATE_FALSE,null, decorators, isPrivate);
        } else {
            return new ClassElementNode(token, finish, KIND_ACCESSOR, key, placement, null, STATE_ABSENT, get, null, STATE_ABSENT,
                    STATE_TRUE, null, decorators, isPrivate);
        }
    }

    public static ClassElementNode createSetter(long token, int finish, Expression key, int placement, FunctionNode set, List<Expression> decorators, boolean isPrivate) {
        if(isPrivate) {
            if ((placement & PLACEMENT_PROTOTYPE) != 0) {
                placement = PLACEMENT_OWN;
            }
            return  new ClassElementNode(token, finish, KIND_ACCESSOR, key, placement, null, STATE_ABSENT, null, set, STATE_FALSE,
                    STATE_FALSE,null, decorators, isPrivate);
        } else {
            return new ClassElementNode(token, finish, KIND_ACCESSOR, key, placement, null, STATE_ABSENT, null, set, STATE_ABSENT,
                    STATE_TRUE, null, decorators, isPrivate);
        }
    }

    public static ClassElementNode createField(long token, int finish, Expression key, int placement, FunctionNode initialize, List<Expression> decorators, boolean isPrivate) {
        if(isPrivate) {
            return new ClassElementNode(token, finish, KIND_FIELD, key, placement, null, STATE_FALSE, null, null, STATE_FALSE, STATE_FALSE,initialize, decorators, isPrivate);
        } else {
            return new ClassElementNode(token, finish, KIND_FIELD, key, placement, null, STATE_TRUE, null, null, STATE_TRUE, STATE_TRUE,initialize, decorators, isPrivate);
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

    public List<Expression> getDecorators() {
        return decorators;
    }

    public ClassElementNode setDecorators(List<Expression> decorators) {
        return new ClassElementNode(this,kind,key,placement,value,writeable,get,set,enumerable,configurable,initialize,decorators,isPrivate);
    }

    public FunctionNode getGetter(){
        return get;
    }

    public ClassElementNode setGetter(FunctionNode get) {
        return new ClassElementNode(this,kind,key,placement,value,writeable,get,set,enumerable,configurable,initialize,decorators,isPrivate);
    }

    public FunctionNode getSetter() {
        return set;
    }

    public ClassElementNode setSetter(FunctionNode set) {
        return new ClassElementNode(this,kind,key,placement,value,writeable,get,set,enumerable,configurable,initialize,decorators,isPrivate);
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
