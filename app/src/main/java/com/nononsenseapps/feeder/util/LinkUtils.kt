package com.nononsenseapps.feeder.util

import java.net.MalformedURLException
import java.net.URI
import java.net.URL

/**
 * Ensures a url is valid, having a scheme and everything. It turns 'google.com' into 'http://google.com' for example.
 */
fun sloppyLinkToStrictURL(url: String): URL = try {
    // If no exception, it's valid
    URL(url)
} catch (_: MalformedURLException) {
    // Might be an unknown protocol in which case a URL is not valid to be created
    val uri = URI(url)

    if (uri.isAbsolute) {
        uri.toURL()
    } else {
        // No scheme, assume http
        URL("http://$url")
    }
}

fun sloppyLinkToStrictURLOrNull(url: String): URL? = try {
    sloppyLinkToStrictURL(url)
} catch (_: MalformedURLException) {
    null
}

/**
 * Returns a URL but does not guarantee that it accurately represents the input string if the input string is an invalid URL.
 * This is used to ensure that migrations to versions where Feeds have URL and not strings don't crash.
 */
fun sloppyLinkToStrictURLNoThrows(url: String): URL = try {
    sloppyLinkToStrictURL(url)
} catch (_: MalformedURLException) {
    sloppyLinkToStrictURL("")
}

/**
 * On error, this method simply returns the original link. It does *not* throw exceptions.
 */
fun relativeLinkIntoAbsolute(base: URL, link: String): String = try {
    // If no exception, it's valid
    relativeLinkIntoAbsoluteOrThrow(base, link).toString()
} catch (_: MalformedURLException) {
    link
}

/**
 * On error, this method simply returns the original link. It does *not* throw exceptions.
 */
fun relativeLinkIntoAbsoluteOrNull(base: URL, link: String?): String? = try {
    // If no exception, it's valid
    if (link != null) {
        relativeLinkIntoAbsoluteOrThrow(base, link).toString()
    } else {
        null
    }
} catch (_: MalformedURLException) {
    link
}

/**
 * On error, throws MalformedURLException.
 */
@Throws(MalformedURLException::class)
fun relativeLinkIntoAbsoluteOrThrow(base: URL, link: String): URL = try {
    // If no exception, it's valid
    URL(link)
} catch (_: MalformedURLException) {
    URL(base, link)
}
