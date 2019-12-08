package com.anatawa12.mcLauncher.launchInfo.json

data class Library(
    val downloads: LibraryDownloads? = null,
    val extract: Extract? = null,
    val name: String,
    val natives: Natives? = null,
    val rules: List<Rule>? = null,
    val url: String? = null,
    val serverreq: Boolean? = null,
    val clientreq: Boolean? = null,
    val checksums: List<String>? = null
)
