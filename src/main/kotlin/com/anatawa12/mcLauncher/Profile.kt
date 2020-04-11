package com.anatawa12.mcLauncher

data class Profile(
    val version: String,
    val gameDirPath: String,
    val jvmArguments: List<String> = listOf(),
    val operatingSystem: OperatingSystem = OperatingSystem.current,
    val appDataDirPath: String = operatingSystem.getAppDataDirPath()
)
