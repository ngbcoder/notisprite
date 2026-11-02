package com.notisprite.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.notisprite.app.R
import com.notisprite.app.databinding.ActivityMainBinding
import com.notisprite.app.databinding.RowPermissionBinding

/**
 * M0 landing screen: shows whether the two system-level permissions the app
 * relies on are granted and deep-links into the relevant settings screens.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.notificationAccessRow.grant.setOnClickListener {
            openNotificationListenerSettings()
        }
        binding.overlayRow.grant.setOnClickListener {
            openOverlaySettings()
        }
    }

    override fun onResume() {
        super.onResume()
        renderPermissionState()
    }

    private fun renderPermissionState() {
        val notificationAccess = hasNotificationAccess()
        val overlayAccess = Settings.canDrawOverlays(this)

        bindRow(
            row = binding.notificationAccessRow,
            titleRes = R.string.notification_access_title,
            descriptionRes = R.string.notification_access_description,
            grantLabelRes = R.string.grant_notification_access,
            granted = notificationAccess,
        )
        bindRow(
            row = binding.overlayRow,
            titleRes = R.string.overlay_title,
            descriptionRes = R.string.overlay_description,
            grantLabelRes = R.string.grant_overlay,
            granted = overlayAccess,
        )
    }

    private fun bindRow(
        row: RowPermissionBinding,
        @StringRes titleRes: Int,
        @StringRes descriptionRes: Int,
        @StringRes grantLabelRes: Int,
        granted: Boolean,
    ) {
        row.destinationTitle.setText(titleRes)
        row.destinationDescription.setText(descriptionRes)
        row.status.setText(if (granted) R.string.status_granted else R.string.status_missing)
        row.status.setTextColor(statusColor(granted))
        row.grant.setText(grantLabelRes)
        row.grant.isEnabled = !granted
    }

    private fun hasNotificationAccess(): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(this).contains(packageName)

    private fun statusColor(granted: Boolean): Int =
        ContextCompat.getColor(
            this,
            if (granted) R.color.sprite_green else R.color.status_missing,
        )

    private fun openNotificationListenerSettings() {
        startActivitySafely(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    private fun openOverlaySettings() {
        startActivitySafely(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"),
            ),
        )
    }

    private fun startActivitySafely(intent: Intent) {
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}
