package com.example.fc_006

import android.Manifest
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fc_006.model.Asteroid
import com.example.fc_006.notifications.MissionNotificationHelper
import com.example.fc_006.viewmodel.AsteroidUiState
import com.example.fc_006.viewmodel.AsteroidViewModel
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat

class MainActivity : AppCompatActivity() {

    private val viewModel: AsteroidViewModel by viewModels()
    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var notificationHelper: MissionNotificationHelper
    private lateinit var statusText: TextView
    private lateinit var notificationPermissionChip: TextView
    private lateinit var signalStatusChip: TextView
    private lateinit var scanProgress: ProgressBar
    private lateinit var scanButton: MaterialButton
    private lateinit var signalButton: MaterialButton
    private lateinit var nameText: TextView
    private lateinit var hazardText: TextView
    private lateinit var distanceText: TextView
    private lateinit var dateText: TextView
    private lateinit var diameterText: TextView

    private var delayedSignalRunnable: Runnable? = null
    private var delayedScanAlertRunnable: Runnable? = null
    private var pendingNotificationAction: (() -> Unit)? = null

    private val wholeNumberFormat = NumberFormat.getNumberInstance().apply {
        maximumFractionDigits = 0
    }

    private val decimalNumberFormat = NumberFormat.getNumberInstance().apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            updateNotificationPermissionChip(notificationHelper.canSendNotifications())
            if (granted) {
                setMissionStatus(getString(R.string.status_notification_granted))
                pendingNotificationAction?.invoke()
            } else {
                setMissionStatus(getString(R.string.status_notification_denied))
            }
            pendingNotificationAction = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        notificationHelper = MissionNotificationHelper(this)

        bindViews()
        configureWindowInsets()
        notificationHelper.createChannels()
        initializeConsole()
        setupListeners()
        observeViewModel()
        requestNotificationPermissionIfNeeded()
        handleNotificationIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    override fun onDestroy() {
        delayedSignalRunnable?.let(mainHandler::removeCallbacks)
        delayedScanAlertRunnable?.let(mainHandler::removeCallbacks)
        super.onDestroy()
    }

    private fun bindViews() {
        statusText = findViewById(R.id.statusText)
        notificationPermissionChip = findViewById(R.id.notificationPermissionChip)
        signalStatusChip = findViewById(R.id.signalStatusChip)
        scanProgress = findViewById(R.id.scanProgress)
        scanButton = findViewById(R.id.scanButton)
        signalButton = findViewById(R.id.signalButton)
        nameText = findViewById(R.id.asteroidNameValue)
        hazardText = findViewById(R.id.asteroidHazardValue)
        distanceText = findViewById(R.id.asteroidDistanceValue)
        dateText = findViewById(R.id.asteroidDateValue)
        diameterText = findViewById(R.id.asteroidDiameterValue)
    }

    private fun configureWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeConsole() {
        resetAsteroidPanel()
        updateSignalChip(R.string.chip_signal_idle, R.color.console_green_dim)
        updateNotificationPermissionChip(notificationHelper.canSendNotifications())
        scanProgress.visibility = View.GONE
    }

    private fun setupListeners() {
        scanButton.setOnClickListener {
            viewModel.scanForAsteroids(BuildConfig.NASA_API_KEY)
        }

        signalButton.setOnClickListener {
            withNotificationPermission {
                armSignalTimer()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is AsteroidUiState.Idle -> {
                    scanButton.isEnabled = true
                    scanProgress.visibility = View.GONE
                    setMissionStatus(getString(R.string.status_idle))
                }

                is AsteroidUiState.Loading -> {
                    scanButton.isEnabled = false
                    scanProgress.visibility = View.VISIBLE
                    resetAsteroidPanel()
                    setMissionStatus(getString(R.string.status_scanning))
                }

                is AsteroidUiState.Success -> {
                    scanButton.isEnabled = true
                    scanProgress.visibility = View.GONE
                    bindAsteroid(state.asteroid)
                    val statusMessage = if (state.asteroid.isPotentiallyHazardous) {
                        R.string.status_warning_queued
                    } else {
                        R.string.status_safe_queued
                    }
                    setMissionStatus(getString(statusMessage))
                    queueScanAlert(state.asteroid)
                }

                is AsteroidUiState.Error -> {
                    scanButton.isEnabled = true
                    scanProgress.visibility = View.GONE
                    setMissionStatus(state.message)
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !notificationHelper.hasRuntimePermission()
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            updateNotificationPermissionChip(notificationHelper.canSendNotifications())
        }
    }

    private fun withNotificationPermission(action: () -> Unit) {
        if (notificationHelper.canSendNotifications()) {
            updateNotificationPermissionChip(true)
            action()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !notificationHelper.hasRuntimePermission()
        ) {
            pendingNotificationAction = action
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }

        updateNotificationPermissionChip(false)
        setMissionStatus(getString(R.string.status_notification_denied))
    }

    private fun armSignalTimer() {
        delayedSignalRunnable?.let(mainHandler::removeCallbacks)

        signalButton.isEnabled = false
        signalButton.text = getString(R.string.signal_timer_armed_button)
        updateSignalChip(R.string.chip_signal_armed, R.color.console_gold_dim)
        setMissionStatus(getString(R.string.status_signal_timer_armed))

        val signalRunnable = Runnable {
            delayedSignalRunnable = null
            signalButton.isEnabled = true
            signalButton.text = getString(R.string.arm_signal_timer)
            updateSignalChip(R.string.chip_signal_idle, R.color.console_green_dim)
            notificationHelper.showIncomingSignal()
            setMissionStatus(getString(R.string.status_signal_sent))
        }

        delayedSignalRunnable = signalRunnable
        mainHandler.postDelayed(signalRunnable, SIGNAL_DELAY_MS)
    }

    private fun queueScanAlert(asteroid: Asteroid) {
        withNotificationPermission {
            delayedScanAlertRunnable?.let(mainHandler::removeCallbacks)

            val alertRunnable = Runnable {
                delayedScanAlertRunnable = null
                if (asteroid.isPotentiallyHazardous) {
                    notificationHelper.showHazardDetected(
                        asteroid.name,
                        formatDistanceValue(
                            asteroid.closeApproachData.firstOrNull()?.missDistance?.kilometers
                        )
                    )
                } else {
                    notificationHelper.showThreatEvaded(asteroid.name)
                }
                setMissionStatus(getString(R.string.status_alert_dispatched))
            }

            delayedScanAlertRunnable = alertRunnable
            mainHandler.postDelayed(alertRunnable, SCAN_ALERT_DELAY_MS)
        }
    }

    private fun bindAsteroid(asteroid: Asteroid) {
        nameText.text = asteroid.name
        hazardText.text = if (asteroid.isPotentiallyHazardous) {
            getString(R.string.hazard_detected_value)
        } else {
            getString(R.string.hazard_cleared_value)
        }
        distanceText.text =
            formatDistanceValue(asteroid.closeApproachData.firstOrNull()?.missDistance?.kilometers)
        dateText.text =
            asteroid.closeApproachData.firstOrNull()?.closeApproachDate ?: getString(R.string.value_unknown)
        diameterText.text = getString(
            R.string.diameter_range_value,
            decimalNumberFormat.format(asteroid.estimatedDiameter.kilometers.minKilometers),
            decimalNumberFormat.format(asteroid.estimatedDiameter.kilometers.maxKilometers)
        )
    }

    private fun resetAsteroidPanel() {
        nameText.text = getString(R.string.value_placeholder)
        hazardText.text = getString(R.string.value_placeholder)
        distanceText.text = getString(R.string.value_placeholder)
        dateText.text = getString(R.string.value_placeholder)
        diameterText.text = getString(R.string.value_placeholder)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val notificationMessage =
            intent?.getStringExtra(MissionNotificationHelper.EXTRA_NOTIFICATION_MESSAGE) ?: return
        setMissionStatus(notificationMessage)
        intent.removeExtra(MissionNotificationHelper.EXTRA_NOTIFICATION_MESSAGE)
    }

    private fun setMissionStatus(message: String) {
        statusText.text = message
    }

    private fun updateNotificationPermissionChip(isReady: Boolean) {
        val textRes = if (isReady) {
            R.string.chip_notifications_ready
        } else {
            R.string.chip_notifications_needed
        }
        val colorRes = if (isReady) {
            R.color.console_green_dim
        } else {
            R.color.console_gold_dim
        }
        updateChip(notificationPermissionChip, getString(textRes), colorRes)
    }

    private fun updateSignalChip(textRes: Int, colorRes: Int) {
        updateChip(signalStatusChip, getString(textRes), colorRes)
    }

    private fun updateChip(view: TextView, text: String, colorRes: Int) {
        view.text = text
        view.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, colorRes))
    }

    private fun formatDistanceValue(rawDistance: String?): String {
        val value = rawDistance?.toDoubleOrNull() ?: return getString(R.string.value_unknown)
        return wholeNumberFormat.format(value)
    }

    companion object {
        private const val SIGNAL_DELAY_MS = 8_000L
        private const val SCAN_ALERT_DELAY_MS = 6_000L
    }
}
