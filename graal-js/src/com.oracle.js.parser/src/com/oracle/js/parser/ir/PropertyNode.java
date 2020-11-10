/*
 * Copyright (c) 2010, 2020, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.oracle.js.parser.ir;

import com.oracle.js.parser.TokenType;
import com.oracle.js.parser.ir.visitor.NodeVisitor;
import com.oracle.js.parser.ir.visitor.TranslatorNodeVisitor;

import java.util.List;

/**
 * IR representation of an object literal property.
 */
public final class PropertyNode extends Node {

    /**
     * Property key.
     */
    private final Expression key;

    /**
     * Property value.
     */
    private final Expression value;

    /**
     * Property getter.
     */
    private final FunctionNode getter;

    /**
     * Property setter.
     */
    private final FunctionNode setter;

    private final boolean computed;

    private final boolean coverInitializedName;

    private final boolean proto;

    private final boolean classField;

    private final boolean isAnonymousFunctionDefinition;

    private final List<Expression> decorators;

    private final int placements;

    public static final int PLACEMENT_EMPTY = 1 << 0;

    public static final int PLACEMENT_STATIC = 1 << 1;

    public static final int PLACEMENT_OWN = 1 << 2;

    public static final int PLACEMENT_PROTOTYPE = 1 << 3;

    public PropertyNode(long token, int finish, Expression key, Expression value, FunctionNode getter, FunctionNode setter, boolean computed, boolean coverInitializedName, boolean proto, List<Expression> decorators, int placements) {
        this(token, finish, key, value, getter, setter, computed, coverInitializedName, proto, false, false, decorators, placements);
    }

    public PropertyNode(long token, int finish, Expression key, Expression value, FunctionNode getter, FunctionNode setter, boolean computed, boolean coverInitializedName, boolean proto, int placements) {
        this(token, finish, key, value, getter, setter, computed, coverInitializedName, proto, false, false, null, placements);
    }

    public PropertyNode(long token, int finish, Expression key, Expression value, FunctionNode getter, FunctionNode setter, boolean computed, boolean coverInitializedName, boolean proto, boolean classField, boolean isAnonymousFunctionDefinition, int placements) {
        this(token, finish, key, value, getter, setter, computed, coverInitializedName, proto, classField, isAnonymousFunctionDefinition, null, placements);
    }

    /**
     * Constructor
     *
     * @param token  token
     * @param finish finish
     * @param key    the key of this property
     * @param value  the value of this property
     * @param getter getter function body
     * @param setter setter function body
     */
    public PropertyNode(long token, int finish, Expression key, Expression value, FunctionNode getter, FunctionNode setter, boolean computed, boolean coverInitializedName, boolean proto, boolean classField, boolean isAnonymousFunctionDefinition, List<Expression> decorators, int placements) {
        super(token, finish);
        this.key = key;
        this.value = value;
        this.getter = getter;
        this.setter = setter;
        this.computed = computed;
        this.coverInitializedName = coverInitializedName;
        this.proto = proto;
        this.classField = classField;
        this.isAnonymousFunctionDefinition = isAnonymousFunctionDefinition;
        this.decorators = decorators;
        this.placements = placements;
    }

    private PropertyNode(PropertyNode propertyNode, Expression key, Expression value, FunctionNode getter, FunctionNode setter, boolean computed, boolean coverInitializedName, boolean proto, List<Expression> decorators, int placements) {
        super(propertyNode);
        this.key = key;
        this.value = value;
        this.getter = getter;
        this.setter = setter;
        this.computed = computed;
        this.coverInitializedName = coverInitializedName;
        this.proto = proto;
        this.classField = propertyNode.classField;
        this.isAnonymousFunctionDefinition = propertyNode.isAnonymousFunctionDefinition;
        this.decorators = decorators;
        this.placements = placements;
    }

    /**
     * Get the name of the property key
     *
     * @return key name
     */
    public String getKeyName() {
        return key instanceof PropertyKey ? ((PropertyKey) key).getPropertyName() : null;
    }

    @Override
    public Node accept(final NodeVisitor<? extends LexicalContext> visitor) {
        if (visitor.enterPropertyNode(this)) {
            //@formatter:off
            return visitor.leavePropertyNode(
                    setKey((Expression) key.accept(visitor)).
                            setValue(value == null ? null : (Expression) value.accept(visitor)).
                            setGetter(getter == null ? null : (FunctionNode) getter.accept(visitor)).
                            setSetter(setter == null ? null : (FunctionNode) setter.accept(visitor)));
            //@formatter:on
        }

        return this;
    }

    @Override
    public <R> R accept(TranslatorNodeVisitor<? extends LexicalContext, R> visitor) {
        return visitor.enterPropertyNode(this);
    }

    @Override
    public void toString(final StringBuilder sb, final boolean printType) {
        if (value != null) {
            if (hasStaticPlacement()) {
                sb.append("static ");
            }
            if (value instanceof FunctionNode && ((FunctionNode) value).isMethod()) {
                toStringKey(sb, printType);
                ((FunctionNode) value).toStringTail(sb, printType);
            } else {
                toStringKey(sb, printType);
                sb.append(": ");
                value.toString(sb, printType);
            }
        }

        if (getter != null) {
            if (hasStaticPlacement()) {
                sb.append("static ");
            }
            sb.append("get ");
            toStringKey(sb, printType);
            getter.toStringTail(sb, printType);
        }

        if (setter != null) {
            if (hasStaticPlacement()) {
                sb.append("static ");
            }
            sb.append("set ");
            toStringKey(sb, printType);
            setter.toStringTail(sb, printType);
        }
    }

    private void toStringKey(final StringBuilder sb, final boolean printType) {
        if (computed) {
            sb.append('[');
        }
        key.toString(sb, printType);
        if (computed) {
            sb.append(']');
        }
    }

    /**
     * Get the getter for this property
     *
     * @return getter or null if none exists
     */
    public FunctionNode getGetter() {
        return getter;
    }

    /**
     * Set the getter of this property, null if none
     *
     * @param getter getter
     * @return same node or new node if state changed
     */
    public PropertyNode setGetter(final FunctionNode getter) {
        if (this.getter == getter) {
            return this;
        }
        return new PropertyNode(this, key, value, getter, setter, computed, coverInitializedName, proto, decorators, placements);
    }

    /**
     * Return the key for this property node
     *
     * @return the key
     */
    public Expression getKey() {
        return key;
    }

    private PropertyNode setKey(final Expression key) {
        if (this.key == key) {
            return this;
        }
        return new PropertyNode(this, key, value, getter, setter, computed, coverInitializedName, proto, decorators, placements);
    }

    /**
     * Get the setter for this property
     *
     * @return setter or null if none exists
     */
    public FunctionNode getSetter() {
        return setter;
    }

    /**
     * Set the setter for this property, null if none
     *
     * @param setter setter
     * @return same node or new node if state changed
     */
    public PropertyNode setSetter(final FunctionNode setter) {
        if (this.setter == setter) {
            return this;
        }
        return new PropertyNode(this, key, value, getter, setter, computed, coverInitializedName, proto, decorators, placements);
    }

    /**
     * Get the value of this property
     *
     * @return property value
     */
    public Expression getValue() {
        return value;
    }

    /**
     * Set the value of this property
     *
     * @param value new value
     * @return same node or new node if state changed
     */
    public PropertyNode setValue(final Expression value) {
        if (this.value == value) {
            return this;
        }
        return new PropertyNode(this, key, value, getter, setter, computed, coverInitializedName, proto, decorators, placements);
    }

    /**
     * Get the decorators of this property
     *
     * @return property decorators
     */
    public List<Expression> getDecorators() {
        return decorators;
    }

    /**
     * Sets the decorators of this property
     *
     * @param decorators new decorators
     * @return same node or new node if decorators changed
     */
    public PropertyNode setDecorators(final List<Expression> decorators) {
        if (this.decorators == decorators) {
            return this;
        }
        return new PropertyNode(this, key, value, getter, setter, computed, coverInitializedName, proto, decorators, placements);
    }

    public boolean hasStaticPlacement() {
        return (placements & PLACEMENT_STATIC) != 0;
    }

    public boolean hasOwnPlacement() {
        return (placements & PLACEMENT_OWN) != 0;
    }

    public boolean hasPrototypePlacement() {
        return (placements & PLACEMENT_PROTOTYPE) != 0;
    }

    public int getPlacements() {
        return  placements;
    }

    public boolean isComputed() {
        return computed;
    }

    public boolean isCoverInitializedName() {
        return coverInitializedName;
    }

    public boolean isProto() {
        return proto;
    }

    public boolean isRest() {
        return key != null && key.isTokenType(TokenType.SPREAD_OBJECT);
    }

    public boolean isClassField() {
        return classField;
    }

    public boolean isAnonymousFunctionDefinition() {
        return isAnonymousFunctionDefinition;
    }

    public boolean isPrivate() {
        return key instanceof IdentNode && ((IdentNode) key).isPrivate();
    }

    public String getPrivateName() {
        assert isPrivate();
        return ((IdentNode) key).getName();
    }

    public boolean isAccessor() {
        return getter != null || setter != null;
    }
}
