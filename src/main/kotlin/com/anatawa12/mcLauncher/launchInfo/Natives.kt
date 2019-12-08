package com.anatawa12.mcLauncher.launchInfo

import com.anatawa12.mcLauncher.launchInfo.json.Natives as JsonNatives

data class Natives(
    val linux: String,
    val osx: String,
    val windows: String
) {
    constructor(json: JsonNatives) : this(
        json.linux ?: "",
        json.osx ?: "",
        json.windows ?: ""
    )

    companion object {
        val DEFAULT_NATIVES = Natives("", "", "")
    }
}
