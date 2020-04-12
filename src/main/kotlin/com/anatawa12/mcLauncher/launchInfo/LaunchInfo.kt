package com.anatawa12.mcLauncher.launchInfo

import com.anatawa12.mcLauncher.launchInfo.json.ArgumentElement
import com.anatawa12.mcLauncher.launchInfo.json.ClientJson
import com.anatawa12.mcLauncher.launchInfo.json.Logging
import com.anatawa12.mcLauncher.launchInfo.json.StringArgumentElement
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap

@Suppress("DataClassPrivateConstructor")
data class LaunchInfo private constructor(
    val id: String,
    val assets: String,
    val downloads: ImmutableMap<String, DownloadFile>,
    val libraries: ImmutableList<ImmutableList<Library>>,
    val logging: ImmutableMap<String, Logging>,
    val mainClass: String,
    val minecraftArguments: ImmutableList<ArgumentElement>,
    val jvmArguments: ImmutableList<ArgumentElement>? = null,
    val jar: String,
    val type: String
) {
    class Builder(jsonName: String) {
        var id: String? = null
        var assets: String? = null
        val downloads: MutableMap<String, DownloadFile> = mutableMapOf()
        val libraries: MutableList<ImmutableList<Library>> = mutableListOf()
        val logging: MutableMap<String, Logging> = mutableMapOf()
        var mainClass: String? = null
        var minecraftArguments: ImmutableList<ArgumentElement>? = null
        var jvmArguments: ImmutableList<ArgumentElement>? = null
        var jar: String = jsonName
        var type: String? = null

        fun addVersionJson(client: ClientJson) {
            client.downloads?.let {
                downloads += it.mapValues { DownloadFile(it.value) }
            }

            id = client.id
            assets = client.assets

            libraries.add(0, client.libraries.map { Library(it) }.toImmutableList())
            logging += client.logging.orEmpty()
            mainClass = client.mainClass
            minecraftArguments = client.arguments?.game?.toImmutableList()
                ?: client.minecraftArguments?.split(' ')?.map { StringArgumentElement(it) }?.toImmutableList()
                        ?: minecraftArguments
            jvmArguments = client.arguments?.jvm?.toImmutableList() ?: jvmArguments
            jar = client.jar ?: jar
            type = client.type
        }

        fun build(): LaunchInfo = LaunchInfo(
            id = requireNotNull(id) { "id is not set" },
            assets = requireNotNull(assets) { "assets is not set" },
            downloads = downloads.toImmutableMap(),
            libraries = libraries.toImmutableList(),
            logging = logging.toImmutableMap(),
            mainClass = requireNotNull(mainClass) { "mainClass is not set" },
            minecraftArguments = requireNotNull(minecraftArguments) { "arguments or minecraftArguments is not set" },
            jvmArguments = jvmArguments,
            jar = jar,
            type = requireNotNull(type) { "type is not set" }
        )
    }
}
