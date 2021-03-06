package com.github.mouse0w0.coffeemaker.template.handler;

import com.github.mouse0w0.coffeemaker.evaluator.Evaluator;
import com.github.mouse0w0.coffeemaker.evaluator.LocalVar;
import com.github.mouse0w0.coffeemaker.template.TemplateProcessException;
import com.github.mouse0w0.coffeemaker.template.tree.insn.BtInsnList;
import com.github.mouse0w0.coffeemaker.template.tree.insn.BtInsnNode;
import com.github.mouse0w0.coffeemaker.template.tree.insn.BtLabel;
import org.objectweb.asm.MethodVisitor;

import java.util.Map;

public final class BtForeach extends BtInsnNode {
    private final String iterable;
    private final String variableName;
    private final BtInsnList insnList;

    public BtForeach(String iterable, String variableName, BtInsnList insnList) {
        this.iterable = iterable;
        this.variableName = variableName;
        this.insnList = insnList;
    }

    @Override
    public void accept(MethodVisitor methodVisitor, Evaluator evaluator) {
        Object value = evaluator.eval(iterable);
        if (value == null) {
            throw new TemplateProcessException("The value of iterable \"" + iterable + "\" cannot be null");
        }
        Class<?> type = value.getClass();
        if (Iterable.class.isAssignableFrom(type)) {
            accept(methodVisitor, evaluator, (Iterable<?>) value);
        } else if (type.isArray()) {
            Class<?> componentType = type.getComponentType();
            if (!componentType.isPrimitive() && !componentType.isArray()) {
                accept(methodVisitor, evaluator, (Object[]) value);
            }
        } else {
            accept(methodVisitor, evaluator, value);
        }
    }

    private void accept(MethodVisitor methodVisitor, Evaluator evaluator, Iterable<?> iterable) {
        for (Object value : iterable) {
            accept(methodVisitor, evaluator, value);
        }
    }

    private void accept(MethodVisitor methodVisitor, Evaluator evaluator, Object[] array) {
        for (Object value : array) {
            accept(methodVisitor, evaluator, value);
        }
    }

    private void accept(MethodVisitor methodVisitor, Evaluator evaluator, Object value) {
        insnList.resetLabels();
        try (LocalVar localVar = evaluator.pushLocalVar()) {
            localVar.put(variableName, value);
            insnList.accept(methodVisitor, evaluator);
        }
    }

    @Override
    public BtInsnNode clone(Map<BtLabel, BtLabel> clonedLabels) {
        return new BtForeach(iterable, variableName, insnList);
    }
}
