package com.anatawa12.mcLauncher.launchInfo

import com.anatawa12.mcLauncher.json.DownloadFile as JsonDownloadFile

data class DownloadFile(
    val sha1: String,
    val size: Int,
    val url: String
) {
    constructor(json: JsonDownloadFile) : this(
        json.sha1,
        json.size,
        json.url
    )
}
