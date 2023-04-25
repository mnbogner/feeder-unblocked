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

    System.out.println("FOO - envoyHttpClient -> build okhttp client with cronet interceptor")

    val builder: OkHttpClient.Builder = OkHttpClient
        .Builder()
        .addInterceptor { chain ->
            // data sources are bound at startup with this instance of OkHttpClient so
            // we need an interceptor to block connections until CronetEngine is ready
            if (CronetNetworking.cronetEngine() == null) {
                System.out.println("FOO - no envoy so intercept and return error")
                Log.d("FOO2", "no envoy so intercept and return error")
                throw IOException("oops")
            } else {
                System.out.println("FOO - envoy running so pass through")
                Log.d("FOO2", "envoy running so pass through")
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
