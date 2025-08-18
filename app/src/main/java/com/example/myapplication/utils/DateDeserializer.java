package com.example.myapplication.utils;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateDeserializer implements JsonDeserializer<Date> {

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        try {
            // 处理Unix时间戳（毫秒）
            if (json.getAsString().matches("\\d+")) {
                return new Date(json.getAsLong());
            }

            // 处理ISO 8601格式（备用）
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    .parse(json.getAsString());
        } catch (Exception e) {
            throw new JsonParseException("日期解析失败: " + json.getAsString(), e);
        }
    }
}