/*
 * This file is part of Titanium
 * Copyright (C) 2023, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.annotation.scanning;

import java.lang.annotation.ElementType;
import java.util.*;
import java.util.function.Predicate;

import org.objectweb.asm.Type;

public class ModFileScanData {
    private final Set<AnnotationData> annotations = new LinkedHashSet<>();
    private final Set<ClassData> classes = new LinkedHashSet<>();
    private Map<String,?> functionalScanners;

    public static Predicate<Type> interestingAnnotations() {
        return t->true;
    }

    public Set<ClassData> getClasses() {
        return classes;
    }

    public Set<AnnotationData> getAnnotations() {
        return annotations;
    }

    public record ClassData(Type clazz, Type parent, Set<Type> interfaces) {}

    public record AnnotationData(Type annotationType, ElementType targetType, Type clazz, String memberName, Map<String, Object> annotationData) {}
}
