package com.anatawa12.mcLauncher.launchInfo.json.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

object DateJsonAdapter : JsonAdapter<Date>(), JsonAdapter.Factory {
    private val sdFormat0 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.JAPAN)
    private val sdFormat1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.JAPAN)

    init {
        sdFormat0.timeZone = TimeZone.getTimeZone("GMT")
        sdFormat1.timeZone = TimeZone.getTimeZone("GMT")
    }

    @Synchronized
    @Throws(Exception::class)
    override fun fromJson(reader: JsonReader): Date {
        val string = reader.nextString()
        if (string[22] == ':')
            return sdFormat0.parse(string)
        else
            return sdFormat1.parse(string)
    }

    @Synchronized
    @Throws(Exception::class)
    override fun toJson(writer: JsonWriter, value: Date?) {
        writer.value(sdFormat0.format(value).replace("Z", "+00:00"))
    }

    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type == Date::class.java) {
            return DateJsonAdapter
        }
        return null
    }
}
