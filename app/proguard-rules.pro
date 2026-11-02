# Add project specific ProGuard rules here.
# For more details, see https://developer.android.com/build/shrink-code

# Keep the notification listener service entry point (bound by the system).
-keep class com.notisprite.app.services.NotiSpriteListener { *; }

# Timber: strip verbose/debug logging from release builds.
-dontwarn org.jetbrains.annotations.**
