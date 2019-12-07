package com.anatawa12.mcLauncher


sealed class KnownErrorException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable?) : super(message, cause)

    class InheritsFromIsLooping(entry: String) :
        KnownErrorException("'inheritsFrom' is looping! entry version is '$entry'")

    class InvalidVersionJson(version: String, cause: Throwable? = null) :
        KnownErrorException("$version.json is invalid", cause)

    class InvalidVersionJsonData(version: String, cause: Throwable? = null) :
        KnownErrorException("$version.json has invalid data", cause)

    class VersionJsonNotFile(version: String, cause: Throwable) :
        KnownErrorException("$version.json is not a file.", cause)
}
