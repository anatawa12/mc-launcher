package com.anatawa12.mcLauncher

import com.google.gson.*
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import java.lang.reflect.Type

class OldPropertyMapSerializer : JsonSerializer<PropertyMap> {
    override fun serialize(
        var1: PropertyMap,
        var2: Type,
        var3: JsonSerializationContext
    ): JsonElement {
        val var4 = JsonObject()
        val var5: Iterator<*> = var1.keySet().iterator()
        while (var5.hasNext()) {
            val var6 = var5.next() as String
            val var7 = JsonArray()
            val var8: Iterator<*> = var1[var6].iterator()
            while (var8.hasNext()) {
                val var9 = var8.next() as Property
                var7.add(JsonPrimitive(var9.value))
            }
            var4.add(var6, var7)
        }
        return var4
    }
}
