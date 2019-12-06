package com.anatawa12.mcLauncher.json

import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okio.Buffer
import java.io.File

fun main() {
    val fileName = "1.7.10"
    //val fileName = "1.7.10-Forge10.13.4.1614-1.7.10"

    val adapter = Moshi.Builder()
        .add(DateJsonAdapter)
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter(VersionJson::class.java)

    val info = adapter.fromJson(File("./$fileName.json").readText())
    val buffer = Buffer()
    val writer = JsonWriter.of(buffer)

    writer.indent = "  "
    //writer.indent = "\t"

    adapter.toJson(writer, info)

    buffer.writeTo(File("./$fileName.e.json").outputStream())
}
