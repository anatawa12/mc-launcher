package com.anatawa12.mcLauncher.json

import java.util.*

data class VersionJson(
    val assetIndex: AssetIndex? = null,
    val assets: String? = null,
    val downloads: Map<String, DownloadFile>? = null,
    val id: String,
    val libraries: List<Library> = listOf(),
    val logging: Map<String, Logging>? = null,
    val mainClass: String,
    val minecraftArguments: String,
    val minimumLauncherVersion: Int? = null,
    val releaseTime: Date,
    val time: Date,
    val type: String,
    val inheritsFrom: String? = null,
    val jar: String? = null
)
