package com.anatawa12.mcLauncher

data class Platform(val os: OperatingSystem, val arch: Architecture) {
    enum class OperatingSystem {
        Linux {
            override fun getAppDataDirPath(): String {
                return "${System.getProperty("user.home")}/.minecraft"
            }
        },
        MacOS {
            override fun getAppDataDirPath(): String {
                return "${System.getProperty("user.home")}/Library/Application Support/minecraft"
            }
        },
        Windows {
            override fun getAppDataDirPath(): String {
                return "${System.getenv("APPDATA")}\\.minecraft"
            }
        },
        ;

        abstract fun getAppDataDirPath(): String

        companion object {
            val current: OperatingSystem

            init {
                val osName = System.getProperty("os.name")
                current = when {
                    osName.startsWith("Windows") -> {
                        Windows
                    }
                    osName.startsWith("Linux")
                            || osName.startsWith("FreeBSD")
                            || osName.startsWith("SunOS")
                            || osName.startsWith("Unix") -> {
                        Linux
                    }
                    osName.startsWith("Mac OS X")
                            || osName.startsWith("Darwin") -> {
                        MacOS
                    }
                    else -> {
                        throw LinkageError("Unknown platform: $osName")
                    }
                }
            }
        }
    }

    enum class Architecture(val is64Bit: Boolean) {
        X86(false),
        ARM64(true),

        ;

        companion object {
            val current: Architecture

            init {
                val osArch = System.getProperty("os.arch")

                val is64Bit = osArch.contains("64") || osArch.startsWith("armv8")

                current = if (is64Bit) ARM64
                else X86
            }
        }
    }

    companion object {
        val current = Platform(OperatingSystem.current, Architecture.current)
    }
}

