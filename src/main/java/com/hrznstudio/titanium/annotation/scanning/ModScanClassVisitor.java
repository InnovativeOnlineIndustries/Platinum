/*
 * This file is part of Titanium
 * Copyright (C) 2023, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.annotation.scanning;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModScanClassVisitor extends ClassVisitor {
    private final ModFileScanData data;
    public ModScanClassVisitor(ModFileScanData data) {
        super(Opcodes.ASM9);
        this.data = data;
    }

    private Type asmType;
    private Type asmSuperType;
    private Set<Type> interfaces;
    private final LinkedList<AnnotationBuilder> annotations = new LinkedList<>();

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.asmType = Type.getObjectType(name);
        this.asmSuperType = superName != null && superName.length() > 0 ? Type.getObjectType(superName) : null;
        this.interfaces = Stream.of(interfaces).map(Type::getObjectType).collect(Collectors.toSet());
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        AnnotationBuilder builder = new AnnotationBuilder(ElementType.TYPE, Type.getType(descriptor), this.asmType.getClassName());
        this.annotations.addFirst(builder);
        return new ModScanAnnotationVisitor(annotations, builder);
    }

    public void buildModData() {
        data.getClasses().add(new ModFileScanData.ClassData(asmType, asmSuperType, interfaces));
        this.annotations.forEach(annotationBuilder -> data.getAnnotations().add(annotationBuilder.build(asmType)));
    }
}
