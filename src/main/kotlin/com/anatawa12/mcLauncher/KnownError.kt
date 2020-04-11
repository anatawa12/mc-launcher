package com.anatawa12.mcLauncher


sealed class KnownErrorException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable?) : super(message, cause)

    class InvalidVersionJson(version: String, message: String? = null, cause: Throwable? = null) :
        KnownErrorException("$version.json is invalid${message?.let { ": $it" } ?: ""}", cause) {
        constructor(version: String, cause: Throwable? = null) : this(version, null, cause = cause)
    }

    class InvalidVersionJsonData(version: String, message: String? = null, cause: Throwable? = null) :
        KnownErrorException("$version.json has invalid data${message?.let { ": $it" } ?: ""}", cause) {
        constructor(version: String, cause: Throwable? = null) : this(version, null, cause = cause)
    }

    class InvalidLibraries(id: String, message: String? = null, cause: Throwable? = null) :
        KnownErrorException("library data for '$id' is invalid${message?.let { ": $it" } ?: ""}", cause) {
        constructor(id: String, cause: Throwable? = null) : this(id, null, cause = cause)
    }

    class VersionJsonNotFile(version: String, message: String? = null, cause: Throwable) :
        KnownErrorException("$version.json is not a file.${message?.let { ": $it" } ?: ""}", cause) {
        constructor(version: String, cause: Throwable) : this(version, null, cause = cause)
    }

    class InvalidLibrary(libraryPath: String, message: String? = null, cause: Throwable? = null) :
        KnownErrorException("$libraryPath is not valid.${message?.let { ": $it" } ?: ""}", cause) {
        constructor(libraryPath: String, cause: Throwable? = null) : this(libraryPath, null, cause = cause)
    }
}
