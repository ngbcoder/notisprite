package com.notisprite.app.services

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import timber.log.Timber

/**
 * Entry point for system notification events.
 *
 * M0 scope: the service is registered, binds correctly, and logs traffic so the
 * listener permission flow can be verified end to end.
 *
 * TODO(M2): filter irrelevant notifications, normalise them into a domain model,
 * and forward them to the overlay layer instead of logging.
 */
class NotiSpriteListener : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
        Timber.i("Notification listener connected (active=%d)", activeNotifications?.size ?: 0)
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Timber.w("Notification listener disconnected")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn ?: return
        Timber.d("Notification posted: pkg=%s key=%s", notification.packageName, notification.key)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        val notification = sbn ?: return
        Timber.d("Notification removed: pkg=%s key=%s", notification.packageName, notification.key)
    }
}
