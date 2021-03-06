package com.anatawa12.mcLauncher

data class Profile(
    val version: String,
    val gameDirPath: String,
    val jvmArguments: List<String> = listOf(),
    val platform: Platform = Platform.current,
    val appDataDirPath: String = platform.os.getAppDataDirPath()
)
