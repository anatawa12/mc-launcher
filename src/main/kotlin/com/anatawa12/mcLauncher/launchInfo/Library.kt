package com.anatawa12.mcLauncher.launchInfo

import com.anatawa12.mcLauncher.json.Extract
import com.anatawa12.mcLauncher.json.Rule
import kotlinx.collections.immutable.*
import com.anatawa12.mcLauncher.json.Library as JsonLibrary

data class Library(
    val downloads: ImmutableMap<String, Artifact>,
    val extract: Extract?,
    val name: String,
    val natives: Natives,
    val rules: ImmutableList<Rule>,
    val serverreq: Boolean,
    val clientreq: Boolean
) {
    constructor(json: JsonLibrary) : this(
        downloads = downloads(json),
        extract = json.extract,
        name = json.name,
        natives = json.natives?.let(::Natives) ?: Natives.DEFAULT_NATIVES,
        rules = json.rules?.toImmutableList() ?: persistentListOf(),
        serverreq = json.serverreq ?: true,
        clientreq = json.clientreq ?: true
    )

    companion object {
        private const val DEFAULT_BASE_URL = "https://libraries.minecraft.net/"

        private fun downloads(json: JsonLibrary): ImmutableMap<String, Artifact> {
            if (json.downloads != null) {
                return json.downloads.classifiers?.mapValues { Artifact(it.value) }?.toImmutableMap()
                    ?: persistentMapOf("" to Artifact(json.downloads.artifact!!))
            } else {
                val name = json.name

                val elements = name.split(':')
                check(elements.size == 3) { "invalid library name: $name" }

                val (group, artifact, version) = elements

                val jarName = "$artifact-$version.jar"

                val baseUrl = json.url ?: DEFAULT_BASE_URL

                val path = "${group.replace('.', ':')}/$artifact/$version/$jarName"

                return persistentMapOf(
                    "" to Artifact(
                        path = path,
                        sha1 = null,
                        size = null,
                        url = "$baseUrl$path"
                    )
                )
            }
        }
    }
}
