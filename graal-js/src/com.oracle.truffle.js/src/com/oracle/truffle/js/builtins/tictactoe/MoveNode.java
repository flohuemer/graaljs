package com.oracle.truffle.js.builtins.tictactoe;

import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.object.DynamicObject;
import com.oracle.truffle.js.nodes.access.GetIteratorNode;
import com.oracle.truffle.js.nodes.access.IteratorCloseNode;
import com.oracle.truffle.js.nodes.access.IteratorStepNode;
import com.oracle.truffle.js.nodes.access.IteratorValueNode;
import com.oracle.truffle.js.nodes.function.JSBuiltin;
import com.oracle.truffle.js.nodes.function.JSBuiltinNode;
import com.oracle.truffle.js.runtime.JSContext;
import com.oracle.truffle.js.runtime.JSRuntime;
import com.oracle.truffle.js.runtime.objects.IteratorRecord;
import com.oracle.truffle.js.runtime.objects.Undefined;

public abstract class MoveNode extends JSBuiltinNode {
    private JSContext context;

    @Child private GetIteratorNode getIteratorNode;
    @Child private IteratorStepNode iteratorStepNode;
    @Child private IteratorValueNode iteratorValueNode;
    @Child private IteratorCloseNode iteratorCloseNode;


    public MoveNode(JSContext context, JSBuiltin builtin) {
        super(context, builtin);
        this.context = context;
        this.getIteratorNode = GetIteratorNode.create(context);
        this.iteratorStepNode = IteratorStepNode.create(context);
        this.iteratorValueNode = IteratorValueNode.create(context);
        this.iteratorCloseNode = IteratorCloseNode.create(context);
    }

    @Specialization
    protected int move(Object boardState) {
        if(boardState == Undefined.instance) {
            //TODO: handle error
            return -1;
        }
        IteratorRecord iterator = getIteratorNode.execute(boardState);
        Object next;
        int[] board = new int[9];
        int boardIndex = 0;
        try {
            next = iteratorStepNode.execute(iterator);
            while(next != Boolean.FALSE) {
                Object value = iteratorValueNode.execute((DynamicObject) next);
                board[boardIndex++] = JSRuntime.toInt32(JSRuntime.toObject(context, value));
                next = iteratorStepNode.execute(iterator);
            }
        } catch (Exception ex) {
            iteratorCloseNode.executeAbrupt(iterator.getIterator());
            throw ex;
        }
        return SearchTree.getNextAction(board);
    }
}
