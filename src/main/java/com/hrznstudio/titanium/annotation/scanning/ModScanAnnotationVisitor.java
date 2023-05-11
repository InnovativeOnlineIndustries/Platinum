/*
 * This file is part of Titanium
 * Copyright (C) 2023, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.annotation.scanning;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

import java.util.LinkedList;

public class ModScanAnnotationVisitor extends AnnotationVisitor {
    private final AnnotationBuilder annotation;
    private LinkedList<AnnotationBuilder> annotations;
    private boolean array;
    private String name;
    private boolean isSubAnnotation;

    public ModScanAnnotationVisitor(LinkedList<AnnotationBuilder> annotations, AnnotationBuilder annotation) {
        super(Opcodes.ASM9);
        this.annotations = annotations;
        this.annotation = annotation;
    }

    public ModScanAnnotationVisitor(LinkedList<AnnotationBuilder> annotations, AnnotationBuilder annotation, String name) {
        this(annotations, annotation);
        this.array = true;
        this.name = name;
        annotation.addArray(name);
    }

    public ModScanAnnotationVisitor(LinkedList<AnnotationBuilder> annotations, AnnotationBuilder annotation, boolean isSubAnnotation) {
        this(annotations, annotation);
        this.isSubAnnotation = true;
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        return new ModScanAnnotationVisitor(annotations, annotation, name);
    }

    @Override
    public void visitEnum(String name, String desc, String value)
    {
        annotation.addEnumProperty(name, desc, value);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String desc) {
        AnnotationBuilder ma = annotations.getFirst();
        final AnnotationBuilder childAnnotation = ma.addChildAnnotation(name, desc);
        annotations.addFirst(childAnnotation);
        return new ModScanAnnotationVisitor(annotations, childAnnotation,true);
    }

    @Override
    public void visit(String name, Object value) {
        annotation.addProperty(name, value);
    }

    @Override
    public void visitEnd() {
        if (array) {
            annotation.endArray();
        }

        if (isSubAnnotation) {
            AnnotationBuilder child = annotations.removeFirst();
            annotations.addLast(child);
        }
    }
}
