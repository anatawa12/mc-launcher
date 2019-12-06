package com.anatawa12.mcLauncher

import com.anatawa12.mcLauncher.json.DateJsonAdapter
import com.anatawa12.mcLauncher.json.VersionJson
import com.anatawa12.mcLauncher.launchInfo.LaunchInfo
import com.anatawa12.mcLauncher.launchInfo.Natives
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.io.FileNotFoundException

object Main {
    val moshi = Moshi.Builder()
        .add(DateJsonAdapter)
        .add(KotlinJsonAdapterFactory())
        .build()

    val versionJsonAdapter = moshi.adapter(VersionJson::class.java)

    lateinit var homeDir: File
        private set
    lateinit var appDataDir: File
        private set

    @JvmStatic
    fun main(args: Array<String>) {
        init()

        val info = loadLaunchInfo(args[0])

        println(info)
        println(createArguments(info))
    }

    private fun init() {
        homeDir = File(System.getProperty("user.home"))
        val appDataDirPath: String
        when (Platform.current) {
            Platform.Linux -> {
                appDataDirPath = "$homeDir/.minecraft"
            }
            Platform.MacOS -> {
                appDataDirPath = "$homeDir/Library/Application Support/minecraft"
            }
            Platform.Windows -> {
                appDataDirPath = "${System.getenv("APPDATA")}\\.minecraft"
            }
        }
        appDataDir = File(appDataDirPath)
    }

    fun loadLaunchInfo(version: String): LaunchInfo {
        val loadedVersions = mutableListOf<VersionJson>()
        var versionJsonVersion: String? = version
        while (versionJsonVersion != null) {
            if (loadedVersions.any { it.id == versionJsonVersion })
                knownError("'inheritsFrom' is looping! entry version is '$version'")

            val loadedVersionJson = loadVersionJson(versionJsonVersion)
            loadedVersions += loadedVersionJson

            versionJsonVersion = loadedVersionJson.inheritsFrom
        }

        val builder = LaunchInfo.Builder(version)

        loadedVersions.reversed().forEach(builder::addVersionJson)

        return builder.build()
    }

    fun loadVersionJson(version: String): VersionJson {
        val jsonFile = appDataDir.resolve("versions").resolve(version).resolve("$version.json")

        val jsonText = try {
            jsonFile.readText()
        } catch (e: FileNotFoundException) {
            knownError("$version.json is not a file.", e)
        }

        try {
            return versionJsonAdapter.fromJson(jsonText) ?: knownError("$version.json is valid")
        } catch (e: JsonEncodingException) {
            knownError("$version.json is valid", e)
        } catch (e: JsonDataException) {
            knownError("$version.json is valid", e)
        }
    }

    // TODO Support ${arch}
    fun classifier(natives: Natives): String = when (Platform.current) {
        Platform.Linux -> natives.linux
        Platform.MacOS -> natives.osx
        Platform.Windows -> natives.windows
    }

    fun createArguments(info: LaunchInfo): String = buildString {
        val librariesDir = appDataDir.resolve("libraries")
        append("-cp ")
        info.libraries
            .asSequence()
            .filter { it.extract == null }
            .map { it.downloads[classifier(it.natives)] ?: knownError("downloads invalid") }
            .map { librariesDir.resolve(it.path) }
            .joinTo(this, separator = File.pathSeparator, postfix = File.pathSeparator)
        append("$appDataDir/versions/${info.jar}/${info.jar}.jar")
    }
}
