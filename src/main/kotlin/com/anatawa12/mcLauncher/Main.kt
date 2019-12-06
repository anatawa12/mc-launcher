package com.anatawa12.mcLauncher

import java.io.File

object Main {
    lateinit var homeDir: File
        private set
    lateinit var appDataDir: File
        private set

    @JvmStatic
    fun main(args: Array<String>) {
        init()
    }

    private fun init() {
        homeDir = File(System.getProperty("user.home"))
        val appDataDirPath: String
        when (Platform.current) {
            Platform.Linux -> {
                appDataDirPath = "$homeDir/.minecraft"
            }
            Platform.MacOS -> {
                appDataDirPath = "$homeDir/Library/Application Support/minecraft"
            }
            Platform.Windows -> {
                appDataDirPath = "${System.getenv("APPDATA")}\\.minecraft"
            }
        }
        appDataDir = File(appDataDirPath)
    }
}
