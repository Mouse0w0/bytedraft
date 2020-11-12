package com.github.mouse0w0.coffeemaker.template.impl2;

import com.github.mouse0w0.coffeemaker.EmptyEvaluator;
import com.github.mouse0w0.coffeemaker.template.*;
import com.github.mouse0w0.coffeemaker.template.impl2.tree.BtClass;
import com.github.mouse0w0.coffeemaker.template.impl2.tree.BtClassVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

public class TemplateParserImpl implements TemplateParser {
    private static final String TEMPLATE_ANNOTATION = Type.getDescriptor(TemplateClass.class);

    private final List<Handler> handlers = new ArrayList<>();

    public TemplateParserImpl() {
    }

    @Override
    public Template parse(InputStream in) throws IOException, TemplateParseException {
        ClassReader cr = new ClassReader(in);
        BtClassVisitor cv = new BtClassVisitor();
        cr.accept(cv, 0);
        BtClass btClass = cv.getBtClass();

        String name = btClass.get(BtClass.NAME).computeString(EmptyEvaluator.INSTANCE);

        if (btClass.findAnnotation(TEMPLATE_ANNOTATION) == null) {
            throw new InvalidTemplateException(name.replace('/', '.'));
        }

        return new TemplateImpl(name, btClass);
    }
}
