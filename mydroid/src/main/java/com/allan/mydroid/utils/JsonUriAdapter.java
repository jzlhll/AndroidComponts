package com.allan.mydroid.utils;

import com.google.gson.*;
import android.net.Uri;
import java.lang.reflect.Type;

public class JsonUriAdapter implements JsonSerializer<Uri>, JsonDeserializer<Uri> {
    
    @Override
    public JsonElement serialize(Uri src, Type typeOfSrc, JsonSerializationContext context) {
        // 序列化：Uri -> String
        return new JsonPrimitive(src.toString());
    }

    @Override
    public Uri deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        // 反序列化：String -> Uri
        try {
            return Uri.parse(json.getAsString());
        } catch (Exception e) {
            throw new JsonParseException("Invalid URI format: " + json, e);
        }
    }
}