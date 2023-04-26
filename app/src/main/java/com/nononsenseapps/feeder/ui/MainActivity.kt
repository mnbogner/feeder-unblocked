package com.nononsenseapps.feeder.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.core.util.Consumer
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.nononsenseapps.feeder.base.DIAwareComponentActivity
import com.nononsenseapps.feeder.model.workmanager.requestFeedSync
import com.nononsenseapps.feeder.model.workmanager.scheduleGetUpdates
import com.nononsenseapps.feeder.notifications.NotificationsWorker
import com.nononsenseapps.feeder.ui.compose.navigation.AddFeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.ArticleDestination
import com.nononsenseapps.feeder.ui.compose.navigation.EditFeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.FeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.SearchFeedDestination
import com.nononsenseapps.feeder.ui.compose.navigation.SettingsDestination
import com.nononsenseapps.feeder.ui.compose.navigation.SyncScreenDestination
import com.nononsenseapps.feeder.ui.compose.utils.withAllProviders
import kotlinx.coroutines.launch
import org.greatfire.envoy.*
import org.kodein.di.instance

class MainActivity : DIAwareComponentActivity() {
    private val notificationsWorker: NotificationsWorker by instance()
    private val mainActivityViewModel: MainActivityViewModel by instance(arg = this)

    private val DIRECT_URL = arrayListOf<String>()

    private var waitingForEnvoy = false
    private var envoyUnused = false

    // this receiver should be triggered by a success or failure broadcast from the
    // NetworkIntentService (indicating whether submitted urls were valid or invalid)
    private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && context != null) {
                if (intent.action == ENVOY_BROADCAST_VALIDATION_SUCCEEDED) {
                    val validUrl = intent.getStringExtra(ENVOY_DATA_URL_SUCCEEDED)
                    if (validUrl.isNullOrEmpty()) {
                        Log.w(LOG_TAG, "received a valid url that was empty or null")
                    } else if (waitingForEnvoy) {
                        // select the first valid url that is received (assumed to have the lowest latency)
                        waitingForEnvoy = false
                        if (DIRECT_URL.contains(validUrl)) {
                            Log.d(LOG_TAG, "received a direct url, don't need to start engine")
                            // set flag so resuming activity doesn't trigger another envoy check
                            envoyUnused = true
                        } else {
                            Log.d(LOG_TAG, "received a valid url, start engine")
                            CronetNetworking.initializeCronetEngine(context, validUrl)
                        }
                        // after starting CronetEngine, sync feeds
                        maybeRequestSync()
                    } else {
                        Log.d(LOG_TAG, "already selected a valid url, ignore additional urls")
                    }
                } else if (intent.action == ENVOY_BROADCAST_VALIDATION_FAILED) {
                    val invalidUrl = intent.getStringExtra(ENVOY_DATA_URL_FAILED)
                    if (invalidUrl.isNullOrEmpty()) {
                        Log.w(LOG_TAG, "received an invalid url that was empty or null")
                    } else {
                        Log.w(LOG_TAG, "received an invalid url")
                    }
                } else if (intent.action == ENVOY_BROADCAST_VALIDATION_ENDED) {
                    val cause = intent.getStringExtra(ENVOY_DATA_VALIDATION_ENDED_CAUSE)
                    if (cause.isNullOrEmpty()) {
                        Log.e(LOG_TAG, "received an envoy validation ended broadcast with an invalid cause")
                    } else {
                        Log.e(LOG_TAG, "received an envoy validation ended broadcast with a cause: " + cause)
                    }
                    // set flag so resuming activity doesn't trigger another envoy check
                    waitingForEnvoy = false
                    // TODO - if CronetEngine can't be started, do we attempt to sync feeds anyway?
                } else {
                    Log.w(LOG_TAG, "received unexpected intent: " + intent.action)
                }
            } else {
                Log.w(LOG_TAG, "receiver triggered but context or intent was null")
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // moved to start/stop to avoid an issue with registering multiple instances of the receiver when app is swiped away
        Log.d(LOG_TAG, "start/register broadcast receiver")
        // register to receive test results
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, IntentFilter().apply {
            addAction(ENVOY_BROADCAST_VALIDATION_SUCCEEDED)
            addAction(ENVOY_BROADCAST_VALIDATION_FAILED)
            addAction(ENVOY_BROADCAST_VALIDATION_ENDED)
        })

        notificationsWorker.runForever()
    }

    override fun onStop() {
        notificationsWorker.stopForever()

        // moved to start/stop to avoid an issue with registering multiple instances of the receiver when app is swiped away
        Log.d(LOG_TAG, "stop/unregister broadcast receiver")
        // unregister receiver for test results
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver)

        super.onStop()
    }

    override fun onResume() {
        super.onResume()

        // start envoy here to prevent exception from starting a service when out of focus
        if (envoyUnused) {
            Log.w(LOG_TAG, "direct connection previously worked, don't try to start envoy")
        } else if (CronetNetworking.cronetEngine() != null) {
            Log.w(LOG_TAG, "cronet already running, don't try to start envoy again")
        } else if (waitingForEnvoy) {
            Log.w(LOG_TAG, "already processing urls, don't try to start envoy again")
        } else {
            // run envoy setup (fetches and validate urls)
            Log.d(LOG_TAG, "start envoy to process urls")
            waitingForEnvoy = true
            envoyInit()
        }

        mainActivityViewModel.setResumeTime()
        scheduleGetUpdates(di)
    }

    fun envoyInit() {

        val listOfUrls = mutableListOf<String>()
        listOfUrls.add("url_1")
        listOfUrls.add("url_2")
        listOfUrls.add("url_3")

        val urlSources = mutableListOf<String>()

        Log.d(LOG_TAG, "submit urls")

        NetworkIntentService.submit(
            this@MainActivity,
            listOfUrls,
            DIRECT_URL,
            "",
            urlSources,
            1,
            1,
            1
        )
    }

    private fun maybeRequestSync() = lifecycleScope.launch {
        if (mainActivityViewModel.shouldSyncOnResume) {
            if (mainActivityViewModel.isOkToSyncAutomatically()) {
                Log.d(LOG_TAG, "ok to sync feeds")
                requestFeedSync(
                    di = di,
                    forceNetwork = false,
                )
            } else {
                Log.d(LOG_TAG, "not ok to sync feeds")
            }
        } else {
            Log.d(LOG_TAG, "don't sync feeds")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // reset in onCreate, check in onResume
        envoyUnused = false

        mainActivityViewModel.ensurePeriodicSyncConfigured()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            withAllProviders {
                AppContent()
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    fun AppContent() {
        val navController = rememberAnimatedNavController()

        AnimatedNavHost(navController, startDestination = FeedDestination.route) {
            FeedDestination.register(this, navController)
            ArticleDestination.register(this, navController)
            // Feed editing
            EditFeedDestination.register(this, navController)
            SearchFeedDestination.register(this, navController)
            AddFeedDestination.register(this, navController)
            // Settings
            SettingsDestination.register(this, navController)
            // Sync settings
            SyncScreenDestination.register(this, navController)
        }

        DisposableEffect(navController) {
            val listener = Consumer<Intent> { intent ->
                if (!navController.handleDeepLink(intent)) {
                    Log.e(LOG_TAG, "NavController rejected intent: $intent")
                }
            }
            addOnNewIntentListener(listener)
            onDispose { removeOnNewIntentListener(listener) }
        }
    }

    companion object {
        private const val LOG_TAG = "FEEDER_MAIN"
    }
}
