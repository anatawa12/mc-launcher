package com.anatawa12.mcLauncher.launchInfo

import com.anatawa12.mcLauncher.launchInfo.json.ClientJson
import com.anatawa12.mcLauncher.launchInfo.json.Logging
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap

@Suppress("DataClassPrivateConstructor")
data class LaunchInfo private constructor(
    val id: String,
    val downloads: ImmutableMap<String, DownloadFile>,
    val libraries: ImmutableList<ImmutableList<Library>>,
    val logging: ImmutableMap<String, Logging>,
    val mainClass: String,
    val minecraftArguments: String,
    val jar: String,
    val type: String
) {
    class Builder(jsonName: String) {
        var id: String? = null
        val downloads: MutableMap<String, DownloadFile> = mutableMapOf()
        val libraries: MutableList<ImmutableList<Library>> = mutableListOf()
        val logging: MutableMap<String, Logging> = mutableMapOf()
        var mainClass: String? = null
        var minecraftArguments: String? = null
        var jar: String = jsonName
        var type: String? = null

        fun addVersionJson(client: ClientJson) {
            client.downloads?.let {
                downloads += it.mapValues { DownloadFile(it.value) }
            }

            id = client.id

            libraries.add(0, client.libraries.map { Library(it) }.toImmutableList())
            logging += client.logging.orEmpty()
            mainClass = client.mainClass
            minecraftArguments = client.minecraftArguments
            jar = client.jar ?: jar
            type = client.type
        }

        fun build(): LaunchInfo = LaunchInfo(
            id = requireNotNull(id) { "id is not set" },
            downloads = downloads.toImmutableMap(),
            libraries = libraries.toImmutableList(),
            logging = logging.toImmutableMap(),
            mainClass = requireNotNull(mainClass) { "mainClass is not set" },
            minecraftArguments = requireNotNull(minecraftArguments) { "minecraftArguments is not set" },
            jar = jar,
            type = requireNotNull(type) { "type is not set" }
        )
    }
}
