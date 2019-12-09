package com.anatawa12.mcLauncher

import java.io.File

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        Launcher(Profile(args[0], File(".").absolutePath, args.copyOfRange(1, args.size).toList())).apply {
            loginer = Loginer("", "")
        }.launch()
    }
}
