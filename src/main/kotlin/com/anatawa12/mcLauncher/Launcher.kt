package com.anatawa12.mcLauncher

import com.anatawa12.mcLauncher.launchInfo.Artifact
import com.anatawa12.mcLauncher.launchInfo.LaunchInfo
import com.anatawa12.mcLauncher.launchInfo.Library
import com.anatawa12.mcLauncher.launchInfo.Natives
import com.anatawa12.mcLauncher.launchInfo.json.*
import com.anatawa12.mcLauncher.launchInfo.json.adapters.DateJsonAdapter
import com.anatawa12.mcLauncher.launchInfo.json.adapters.NonArrayIfSingleAdapterFactory
import com.google.gson.GsonBuilder
import com.mojang.authlib.properties.PropertyMap
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.collections.immutable.persistentListOf
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URL
import java.util.zip.ZipInputStream
import kotlin.random.Random
import kotlin.system.exitProcess

class Launcher(
    val profile: Profile
) {
    val appDataDir: File = File(profile.appDataDirPath)
    val platform = profile.platform
    lateinit var info: LaunchInfo
    lateinit var nativeLibraryDirName: String
    lateinit var loggingFilePath: String
    lateinit var loginer: Loginer

    //region loadLaunchInfo

    fun loadLaunchInfo(version: String): LaunchInfo {
        val loadedVersions = mutableListOf<ClientJson>()
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

    fun loadVersionJson(version: String): ClientJson {
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
    private fun classifier(natives: Natives): String = when (platform.os) {
        Platform.OperatingSystem.Linux -> natives.linux
        Platform.OperatingSystem.MacOS -> natives.osx
        Platform.OperatingSystem.Windows -> natives.windows
    }

    private fun checkRule(rule: Rule): Boolean {
        if (rule.os != null) {
            val os = rule.os
            when (os.name) {
                "osx" -> if (platform.os != Platform.OperatingSystem.MacOS) return false
                "windows" -> if (platform.os != Platform.OperatingSystem.MacOS) return false
            }
            if (os.version != null) {
                if (!os.version.toRegex().matches(platform.version)) return false
            }
            when (os.arch) {
                "x86" -> if (platform.arch != Platform.Architecture.X86) return false
            }
        }
        if (rule.features != null) {
            // TODO: use real value
            if (rule.features.has_custom_resolution != false)
                return false
            if (rule.features.is_demo_user != false)
                return false
        }
        return true
    }

    private fun checkRules(rules: Collection<Rule>): Boolean {
        if (rules.isEmpty()) return true
        var allow = false
        for (rule in rules) {
            if (checkRule(rule)) {
                when (rule.action) {
                    RuleAction.allow -> allow = true
                    RuleAction.disallow -> allow = false
                }
            }
        }
        return allow
    }

    private fun Sequence<Library>.filterWithRule(): Sequence<Library> = filter { checkRules(it.rules) }

    fun getLoadArtifacts(): Sequence<Artifact> {
        return info.libraries
            .asSequence()
            .map { inList ->
                inList
                    .asSequence()
                    .filterWithRule()
                    .filter { it.extract == null }
                    .map {
                        it.downloads[classifier(it.natives)]
                            ?: throw KnownErrorException.InvalidLibraries(
                                it.name,
                                "download for this platform not found."
                            )
                    }
                    .groupBy { File(it.path).parentFile.parent }
                    .map { it.value.minBy { File(it.path).parentFile.name }!! }
            }
            .flatMap { it.asSequence() }
    }

    //endregion

    //region preparing

    fun downloadCheck(path: String, url: String, sha1: String?, size: Int?) {
        val file = appDataDir.resolve(path)
        var needInstall = false
        if (file.exists()) {
            val fileData = file.readBytes()
            needInstall = !verify(fileData, sha1, size)
        } else {
            needInstall = true
        }

        if (!needInstall) return

        val data = try {
            URL(url).openStream().readBytes()
        } catch (e: IOException) {
            throw KnownErrorException.InvalidLibrary(path, "can't get library", e)
        }

        if (!verify(data, sha1, size))
            throw KnownErrorException.InvalidLibrary(path, "verify failed.")

        file.parentFile.mkdirs()
        file.writeBytes(data)
    }

    fun verify(data: ByteArray, sha1: String?, size: Int?): Boolean {
        if (size != null)
            if (data.size != size)
                return false
        if (sha1 != null)
            if (DigestUtils.sha1Hex(data) != sha1)
                return false
        return true
    }

    fun prepareLibraries() {
        val libraries = appDataDir.resolve("libraries")
        for (artifact in getLoadArtifacts()) {
            downloadCheck("$libraries/${artifact.path}", artifact.url, artifact.sha1, artifact.size)
        }
    }

    fun prepareNativeLibraries() {
        nativeLibraryDirName = Random.nextLong().run {
            "%04x-%04x-%04x-%04x".format(
                ushr(16 * 3).and(0xFFFF).toInt(),
                ushr(16 * 2).and(0xFFFF).toInt(),
                ushr(16 * 1).and(0xFFFF).toInt(),
                ushr(16 * 0).and(0xFFFF).toInt()
            )
        }
        val libraries = appDataDir.resolve("libraries")
        val extractTo = File("$appDataDir/bin/$nativeLibraryDirName")
        val artifacts = info.libraries
            .asSequence()
            .map { inList ->
                inList
                    .asSequence()
                    .filterWithRule()
                    .filter { it.natives.linux != "" || it.natives.osx != "" || it.natives.windows != "" }
                    .mapNotNull {
                        it.downloads[classifier(it.natives)]?.let { dl -> it to dl }
                    }
                    .groupBy { (_, it) -> File(it.path).parentFile.parent }
                    .map { it.value.minBy { (_, it) -> File(it.path).parentFile.name }!! }
            }
            .flatMap { it.asSequence() }

        for ((library, artifact) in artifacts) {
            downloadCheck("$libraries/${artifact.path}", artifact.url, artifact.sha1, artifact.size)
            val extract = library.extract
            ZipInputStream(File("$libraries/${artifact.path}").inputStream()).use { zis ->
                while (true) {
                    val entry = zis.nextEntry ?: break
                    if (entry.isDirectory) continue
                    if (extract?.exclude.orEmpty().any { entry.name.startsWith(it) }) continue
                    extractTo.resolve(entry.name)
                        .apply { parentFile.mkdirs() }
                        .outputStream()
                        .use { zis.copyTo(it) }
                }
            }
        }
    }

    fun prepareLog() {
        val logging = info.logging["client"] ?: return
        loggingFilePath = "$appDataDir/assets/log_configs/${logging.file.id}"
        downloadCheck(loggingFilePath, logging.file.url, logging.file.sha1, logging.file.size)
    }

    fun prepare() {
        loginer.login()
        prepareLibraries()
        prepareNativeLibraries()
        prepareLog()
    }

    //endregion

    //region jvm arguments

    fun createClassPath(): String = buildString {
        val librariesDir = appDataDir.resolve("libraries")
        getLoadArtifacts()
            .map { librariesDir.resolve(it.path) }
            .joinTo(this, separator = File.pathSeparator, postfix = File.pathSeparator)
        append(appDataDir.resolve("versions/${info.jar}/${info.jar}.jar").path)
    }

    val launcherBrand = "anatawa12-mc-launcher"
    val launcherVersion = "0.0.0"

    fun logJvmArguments(): List<String> {
        val logging = info.logging["client"] ?: return listOf()
        return listOf(logging.argument.replace("\${path}", loggingFilePath))
    }

    fun processArguments(arguments: List<ArgumentElement>): List<String> {
        val auth = loginer.auth
        val selectedProfile = auth.selectedProfile
        val map = mapOf(
            "auth_player_name" to selectedProfile.name,
            "version_name" to info.id,
            "game_directory" to profile.gameDirPath,
            "assets_root" to "$appDataDir/assets",
            "assets_index_name" to info.assets,
            "auth_uuid" to selectedProfile.id.toString().replace("-", ""),
            "auth_access_token" to auth.authenticatedToken,
            "user_properties" to GsonBuilder().registerTypeAdapter(
                PropertyMap::class.java,
                OldPropertyMapSerializer()
            ).create().toJson(auth.userProperties),
            "user_type" to auth.userType.getName(),
            "version_type" to info.type,

            // slince 1.13(launcher v21)
            "resolution_width" to null,
            "resolution_height" to null,
            "natives_directory" to "$appDataDir/bin/$nativeLibraryDirName",
            "launcher_name" to launcherBrand,
            "launcher_version" to launcherVersion,
            "classpath" to createClassPath(),

            // internal use
            "anatawa12_client_jar" to appDataDir.resolve("versions/${info.jar}/${info.jar}.jar").toString()
        )

        return arguments
            .flatMap { processArgumentElement(it) }
            .map { it.replace("""\$\{(.*?)\}""".toRegex(), mapTransformer(map)) }
    }

    fun processArgumentElement(element: ArgumentElement) = when (element) {
        is StringArgumentElement -> listOf(element.argument)
        is ConditionalArgumentElement -> if (checkRules(element.rules)) element.value else emptyList()
    }

    fun jvmArguments(): List<String> {
        val list = mutableListOf<String>()

        list += processArguments(info.jvmArguments ?: untilV21JvmArguments)
        list += profile.jvmArguments
        list += logJvmArguments()
        list += info.mainClass
        list += processArguments(info.minecraftArguments)

        return list
    }

    //endregion

    fun launch() {
        info = loadLaunchInfo(profile.version)

        prepare()

        println(jvmArguments())
        println("${System.getProperty("java.home")}/bin/java")
        val builder = ProcessBuilder()
        builder.command(mutableListOf<String>().apply {
            this += "${System.getProperty("java.home")}/bin/java"
            this += jvmArguments()
        })
        builder.inheritIO()
        exitProcess(builder.start().waitFor())
    }

    companion object {
        val moshi = Moshi.Builder()
            .add(DateJsonAdapter)
            .add(NonArrayIfSingleAdapterFactory)
            .add(ArgumentElement.AdapterFactory)
            .add(KotlinJsonAdapterFactory())
            .build()

        val versionJsonAdapter = moshi.adapter(ClientJson::class.java)

        fun mapTransformer(map: Map<String, CharSequence?>): (MatchResult) -> CharSequence = { result ->
            map[result.groupValues[1]] ?: error("unknown: ${result.groupValues[1]}")
        }

        val untilV21JvmArguments = persistentListOf(
            ConditionalArgumentElement(
                listOf(
                    Rule(
                        action = RuleAction.allow,
                        os = RuleOS(
                            name = "osx"
                        )
                    )
                ),
                listOf(
                    "-Xdock:name=Minecraft",
                    "-Xdock:icon=\${assets_root}/objects/99/991b421dfd401f115241601b2b373140a8d78572"
                )
            ),
            ConditionalArgumentElement(
                listOf(
                    Rule(
                        action = RuleAction.allow,
                        os = RuleOS(
                            name = "windows",
                            version = """^10\\."""
                        )
                    )
                ),
                listOf(
                    "-Dos.name=Windows 10",
                    "-Dos.version=10.0"
                )
            ),
            ConditionalArgumentElement(
                listOf(
                    Rule(
                        action = RuleAction.allow,
                        os = RuleOS(
                            name = "windows"
                        )
                    )
                ),
                listOf(
                    "-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump"
                )
            ),
            ConditionalArgumentElement(
                listOf(
                    Rule(
                        action = RuleAction.allow,
                        os = RuleOS(
                            arch = "x86"
                        )
                    )
                ),
                listOf(
                    "-Xss1M"
                )
            ),
            StringArgumentElement("-Djava.library.path=\${natives_directory}"),
            StringArgumentElement("-Dminecraft.launcher.brand=\${launcher_name}"),
            StringArgumentElement("-Dminecraft.launcher.version=\${launcher_version}"),
            StringArgumentElement("-Dminecraft.client.jar=\${anatawa12_client_jar}"),
            StringArgumentElement("-cp"),
            StringArgumentElement("-\${classpath}")
        )
    }
}
