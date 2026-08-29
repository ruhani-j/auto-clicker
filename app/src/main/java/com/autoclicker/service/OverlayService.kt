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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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
import com.autoclicker.ui.overlay.OverlayPanel
import kotlinx.coroutines.*

class OverlayService : Service() {

    companion object {
        const val ACTION_START = "com.autoclicker.START_OVERLAY"
        const val ACTION_STOP = "com.autoclicker.STOP_OVERLAY"
        private const val CHANNEL_ID = "autoclicker_channel"
        private const val NOTIF_ID = 1

        val isRunning = mutableStateOf(false)
        val activeProfiles = mutableStateListOf<ClickerProfile>()
    }

    private lateinit var windowManager: WindowManager
    private var composeView: ComposeView? = null
    private val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = 16; y = 200
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val clickJobs = mutableMapOf<Long, Job>()
    private lateinit var repository: ClickerRepository

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        repository = ClickerRepository(ClickerDatabase.getInstance(this).clickerDao())
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground(NOTIF_ID, buildNotification())
                isRunning.value = true
                showOverlay()
                activeProfiles.filter { it.isEnabled }.forEach { launchClickerJob(it) }
            }
            ACTION_STOP -> {
                stopEverything()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun launchClickerJob(profile: ClickerProfile) {
        clickJobs[profile.id]?.cancel()
        clickJobs[profile.id] = serviceScope.launch {
            delay(profile.startDelayMs)
            var done = 0
            while (isActive && (profile.isInfinite || done < profile.clickCount)) {
                AutoClickerAccessibilityService.instance?.performClick(
                    profile.positionX, profile.positionY,
                    profile.clickType, profile.holdDurationMs, profile.jitterPositionPx
                )
                done++
                val jitter = if (profile.jitterIntervalMs > 0)
                    (-profile.jitterIntervalMs..profile.jitterIntervalMs).random() else 0L
                delay((profile.intervalMs + jitter).coerceAtLeast(50L))
            }
        }
    }

    private fun stopClickerJob(profileId: Long) {
        clickJobs[profileId]?.cancel()
        clickJobs.remove(profileId)
    }

    private fun stopEverything() {
        clickJobs.values.forEach { it.cancel() }
        clickJobs.clear()
        isRunning.value = false
        removeOverlay()
    }

    private fun showOverlay() {
        if (composeView != null) return
        val lifecycleOwner = ServiceLifecycleOwner()

        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeViewModelStoreOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setContent {
                OverlayPanel(
                    profiles = activeProfiles,
                    onToggleProfile = { profile, enabled ->
                        val updated = profile.copy(isEnabled = enabled)
                        val idx = activeProfiles.indexOfFirst { it.id == profile.id }
                        if (idx >= 0) activeProfiles[idx] = updated
                        if (enabled) launchClickerJob(updated) else stopClickerJob(profile.id)
                    },
                    onStopAll = {
                        startService(Intent(this@OverlayService, OverlayService::class.java)
                            .apply { action = ACTION_STOP })
                    },
                    onDrag = { dx, dy ->
                        params.x += dx.toInt()
                        params.y += dy.toInt()
                        composeView?.let { windowManager.updateViewLayout(it, params) }
                    }
                )
            }
        }
        composeView = view
        windowManager.addView(view, params)
    }

    private fun removeOverlay() {
        composeView?.let { windowManager.removeView(it) }
        composeView = null
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
