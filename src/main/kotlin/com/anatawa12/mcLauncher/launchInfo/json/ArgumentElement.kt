package com.anatawa12.mcLauncher.launchInfo.json

import com.anatawa12.mcLauncher.launchInfo.json.adapters.NonArrayIfSingle
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

sealed class ArgumentElement {
    object AdapterFactory : JsonAdapter.Factory {
        override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
            if (type == ArgumentElement::class.java) {
                return Adapter(moshi).nullSafe()
            }
            return null
        }
    }

    private class Adapter(moshi: Moshi) : JsonAdapter<ArgumentElement>() {
        val conditionalAdapter = moshi.adapter(ConditionalArgumentElement::class.java)

        @Synchronized
        @Throws(Exception::class)
        override fun fromJson(reader: JsonReader): ArgumentElement? {
            return when (reader.peek()) {
                JsonReader.Token.NULL -> reader.nextNull()
                JsonReader.Token.STRING -> StringArgumentElement(reader.nextString())
                else -> conditionalAdapter.fromJson(reader)
            }
        }

        @Synchronized
        @Throws(Exception::class)
        override fun toJson(writer: JsonWriter, value: ArgumentElement?) {
            when (value) {
                null -> {
                    writer.nullValue()
                }
                is StringArgumentElement -> {
                    writer.value(value.argument)
                }
                is ConditionalArgumentElement -> {
                    conditionalAdapter.toJson(writer, value)
                }
            }
        }

    }
}

data class StringArgumentElement(val argument: String) : ArgumentElement()

data class ConditionalArgumentElement(val rules: List<Rule>, @NonArrayIfSingle val value: List<String>) :
    ArgumentElement()
