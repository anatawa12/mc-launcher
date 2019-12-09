package com.anatawa12.mcLauncher

import java.io.File

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        Launcher(Profile(args[0], File(".").absolutePath, args.copyOfRange(3, args.size).toList())).apply {
            loginer = Loginer(args[1], args[2])
        }.launch()
    }
}
