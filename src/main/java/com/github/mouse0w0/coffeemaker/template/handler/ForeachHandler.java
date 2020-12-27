package com.github.mouse0w0.coffeemaker.template.handler;

import com.github.mouse0w0.coffeemaker.template.Markers;
import com.github.mouse0w0.coffeemaker.template.tree.BtMethod;
import com.github.mouse0w0.coffeemaker.template.tree.insn.*;

import java.lang.reflect.Method;

public class ForeachHandler extends MethodInsnHandler {
    @Override
    protected Method[] getAcceptableMethods() {
        return new Method[]{
                Utils.getDeclaredMethod(Markers.class, "$foreach", String.class, String.class),
                Utils.getDeclaredMethod(Markers.class, "$endForeach")};
    }

    private BtInsnNode foreachInsn;

    @Override
    protected void handle(BtMethod method, BtInsnNode insn) {
        if ("$foreach".equals(insn.get(BtMethodInsn.NAME).getAsString())) {
            foreachInsn = insn;
        } else {
            BtInsnList instructions = method.getInstructions();
            BtLdcInsn arg1 = (BtLdcInsn) foreachInsn.getPrevious();
            BtLdcInsn arg0 = (BtLdcInsn) arg1.getPrevious();
            BtInsnList insnList = Utils.subInsnList(instructions, foreachInsn.getNextLabel(), insn.getPreviousLabel());
            BtLabel injectPoint = insn.getNextLabel();
            Utils.removeRange(instructions, foreachInsn.getPreviousLabel(), injectPoint);
            instructions.insertBefore(injectPoint, new BtForeach(arg0.getAsString(), arg1.getAsString(), insnList));
        }
    }
}
