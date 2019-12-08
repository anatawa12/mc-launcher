package com.anatawa12.mcLauncher.launchInfo

import com.anatawa12.mcLauncher.launchInfo.json.Artifact as JsonArtifact

data class Artifact(
    val path: String,
    val url: String,
    val sha1: String? = null,
    val size: Int? = null
) {
    constructor(json: JsonArtifact) : this(json.path, json.url, json.sha1, json.size)
}
