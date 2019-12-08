package com.anatawa12.mcLauncher

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        Launcher(Profile(args[0], args.copyOfRange(1, args.size).toList())).launch()
    }
}
