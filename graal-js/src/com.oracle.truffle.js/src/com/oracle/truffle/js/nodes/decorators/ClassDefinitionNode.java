/*
 * Copyright (c) 2018, 2020, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.truffle.js.nodes.decorators;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.api.object.HiddenKey;
import com.oracle.truffle.js.nodes.JavaScriptNode;
import com.oracle.truffle.js.nodes.access.CreateObjectNode;
import com.oracle.truffle.js.nodes.access.InitializeInstanceElementsNode;
import com.oracle.truffle.js.nodes.access.JSWriteFrameSlotNode;
import com.oracle.truffle.js.nodes.access.ObjectLiteralNode.ObjectLiteralMemberNode;
import com.oracle.truffle.js.nodes.access.PropertyGetNode;
import com.oracle.truffle.js.nodes.access.PropertySetNode;
import com.oracle.truffle.js.nodes.function.CreateMethodPropertyNode;
import com.oracle.truffle.js.nodes.function.DefineMethodNode;
import com.oracle.truffle.js.nodes.function.FunctionNameHolder;
import com.oracle.truffle.js.nodes.function.JSFunctionExpressionNode;
import com.oracle.truffle.js.nodes.function.SetFunctionNameNode;
import com.oracle.truffle.js.runtime.Boundaries;
import com.oracle.truffle.js.runtime.Errors;
import com.oracle.truffle.js.runtime.JSContext;
import com.oracle.truffle.js.runtime.JSRealm;
import com.oracle.truffle.js.runtime.JSRuntime;
import com.oracle.truffle.js.runtime.builtins.JSFunction;
import com.oracle.truffle.js.runtime.objects.JSObject;
import com.oracle.truffle.js.runtime.objects.Null;

import java.util.Set;

/**
 * ES6 14.5.14 Runtime Semantics: ClassDefinitionEvaluation.
 */
public final class ClassDefinitionNode extends JavaScriptNode implements FunctionNameHolder {

    private final JSContext context;
    private final boolean hasName;
    private final int instanceFieldCount;
    private final int staticFieldCount;

    //Constructor
    @Child private JavaScriptNode constructorFunctionNode;
    @Child private DefineMethodNode defineConstructorMethodNode;
    @Child private CreateMethodPropertyNode setConstructorNode;

    //Prototype
    @Child private PropertyGetNode getPrototypeNode;
    @Child private CreateObjectNode.CreateObjectWithPrototypeNode createPrototypeNode;

    //Definition
    @Child private JavaScriptNode classHeritageNode;
    @Child private JSWriteFrameSlotNode writeClassBindingNode;
    @Child private SetFunctionNameNode setFunctionName;

    //Class Members
    @Children private final ObjectLiteralMemberNode[] memberNodes;

    //Initialisation
    @Child private PropertySetNode setFieldsNode;
    @Child private InitializeInstanceElementsNode staticFieldsNode;
    @Child private PropertySetNode setPrivateBrandNode;

    //Decorator Class Element Handling

    @Child private PropertySetNode setElementsNode;
    @Child private EvaluateClassElementsNode evaluateClassElementsNode;
    @Child private InitializeClassElementsNode initializeClassElementsNode;

    //Decorator Handling
    @Child private DecorateClassNode decorateClassNode;

    protected ClassDefinitionNode(JSContext context, JSFunctionExpressionNode constructorFunctionNode, JavaScriptNode classHeritageNode, ObjectLiteralMemberNode[] memberNodes,
                                  JSWriteFrameSlotNode writeClassBindingNode, boolean hasName, int instanceFieldCount, int staticFieldCount, boolean hasPrivateInstanceMethods, ClassElementNode[] classElementNodes, JavaScriptNode[] decoratorNodes) {
        this.context = context;
        this.hasName = hasName;
        this.instanceFieldCount = instanceFieldCount;
        this.staticFieldCount = staticFieldCount;

        this.constructorFunctionNode = constructorFunctionNode;
        this.setConstructorNode = CreateMethodPropertyNode.create(context, JSObject.CONSTRUCTOR);
        this.defineConstructorMethodNode = DefineMethodNode.create(context, constructorFunctionNode);

        this.getPrototypeNode = PropertyGetNode.create(JSObject.PROTOTYPE, false, context);
        this.createPrototypeNode = CreateObjectNode.createOrdinaryWithPrototype(context);

        this.classHeritageNode = classHeritageNode;
        this.writeClassBindingNode = writeClassBindingNode;
        this.setFunctionName = hasName ? null : SetFunctionNameNode.create();

        this.memberNodes = memberNodes;

        this.setFieldsNode = instanceFieldCount != 0 ? PropertySetNode.createSetHidden(JSFunction.CLASS_FIELDS_ID, context) : null;
        this.setPrivateBrandNode = hasPrivateInstanceMethods ? PropertySetNode.createSetHidden(JSFunction.PRIVATE_BRAND_ID, context) : null;

        if(context.areDecoratorsEnabled()) {
            this.setElementsNode = PropertySetNode.createSetHidden(JSFunction.ELEMENTS_ID, context);
            this.evaluateClassElementsNode = EvaluateClassElementsNode.create(context, classElementNodes);
            this.initializeClassElementsNode = InitializeClassElementsNode.create(context);

            this.decorateClassNode = DecorateClassNode.create(context, decoratorNodes);
        } else {
            this.setElementsNode = null;
            this.evaluateClassElementsNode = null;
            this.initializeClassElementsNode = null;

            this.decorateClassNode = null;
        }
    }

    private ClassDefinitionNode(JSContext context, JSFunctionExpressionNode constructorFunctionNode, JavaScriptNode classHeritageNode, JSWriteFrameSlotNode writeClassBindingNode, DecorateClassNode decoratorNode, boolean hasName, EvaluateClassElementsNode evaluateClassElementsNode) {
        this.context = context;
        this.hasName = hasName;
        this.instanceFieldCount = 0;
        this.staticFieldCount = 0;

        this.constructorFunctionNode = constructorFunctionNode;
        this.setConstructorNode = CreateMethodPropertyNode.create(context, JSObject.CONSTRUCTOR);
        this.defineConstructorMethodNode = DefineMethodNode.create(context, constructorFunctionNode);

        this.getPrototypeNode = PropertyGetNode.create(JSObject.PROTOTYPE, false, context);
        this.createPrototypeNode = CreateObjectNode.createOrdinaryWithPrototype(context);

        this.classHeritageNode = classHeritageNode;
        this.writeClassBindingNode = writeClassBindingNode;
        this.setFunctionName = hasName ? null : SetFunctionNameNode.create();

        this.memberNodes = null;

        this.setFieldsNode = null;
        this.setPrivateBrandNode = null;

        this.setElementsNode = PropertySetNode.createSetHidden(JSFunction.ELEMENTS_ID, context);
        this.evaluateClassElementsNode = evaluateClassElementsNode;
        this.initializeClassElementsNode = InitializeClassElementsNode.create(context);

        this.decorateClassNode = decoratorNode;
    }

    public static ClassDefinitionNode create(JSContext context, JSFunctionExpressionNode constructorFunction, JavaScriptNode classHeritage, ObjectLiteralMemberNode[] members,
                                             JSWriteFrameSlotNode writeClassBinding, boolean hasName, int instanceFieldCount, int staticFieldCount, boolean hasPrivateInstanceMethods) {
        return new ClassDefinitionNode(context, constructorFunction, classHeritage, members, writeClassBinding, hasName, instanceFieldCount, staticFieldCount, hasPrivateInstanceMethods, null, null);
    }

    public static ClassDefinitionNode createDecoratorClassDefinitionNode(JSContext context, JSFunctionExpressionNode constructorFunctionNode, JavaScriptNode classHeritage, JSWriteFrameSlotNode writeClassBindingNode, ClassElementNode[] classElementNodes, JavaScriptNode[] classDecorators, boolean hasName) {
        return new ClassDefinitionNode(context, constructorFunctionNode, classHeritage, null, writeClassBindingNode, hasName, 0, 0, false, classElementNodes, classDecorators);
    }

    @Override
    public DynamicObject execute(VirtualFrame frame) {
        return executeWithClassName(frame, null);
    }

    public DynamicObject executeWithClassName(VirtualFrame frame, Object className) {
        JSRealm realm = context.getRealm();
        Object protoParent = realm.getObjectPrototype();
        Object constructorParent = realm.getFunctionPrototype();
        if (classHeritageNode != null) {
            Object superclass = classHeritageNode.execute(frame);
            if (superclass == Null.instance) {
                protoParent = Null.instance;
            } else if (!JSRuntime.isConstructor(superclass)) {
                // 6.f. if IsConstructor(superclass) is false, throw a TypeError.
                throw Errors.createTypeError("not a constructor", this);
            } else if (JSRuntime.isGenerator(superclass)) {
                // 6.g.i. if superclass.[[FunctionKind]] is "generator", throw a TypeError
                throw Errors.createTypeError("class cannot extend a generator function", this);
            } else {
                protoParent = getPrototypeNode.getValue(superclass);
                if (protoParent != Null.instance && !JSRuntime.isObject(protoParent)) {
                    throw Errors.createTypeError("protoParent is neither Object nor Null", this);
                }
                constructorParent = superclass;
            }
        }

        /* Let proto be ObjectCreate(protoParent). */
        assert protoParent == Null.instance || JSRuntime.isObject(protoParent);
        DynamicObject proto = createPrototypeNode.execute(frame, ((DynamicObject) protoParent));

        /*
         * Let constructorInfo be the result of performing DefineMethod for constructor with
         * arguments proto and constructorParent as the optional functionPrototype argument.
         */
        DynamicObject constructor = defineConstructorMethodNode.execute(frame, proto, (DynamicObject) constructorParent);

        // Perform MakeConstructor(F, writablePrototype=false, proto).
        JSFunction.setClassPrototype(constructor, proto);

        // If className is not undefined, perform SetFunctionName(F, className).
        if (setFunctionName != null && className != null) {
            setFunctionName.execute(constructor, className);
        }

        // Perform CreateMethodProperty(proto, "constructor", F).
        setConstructorNode.executeVoid(proto, constructor);

        if(context.areDecoratorsEnabled()) {
            return executeWithDecorators(frame, constructor, proto);
        } else {
            executeWithoutDecorators(frame, constructor, proto);
        }

        return constructor;
    }

    private void executeWithoutDecorators(VirtualFrame frame, DynamicObject constructor, DynamicObject proto) {
        //Non-decorator Class Initialization
        Object[][] instanceFields = instanceFieldCount == 0 ? null : new Object[instanceFieldCount][];
        Object[][] staticFields = staticFieldCount == 0 ? null : new Object[staticFieldCount][];

        initializeMembers(frame, proto, constructor, instanceFields, staticFields);

        if(writeClassBindingNode != null) {
            writeClassBindingNode.executeWrite(frame, constructor);
        }

        if(setFieldsNode != null) {
            setFieldsNode.setValue(constructor, instanceFields);
        }

        //If the class contains a private instance method or accessor, set F.[[PrivateBrand]].
        if(setPrivateBrandNode != null) {
            HiddenKey privateBrand = new HiddenKey("Brand");
            setPrivateBrandNode.setValue(constructor, privateBrand);
        }

        if(staticFieldCount != 0) {
            InitializeInstanceElementsNode defineStaticFields = this.staticFieldsNode;
            if (defineStaticFields == null) {
                CompilerDirectives.transferToInterpreterAndInvalidate();
                this.staticFieldsNode = defineStaticFields = insert(InitializeInstanceElementsNode.create(context));
            }
            defineStaticFields.executeStaticFields(constructor, staticFields);
        }
    }

    private DynamicObject executeWithDecorators(VirtualFrame frame,DynamicObject constructor, DynamicObject proto) {
        //Decorator Class Initialization

        //ClassElementEvaluation
        //CoalesceClassElements
        ElementDescriptor[] elements = evaluateClassElementsNode.execute(frame, proto, constructor);

        //DecorateClass
        ClassElementList classElements = decorateClassNode.executeElementDecoration(elements);
        decorateClassNode.executeClassDecoration(frame, classElements);

        //AssignPrivatNames
        assignPrivateNames(classElements);

        //InitializeClassElements
        constructor = initializeClassElementsNode.execute(proto, constructor, classElements);

        if(writeClassBindingNode != null) {
            writeClassBindingNode.executeWrite(frame, constructor);
        }

        //Only elements with kind "own" get pushed to the initialization.
        if(Boundaries.listSize(classElements.getOwnElements()) != 0) {
            classElements.removeStaticAndPrototypeElements();
            setElementsNode.setValue(constructor, classElements);
        }
        return constructor;
    }

    @ExplodeLoop
    private void initializeMembers(VirtualFrame frame, DynamicObject proto, DynamicObject constructor, Object[][] instanceFields, Object[][] staticFields) {
        /* For each ClassElement e in order from NonConstructorMethodDefinitions of ClassBody */
        int instanceFieldIndex = 0;
        int staticFieldIndex = 0;
        for (ObjectLiteralMemberNode memberNode : memberNodes) {
            DynamicObject homeObject = memberNode.isStatic() ? constructor : proto;
            memberNode.executeVoid(frame, homeObject, context);
            if (memberNode.isField()) {
                Object key = memberNode.evaluateKey(frame);
                Object value = memberNode.evaluateValue(frame, homeObject);
                Object[] field = new Object[]{key, value, memberNode.isAnonymousFunctionDefinition()};
                if (memberNode.isStatic() && staticFields != null) {
                    staticFields[staticFieldIndex++] = field;
                } else if (instanceFields != null) {
                    instanceFields[instanceFieldIndex++] = field;
                } else {
                    throw Errors.shouldNotReachHere();
                }
            }
        }
        assert instanceFieldIndex == instanceFieldCount && staticFieldIndex == staticFieldCount;
    }

    private void assignPrivateNames(ClassElementList elements) {
        for (ElementDescriptor element : elements.getOwnElements()) {
            assignPrivateName(element);
        }
        for(ElementDescriptor element: elements.getStaticAndPrototypeElements()) {
            assignPrivateName(element);
        }
    }

    private void assignPrivateName(ElementDescriptor element) {
        if(element.hasKey() && element.hasPrivateKey()) {
            PrivateName key = element.getPrivateKey();
            if(element.isField() || element.isMethod() || element.isAccessor()) {
                key.setKind(element.getKind());
            }
            key.setDescriptor(element.getDescriptor());
        }
    }

    @Override
    public boolean isResultAlwaysOfType(Class<?> clazz) {
        return clazz == DynamicObject.class;
    }

    @Override
    public String getFunctionName() {
        return hasName ? ((FunctionNameHolder) constructorFunctionNode).getFunctionName() : "";
    }

    @Override
    public void setFunctionName(String name) {
        ((FunctionNameHolder) constructorFunctionNode).setFunctionName(name);
    }

    @Override
    protected JavaScriptNode copyUninitialized(Set<Class<? extends Tag>> materializedTags) {
        if(!context.areDecoratorsEnabled())
        {
            return create(context,(JSFunctionExpressionNode) cloneUninitialized(constructorFunctionNode, materializedTags), cloneUninitialized(classHeritageNode, materializedTags),
                    ObjectLiteralMemberNode.cloneUninitialized(memberNodes, materializedTags),
                    cloneUninitialized(writeClassBindingNode, materializedTags), hasName, instanceFieldCount, staticFieldCount, setPrivateBrandNode != null);
        } else {
            return new ClassDefinitionNode(context, (JSFunctionExpressionNode) cloneUninitialized(constructorFunctionNode, materializedTags), cloneUninitialized(classHeritageNode, materializedTags),
                    cloneUninitialized(writeClassBindingNode, materializedTags), DecorateClassNode.cloneUninitialized(decorateClassNode, materializedTags), hasName, EvaluateClassElementsNode.cloneUninitialized(evaluateClassElementsNode, materializedTags));
        }
    }
}
