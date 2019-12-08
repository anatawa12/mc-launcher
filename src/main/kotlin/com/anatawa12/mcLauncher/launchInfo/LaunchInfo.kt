package com.anatawa12.mcLauncher.launchInfo

import com.anatawa12.mcLauncher.launchInfo.json.Logging
import com.anatawa12.mcLauncher.launchInfo.json.VersionJson
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap

@Suppress("DataClassPrivateConstructor")
data class LaunchInfo private constructor(
    var id: String,
    val downloads: ImmutableMap<String, DownloadFile>,
    val libraries: ImmutableList<ImmutableList<Library>>,
    val logging: ImmutableMap<String, Logging>,
    var mainClass: String,
    var minecraftArguments: String,
    var jar: String
) {
    class Builder(jsonName: String) {
        var id: String? = null
        val downloads: MutableMap<String, DownloadFile> = mutableMapOf()
        val libraries: MutableList<ImmutableList<Library>> = mutableListOf()
        val logging: MutableMap<String, Logging> = mutableMapOf()
        var mainClass: String? = null
        var minecraftArguments: String? = null
        var jar: String = jsonName

        fun addVersionJson(version: VersionJson) {
            version.downloads?.let {
                downloads += it.mapValues { DownloadFile(it.value) }
            }

            id = version.id

            libraries.add(0, version.libraries.map { Library(it) }.toImmutableList())
            logging += logging
            mainClass = version.mainClass
            minecraftArguments = version.minecraftArguments
            jar = version.jar ?: jar
        }

        fun build(): LaunchInfo = LaunchInfo(
            id = requireNotNull(id) { "id is not set" },
            downloads = downloads.toImmutableMap(),
            libraries = libraries.toImmutableList(),
            logging = logging.toImmutableMap(),
            mainClass = requireNotNull(mainClass) { "mainClass is not set" },
            minecraftArguments = requireNotNull(minecraftArguments) { "minecraftArguments is not set" },
            jar = jar
        )
    }
}
