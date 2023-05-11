/*
 * This file is part of Titanium
 * Copyright (C) 2023, Horizon Studio <contact@hrznstudio.com>.
 *
 * This code is licensed under GNU Lesser General Public License v3.0, the full license text can be found in LICENSE.txt
 */

package com.hrznstudio.titanium.annotation.scanning;

import com.google.gson.*;

import java.lang.annotation.ElementType;
import java.lang.reflect.Type;
import java.util.HashMap;

public class AnnotationDataSerializer implements JsonSerializer<ModFileScanData.AnnotationData>, JsonDeserializer<ModFileScanData.AnnotationData> {

    @Override
    public JsonElement serialize(ModFileScanData.AnnotationData src, Type type, JsonSerializationContext ctx) {
        var object = new JsonObject();
        object.add("annotationType", ctx.serialize(src.annotationType()));
        object.add("targetType", ctx.serialize(src.targetType()));
        object.add("clazz", ctx.serialize(src.clazz()));
        object.add("memberName", ctx.serialize(src.memberName()));

        var annotationData = new JsonObject();
        annotationData.add("size", ctx.serialize(src.annotationData().size()));
        var annotationDataArray = new JsonArray();

        for (var entry : src.annotationData().entrySet()) {
            var value = entry.getValue();

            var mapEntry = new JsonObject();
            mapEntry.add("key", ctx.serialize(entry.getKey()));
            mapEntry.add("valueType", ctx.serialize(value.getClass().getName()));
            mapEntry.add("value", ctx.serialize(value));
            annotationDataArray.add(mapEntry);
        }

        annotationData.add("data", annotationDataArray);
        object.add("annotationData", annotationData);
        return object;
    }

    @Override
    public ModFileScanData.AnnotationData deserialize(JsonElement json, Type type, JsonDeserializationContext ctx) throws JsonParseException {
        try {
            var from = json.getAsJsonObject();
            var annotationType = (org.objectweb.asm.Type) ctx.deserialize(from.get("annotationType"), org.objectweb.asm.Type.class);
            var targetType = (ElementType) ctx.deserialize(from.get("targetType"), ElementType.class);
            var clazz = (org.objectweb.asm.Type) ctx.deserialize(from.get("clazz"), org.objectweb.asm.Type.class);
            var memberName = (String) ctx.deserialize(from.get("memberName"), String.class);

            var annotationData = new HashMap<String, Object>();
            var annotationDataJson = (JsonObject) ctx.deserialize(from.get("annotationData"), JsonObject.class);
            var annotationDataValues = (JsonArray) ctx.deserialize(annotationDataJson.get("data"), JsonArray.class);
            var size = (int) ctx.deserialize(annotationDataJson.get("size"), int.class);

            for (int i = 0; i < size; i++) {
                var jsonEntry = annotationDataValues.get(i).getAsJsonObject();
                var key = (String) ctx.deserialize(jsonEntry.get("key"), String.class);
                var valueType = Class.forName(ctx.deserialize(jsonEntry.get("valueName"), String.class));
                var value = ctx.deserialize(jsonEntry.get("value"), valueType);
                annotationData.put(key, value);
            }

            return new ModFileScanData.AnnotationData(annotationType, targetType, clazz, memberName, annotationData);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
