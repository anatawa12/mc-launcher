package com.anatawa12.mcLauncher.launchInfo.json

data class LibraryDownloads(
    val artifact: Artifact? = null,
    val classifiers: Map<String, Artifact>? = null
)
