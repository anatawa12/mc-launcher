package com.anatawa12.mcLauncher


sealed class KnownErrorException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable?) : super(message, cause)

    class InvalidVersionJson(version: String, cause: Throwable? = null) :
        KnownErrorException("$version.json is invalid", cause)

    class InvalidVersionJsonData(version: String, cause: Throwable? = null) :
        KnownErrorException("$version.json has invalid data", cause)

    class InvalidLibraries(id: String, cause: Throwable? = null) :
        KnownErrorException("library data for '$id' is invalid", cause)

    class VersionJsonNotFile(version: String, cause: Throwable) :
        KnownErrorException("$version.json is not a file.", cause)

    class InvalidLibrary(libraryPath: String, cause: Throwable? = null) :
        KnownErrorException("$libraryPath is not valid.", cause)
}
