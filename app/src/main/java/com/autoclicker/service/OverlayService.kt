package com.autoclicker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.autoclicker.MainActivity
import com.autoclicker.data.ClickerDatabase
import com.autoclicker.data.ClickerProfile
import com.autoclicker.data.ClickerRepository
import com.autoclicker.ui.overlay.ClickerDot
import com.autoclicker.ui.overlay.OverlayControls
import kotlinx.coroutines.*

class OverlayService : Service() {

    companion object {
        const val ACTION_START = "com.autoclicker.START_OVERLAY"
        const val ACTION_STOP = "com.autoclicker.STOP_OVERLAY"
        private const val CHANNEL_ID = "autoclicker_channel"
        private const val NOTIF_ID = 1

        val isRunning = mutableStateOf(false)
        val activeProfiles = mutableStateListOf<ClickerProfile>()
        val isPaused = mutableStateOf(false)
        val isHidden = mutableStateOf(false)
        val clickTriggers = mutableStateMapOf<Long, Int>()
        val flashEnabled = mutableStateOf(true)
    }

    private lateinit var windowManager: WindowManager
    private lateinit var repository: ClickerRepository
    private lateinit var overlayLifecycleOwner: ServiceLifecycleOwner

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val clickJobs = mutableMapOf<Long, Job>()
    private val dotViews = mutableMapOf<Long, Pair<ComposeView, WindowManager.LayoutParams>>()
    private var controlView: ComposeView? = null
    private var profileObserverJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        repository = ClickerRepository(ClickerDatabase.getInstance(this).clickerDao())
        overlayLifecycleOwner = ServiceLifecycleOwner()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground(NOTIF_ID, buildNotification())
                isRunning.value = true
                isPaused.value = false
                isHidden.value = false
                showControlPanel()
                startProfileObserver()
            }
            ACTION_STOP -> {
                stopEverything()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startProfileObserver() {
        profileObserverJob?.cancel()
        profileObserverJob = serviceScope.launch {
            snapshotFlow { activeProfiles.toList() }.collect { profiles ->
                val profileIds = profiles.map { it.id }.toSet()

                // Remove dots and jobs for deleted profiles
                dotViews.keys.filter { it !in profileIds }.toList().forEach { id ->
                    dotViews[id]?.first?.let { v -> safeRemoveView(v) }
                    dotViews.remove(id)
                    clickJobs[id]?.cancel()
                    clickJobs.remove(id)
                }

                // Add dots and jobs for new profiles; sync position for existing ones
                profiles.forEach { profile ->
                    if (!dotViews.containsKey(profile.id)) {
                        if (!isHidden.value) showDot(profile)
                        launchClickerJob(profile.id)
                    } else {
                        val (view, params) = dotViews[profile.id] ?: return@forEach
                        if (params.x != profile.positionX || params.y != profile.positionY) {
                            params.x = profile.positionX
                            params.y = profile.positionY
                            windowManager.updateViewLayout(view, params)
                        }
                    }
                }
            }
        }
    }

    private fun launchClickerJob(profileId: Long) {
        clickJobs[profileId]?.cancel()
        // Offset by the dot's radius (15dp) so clicks land at the dot's visual center, not its top-left corner
        val dotRadiusPx = (15f * resources.displayMetrics.density).toInt()
        clickJobs[profileId] = serviceScope.launch {
            val startDelay = activeProfiles.find { it.id == profileId }?.startDelayMs ?: 0L
            delay(startDelay)
            var done = 0
            while (isActive) {
                val current = activeProfiles.find { it.id == profileId } ?: break
                if (!current.isInfinite && done >= current.clickCount) break
                if (!isPaused.value) {
                    AutoClickerAccessibilityService.instance?.performClick(
                        current.positionX + dotRadiusPx, current.positionY + dotRadiusPx,
                        current.clickType, current.holdDurationMs, current.jitterPositionPx
                    )
                    clickTriggers[profileId] = (clickTriggers[profileId] ?: 0) + 1
                    done++
                    val jitter = if (current.jitterIntervalMs > 0)
                        (-current.jitterIntervalMs..current.jitterIntervalMs).random() else 0L
                    delay((current.intervalMs + jitter).coerceAtLeast(50L))
                } else {
                    delay(100L)
                }
            }
        }
    }

    private fun showDot(profile: ClickerProfile) {
        if (dotViews.containsKey(profile.id)) return
        val dotParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = profile.positionX
            y = profile.positionY
        }
        val view = ComposeView(this).also { cv ->
            cv.setViewTreeLifecycleOwner(overlayLifecycleOwner)
            cv.setViewTreeViewModelStoreOwner(overlayLifecycleOwner)
            cv.setViewTreeSavedStateRegistryOwner(overlayLifecycleOwner)
            cv.setContent {
                val currentProfile by remember(profile.id) {
                    derivedStateOf { activeProfiles.find { it.id == profile.id } ?: profile }
                }
                val trigger = clickTriggers[profile.id] ?: 0
                val flash by flashEnabled
                ClickerDot(
                    profile = currentProfile,
                    clickTrigger = trigger,
                    flashEnabled = flash,
                    onDrag = { dx, dy ->
                        dotParams.x += dx.toInt()
                        dotParams.y += dy.toInt()
                        windowManager.updateViewLayout(cv, dotParams)
                    },
                    onDragEnd = {
                        val idx = activeProfiles.indexOfFirst { it.id == profile.id }
                        if (idx >= 0) {
                            val updated = activeProfiles[idx].copy(
                                positionX = dotParams.x,
                                positionY = dotParams.y
                            )
                            activeProfiles[idx] = updated
                            serviceScope.launch { repository.update(updated) }
                        }
                    },
                    onProfileUpdate = { updated ->
                        val idx = activeProfiles.indexOfFirst { it.id == updated.id }
                        if (idx >= 0) {
                            activeProfiles[idx] = updated
                            serviceScope.launch { repository.update(updated) }
                        }
                    }
                )
            }
        }
        dotViews[profile.id] = Pair(view, dotParams)
        windowManager.addView(view, dotParams)
    }

    private fun showControlPanel() {
        if (controlView != null) return
        val cp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 16
            y = 200
        }
        val view = ComposeView(this).also { cv ->
            cv.setViewTreeLifecycleOwner(overlayLifecycleOwner)
            cv.setViewTreeViewModelStoreOwner(overlayLifecycleOwner)
            cv.setViewTreeSavedStateRegistryOwner(overlayLifecycleOwner)
            cv.setContent {
                val paused by isPaused
                val hidden by isHidden
                OverlayControls(
                    isPaused = paused,
                    isHidden = hidden,
                    onTogglePause = { isPaused.value = !isPaused.value },
                    onToggleHide = {
                        if (isHidden.value) {
                            isHidden.value = false
                            activeProfiles.forEach { showDot(it) }
                        } else {
                            isHidden.value = true
                            hideDots()
                        }
                    },
                    onDrag = { dx, dy ->
                        cp.x += dx.toInt()
                        cp.y += dy.toInt()
                        windowManager.updateViewLayout(cv, cp)
                    }
                )
            }
        }
        controlView = view
        windowManager.addView(view, cp)
    }

    private fun hideDots() {
        dotViews.values.forEach { (view, _) -> safeRemoveView(view) }
        dotViews.clear()
    }

    private fun stopEverything() {
        profileObserverJob?.cancel()
        profileObserverJob = null
        clickJobs.values.forEach { it.cancel() }
        clickJobs.clear()
        clickTriggers.clear()
        isRunning.value = false
        isPaused.value = false
        isHidden.value = false
        removeOverlay()
    }

    private fun removeOverlay() {
        hideDots()
        controlView?.let { safeRemoveView(it) }
        controlView = null
    }

    private fun safeRemoveView(view: ComposeView) {
        try { windowManager.removeView(view) } catch (ignored: Exception) {}
    }

    private fun buildNotification(): Notification {
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AutoClicker running")
            .setContentText("Tap to open")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pi)
            .build()
    }

    private fun createNotificationChannel() {
        val ch = NotificationChannel(CHANNEL_ID, "AutoClicker", NotificationManager.IMPORTANCE_LOW)
        getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopEverything()
        serviceScope.cancel()
        super.onDestroy()
    }
}

private class ServiceLifecycleOwner :
    LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val registry = LifecycleRegistry(this)
    private val vmStore = ViewModelStore()
    private val ssrc = SavedStateRegistryController.create(this)

    init {
        ssrc.performRestore(null)
        registry.currentState = Lifecycle.State.RESUMED
    }

    override val lifecycle: Lifecycle get() = registry
    override val viewModelStore: ViewModelStore get() = vmStore
    override val savedStateRegistry get() = ssrc.savedStateRegistry
}
