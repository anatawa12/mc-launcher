package com.anatawa12.mcLauncher

import com.anatawa12.mcLauncher.json.DateJsonAdapter
import com.anatawa12.mcLauncher.json.VersionJson
import com.anatawa12.mcLauncher.launchInfo.LaunchInfo
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

        println(loadLaunchInfo(args[0]))
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

        try {
            return versionJsonAdapter.fromJson(jsonFile.readText()) ?: knownError("$version.json is valid")
        } catch (e: FileNotFoundException) {
            knownError("$version.json is not a file.", e)
        }
    }
}