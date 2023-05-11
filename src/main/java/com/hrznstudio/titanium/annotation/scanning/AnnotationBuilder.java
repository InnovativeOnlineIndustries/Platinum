/*
 * This file is part of Titanium
 * Copyright (C) 2023, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.annotation.scanning;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.Map;

public class AnnotationBuilder {
    private final ElementType type;
    private final Type asmType;
    private final String member;
    private final Map<String,Object> values = Maps.newHashMap();

    public record EnumHolder(String desc, String value) {}

    private ArrayList<Object> arrayList;
    private String arrayName;

    public AnnotationBuilder(ElementType type, Type asmType, String member) {
        this.type = type;
        this.asmType = asmType;
        this.member = member;
    }

    public AnnotationBuilder(Type asmType, AnnotationBuilder parent) {
        this.type = parent.type;
        this.asmType = asmType;
        this.member = parent.member;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void addEnumProperty(String key, String enumName, String value) {
        addProperty(key, new EnumHolder(enumName, value));
    }

    public void addArray(String name) {
        this.arrayList = Lists.newArrayList();
        this.arrayName = name;
    }

    public void addProperty(String key, Object value) {
        if (this.arrayList != null) {
            arrayList.add(value);
        } else {
            values.put(key, value);
        }
    }

    public void endArray() {
        values.put(arrayName, arrayList);
        arrayList = null;
    }

    public AnnotationBuilder addChildAnnotation(String name, String desc) {
        AnnotationBuilder child = new AnnotationBuilder(Type.getType(desc), this);
        addProperty(name, child.getValues());
        return child;
    }


    public ModFileScanData.AnnotationData build(final Type clazz) {
        return new ModFileScanData.AnnotationData(asmType, type, clazz, member, values);
    }
}
