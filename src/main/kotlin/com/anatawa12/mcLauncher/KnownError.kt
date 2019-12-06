package com.anatawa12.mcLauncher


class KnownErrorException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}

@Suppress("NOTHING_TO_INLINE")
inline fun knownError(message: Any): Nothing = throw KnownErrorException(message.toString())

@Suppress("NOTHING_TO_INLINE")
inline fun knownError(message: Any, cause: Throwable): Nothing = throw KnownErrorException(message.toString(), cause)
