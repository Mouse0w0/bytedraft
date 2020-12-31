// ASM: a very small and fast Java bytecode manipulation framework
// Copyright (c) 2000-2011 INRIA, France Telecom
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
//    contributors may be used to endorse or promote products derived from
//    this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.
package com.github.mouse0w0.coffeemaker.template.tree.insn;

import com.github.mouse0w0.coffeemaker.evaluator.Evaluator;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A node that represents a stack map frame. These nodes are pseudo instruction nodes in order to be
 * inserted in an instruction list. In fact these nodes must(*) be inserted <i>just before</i> any
 * instruction node <b>i</b> that follows an unconditionnal branch instruction such as GOTO or
 * THROW, that is the target of a jump instruction, or that starts an exception handler block. The
 * stack map frame types must describe the values of the local variables and of the operand stack
 * elements <i>just before</i> <b>i</b> is executed. <br>
 * <br>
 * (*) this is mandatory only for classes whose version is greater than or equal to {@link
 * Opcodes#V1_6}.
 *
 * @author Eric Bruneton
 * @author Mouse0w0 (modify)
 */
public class BtFrame extends BtInsnBase {

    /**
     * The type of this frame. Must be {@link Opcodes#F_NEW} for expanded frames, or {@link
     * Opcodes#F_FULL}, {@link Opcodes#F_APPEND}, {@link Opcodes#F_CHOP}, {@link Opcodes#F_SAME} or
     * {@link Opcodes#F_APPEND}, {@link Opcodes#F_SAME1} for compressed frames.
     */
    public int type;

    /**
     * The types of the local variables of this stack map frame. Elements of this list can be Integer,
     * String or LabelNode objects (for primitive, reference and uninitialized types respectively -
     * see {@link MethodVisitor}).
     */
    public List<Object> local;

    /**
     * The types of the operand stack elements of this stack map frame. Elements of this list can be
     * Integer, String or LabelNode objects (for primitive, reference and uninitialized types
     * respectively - see {@link MethodVisitor}).
     */
    public List<Object> stack;

    private BtFrame() {
        super(-1);
    }

    /**
     * Constructs a new {@link BtFrame}.
     *
     * @param type     the type of this frame. Must be {@link Opcodes#F_NEW} for expanded frames, or
     *                 {@link Opcodes#F_FULL}, {@link Opcodes#F_APPEND}, {@link Opcodes#F_CHOP}, {@link
     *                 Opcodes#F_SAME} or {@link Opcodes#F_APPEND}, {@link Opcodes#F_SAME1} for compressed frames.
     * @param numLocal number of local variables of this stack map frame.
     * @param local    the types of the local variables of this stack map frame. Elements of this list
     *                 can be Integer, String or LabelNode objects (for primitive, reference and uninitialized
     *                 types respectively - see {@link MethodVisitor}).
     * @param numStack number of operand stack elements of this stack map frame.
     * @param stack    the types of the operand stack elements of this stack map frame. Elements of this
     *                 list can be Integer, String or LabelNode objects (for primitive, reference and
     *                 uninitialized types respectively - see {@link MethodVisitor}).
     */
    public BtFrame(
            final int type,
            final int numLocal,
            final Object[] local,
            final int numStack,
            final Object[] stack) {
        super(-1);
        this.type = type;
        switch (type) {
            case Opcodes.F_NEW:
            case Opcodes.F_FULL:
                this.local = asArrayList(numLocal, local);
                this.stack = asArrayList(numStack, stack);
                break;
            case Opcodes.F_APPEND:
                this.local = asArrayList(numLocal, local);
                break;
            case Opcodes.F_CHOP:
                this.local = asArrayList(numLocal);
                break;
            case Opcodes.F_SAME:
                break;
            case Opcodes.F_SAME1:
                this.stack = asArrayList(1, stack);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public int getType() {
        return FRAME;
    }

    @Override
    public void accept(final MethodVisitor methodVisitor, final Evaluator evaluator) {
        switch (type) {
            case Opcodes.F_NEW:
            case Opcodes.F_FULL:
                methodVisitor.visitFrame(type, local.size(), asArray(local), stack.size(), asArray(stack));
                break;
            case Opcodes.F_APPEND:
                methodVisitor.visitFrame(type, local.size(), asArray(local), 0, null);
                break;
            case Opcodes.F_CHOP:
                methodVisitor.visitFrame(type, local.size(), null, 0, null);
                break;
            case Opcodes.F_SAME:
                methodVisitor.visitFrame(type, 0, null, 0, null);
                break;
            case Opcodes.F_SAME1:
                methodVisitor.visitFrame(type, 0, null, 1, asArray(stack));
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public BtInsnBase clone(final Map<BtLabel, BtLabel> clonedLabels) {
        BtFrame clone = new BtFrame();
        clone.type = type;
        if (local != null) {
            clone.local = new ArrayList<>();
            for (int i = 0, n = local.size(); i < n; ++i) {
                Object localElement = local.get(i);
                if (localElement instanceof BtLabel) {
                    localElement = clonedLabels.get(localElement);
                }
                clone.local.add(localElement);
            }
        }
        if (stack != null) {
            clone.stack = new ArrayList<>();
            for (int i = 0, n = stack.size(); i < n; ++i) {
                Object stackElement = stack.get(i);
                if (stackElement instanceof BtLabel) {
                    stackElement = clonedLabels.get(stackElement);
                }
                clone.stack.add(stackElement);
            }
        }
        return clone;
    }

    private static List<Object> asArrayList(final int length) {
        return new ArrayList<>(length);
    }

    private static List<Object> asArrayList(final int length, final Object[] array) {
        List<Object> list = new ArrayList<>(length);
        for (int i = 0; i < length; ++i) {
            list.add(array[i]);
        }
        return list;
    }

    private static Object[] asArray(final List<Object> list) {
        Object[] array = new Object[list.size()];
        for (int i = 0, n = array.length; i < n; ++i) {
            Object o = list.get(i);
            if (o instanceof BtLabel) {
                o = ((BtLabel) o).getLabel();
            }
            array[i] = o;
        }
        return array;
    }
}