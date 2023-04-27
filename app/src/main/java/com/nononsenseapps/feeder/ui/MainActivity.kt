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
                        System.out.println("FOO - broadcastReceiver -> received a valid url that was empty or null")
                    } else if (waitingForEnvoy) {
                        // select the first valid url that is received (assumed to have the lowest latency)
                        waitingForEnvoy = false
                        if (DIRECT_URL.contains(validUrl)) {
                            System.out.println("FOO - broadcastReceiver -> got direct url: " + validUrl + ", don't need to start engine")
                            // set flag so resuming activity doesn't trigger another envoy check
                            envoyUnused = true
                        } else {
                            System.out.println("FOO - broadcastReceiver -> found a valid url: " + validUrl + ", start engine")
                            CronetNetworking.initializeCronetEngine(context, validUrl)
                        }
                        // now that envoy has started (or not) sync (maybe)
                        maybeRequestSync()
                    } else {
                        System.out.println("FOO - broadcastReceiver -> already selected a valid url, ignore valid url: " + validUrl)
                    }
                } else if (intent.action == ENVOY_BROADCAST_VALIDATION_FAILED) {
                    val invalidUrl = intent.getStringExtra(ENVOY_DATA_URL_FAILED)
                    if (invalidUrl.isNullOrEmpty()) {
                        System.out.println("FOO - broadcastReceiver -> received an invalid url that was empty or null")
                    } else {
                        System.out.println("FOO - broadcastReceiver -> got invalid url: " + invalidUrl)
                    }
                } else if (intent.action == ENVOY_BROADCAST_VALIDATION_ENDED) {
                    val cause = intent.getStringExtra(ENVOY_DATA_VALIDATION_ENDED_CAUSE)
                    if (cause.isNullOrEmpty()) {
                        System.out.println("FOO - broadcastReceiver -> received an envoy validation ended broadcast with an invalid cause")
                    } else {
                        System.out.println("FOO - broadcastReceiver -> received an envoy validation ended broadcast with a cause: " + cause)
                    }
                    // set flag so resuming activity doesn't trigger another envoy check
                    waitingForEnvoy = false
                    // do we try to sync if envoy has failed?
                } else {
                    System.out.println("FOO - broadcastReceiver -> received unexpected intent: " + intent.action)
                }
            } else {
                System.out.println("FOO - broadcastReceiver -> receiver triggered but context or intent was null")
            }

        }
    }

    override fun onStart() {
        super.onStart()

        // moved to start/stop to avoid an issue with registering multiple instances of the receiver when app is swiped away
        System.out.println("FOO - onStart -> start/register broadcast receiver")
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
        System.out.println("FOO - onStart -> stop/unregister broadcast receiver")
        // unregister receiver for test results
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver)

        super.onStop()
    }

    override fun onResume() {
        super.onResume()

        System.out.println("FOO - onResume -> start envoy")

        // start cronet here to prevent exception from starting a service when out of focus
        if (envoyUnused) {
            System.out.println("FOO - onResume -> direct connection previously worked, don't try to start envoy")
        } else if (CronetNetworking.cronetEngine() != null) {
            System.out.println("FOO - onResume -> cronet already running, don't try to start envoy again")
        } else if (waitingForEnvoy) {
            System.out.println("FOO - onResume -> already processing urls, don't try to start envoy again")
        } else {
            // run envoy setup (fetches and validate urls)
            System.out.println("FOO - onResume -> start envoy to process urls")
            waitingForEnvoy = true
            envoyInit()
        }

        mainActivityViewModel.setResumeTime()
        scheduleGetUpdates(di)
    }

    fun envoyInit() {

        val listOfUrls = mutableListOf<String>()
        listOfUrls.add("https://abc.zhaolimin.com/gfd/")
        listOfUrls.add("https://abc.hxun.org/gfd/")
        listOfUrls.add("https://abc.afrt.org/gfd/")

        val urlSources = mutableListOf<String>()

        System.out.println("FOO - envoyInit -> submit urls")

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
                System.out.println("FOO - sync feed")
                requestFeedSync(
                    di = di,
                    forceNetwork = false,
                )
            } else {
                System.out.println("FOO - not ok to sync")
            }
        } else {
            System.out.println("FOO - don't sync on resume")
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
