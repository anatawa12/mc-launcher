package com.anatawa12.mcLauncher

import com.anatawa12.mcLauncher.launchInfo.Artifact
import com.anatawa12.mcLauncher.launchInfo.LaunchInfo
import com.anatawa12.mcLauncher.launchInfo.Natives
import com.anatawa12.mcLauncher.launchInfo.json.DateJsonAdapter
import com.anatawa12.mcLauncher.launchInfo.json.VersionJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.io.FileNotFoundException

class Launcher(
    val profile: Profile
) {
    val appDataDir: File = File(profile.appDataDirPath)
    val platform: Platform = profile.platform
    lateinit var info: LaunchInfo
    lateinit var nativeLibraryDirName: String

    fun loadLaunchInfo(version: String): LaunchInfo {
        val loadedVersions = mutableListOf<VersionJson>()
        var versionJsonVersion: String? = version
        while (versionJsonVersion != null) {
            if (loadedVersions.any { it.id == versionJsonVersion })
                throw KnownErrorException.InvalidVersionJsonData(version)

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
            throw KnownErrorException.VersionJsonNotFile(version, e)
        }

        try {
            return versionJsonAdapter.fromJson(jsonText) ?: throw KnownErrorException.InvalidVersionJson(version)
        } catch (e: JsonEncodingException) {
            throw KnownErrorException.InvalidVersionJson(version, e)
        } catch (e: JsonDataException) {
            throw KnownErrorException.InvalidVersionJsonData(version, e)
        }
    }

    // TODO Support ${arch}
    private fun classifier(natives: Natives): String = when (platform) {
        Platform.Linux -> natives.linux
        Platform.MacOS -> natives.osx
        Platform.Windows -> natives.windows
    }

    fun getLoadArtifacts(): Sequence<Artifact> {
        return info.libraries
            .asSequence()
            .map { inList ->
                inList
                    .asSequence()
                    .filter { it.extract == null }
                    .map {
                        it.downloads[classifier(it.natives)]
                            ?: throw KnownErrorException.InvalidVersionJsonData("downloads invalid")
                    }
                    .groupBy { File(it.path).parentFile.parent }
                    .map { it.value.minBy { File(it.path).parentFile.name }!! }
            }
            .flatMap { it.asSequence() }
    }

    fun createClassPath(): String = buildString {
        val librariesDir = appDataDir.resolve("libraries")
        getLoadArtifacts()
            .map { librariesDir.resolve(it.path) }
            .joinTo(this, separator = File.pathSeparator, postfix = File.pathSeparator)
        append(appDataDir.resolve("versions/${info.jar}/${info.jar}.jar").path)
    }

    fun osJvmArgument(): List<String> {
        when (platform) {
            Platform.Linux -> TODO()
            Platform.MacOS -> {
                return listOf(
                    "-Xdock:name=Minecraft",
                    "-Xdock:icon=$appDataDir/assets/objects/99/991b421dfd401f115241601b2b373140a8d78572"
                )
            }
            Platform.Windows -> {
                return listOf(
                    "-Dos.name=Windows 10",
                    "-Dos.version=10.0",
                    "-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump"
                )
            }
        }
    }

    fun prefixedJvmArguments(): List<String> {
        return listOf(
            "-Djava.library.path=$appDataDir/bin/$nativeLibraryDirName",
            "-Dminecraft.launcher.brand=anatawa12-mc-launcher",
            "-Dminecraft.launcher.version=0.0.0",
            "-Dminecraft.client.jar=${appDataDir.resolve("versions/${info.jar}/${info.jar}.jar")}"
        )
    }

    fun launch() {
        info = loadLaunchInfo(profile.version)

        println(createClassPath())
    }

    companion object {
        val moshi = Moshi.Builder()
            .add(DateJsonAdapter)
            .add(KotlinJsonAdapterFactory())
            .build()

        val versionJsonAdapter = moshi.adapter(VersionJson::class.java)
    }
}
