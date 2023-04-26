package com.nononsenseapps.feeder.envoy

import android.util.Log
import com.nononsenseapps.jsonfeed.Feed
import com.nononsenseapps.jsonfeed.JsonFeedParser
import com.nononsenseapps.jsonfeed.feedAdapter
import com.nononsenseapps.jsonfeed.trustAllCerts
import com.squareup.moshi.JsonAdapter
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3.Cache
import okhttp3.OkHttpClient
import org.greatfire.envoy.CronetInterceptor
import org.greatfire.envoy.CronetNetworking

fun envoyHttpClient(
    cacheDirectory: File? = null,
    cacheSize: Long = 10L * 1024L * 1024L,
    trustAllCerts: Boolean = true,
    connectTimeoutSecs: Long = 30L,
    readTimeoutSecs: Long = 30L
): OkHttpClient {

    val LOG_TAG = "ENVOY_HTTP_CLIENT"

    val builder: OkHttpClient.Builder = OkHttpClient
        .Builder()
        .addInterceptor { chain ->
            // data sources are bound at startup with this instance of OkHttpClient so
            // we need an interceptor to block connections until CronetEngine is ready
            if (CronetNetworking.cronetEngine() == null) {
                Log.w(LOG_TAG, "CronetEngine has not been initialized, intercept and throw exception")
                throw IOException("CronetEngine has not been initialized")
            } else {
                Log.d(LOG_TAG, "CronetEngine has been initialized, intercept and proceed")
                chain.proceed(chain.request())
            }
        }
        // CronetInterceptor will be bypassed if CronetEngine has not been initialized
        .addInterceptor(CronetInterceptor())

    if (cacheDirectory != null) {
        builder.cache(Cache(cacheDirectory, cacheSize))
    }

    builder
        .connectTimeout(connectTimeoutSecs, TimeUnit.SECONDS)
        .readTimeout(readTimeoutSecs, TimeUnit.SECONDS)
        .followRedirects(true)

    if (trustAllCerts) {
        builder.trustAllCerts()
    }

    return builder.build()
}

class EnvoyFeedParser(
    httpClient: OkHttpClient,
    jsonFeedAdapter: JsonAdapter<Feed>
) : JsonFeedParser(httpClient, jsonFeedAdapter) {

    constructor(
        cacheDirectory: File? = null,
        cacheSize: Long = 10L * 1024L * 1024L,
        trustAllCerts: Boolean = true,
        connectTimeoutSecs: Long = 5L,
        readTimeoutSecs: Long = 5L
    ) : this(
        envoyHttpClient(
            cacheDirectory = cacheDirectory,
            cacheSize = cacheSize,
            trustAllCerts = trustAllCerts,
            connectTimeoutSecs = connectTimeoutSecs,
            readTimeoutSecs = readTimeoutSecs
        ),
        feedAdapter()
    )
}
