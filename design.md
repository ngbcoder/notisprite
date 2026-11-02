# NotiSprite - Technical Design Document

## 1. Executive Summary
**NotiSprite** is an AMOLED-optimized Android notification utility designed for screen-off states. Replacing generic LED notification dots or standard edge lighting, **NotiSprite** displays animated retro pixel-art icons (sprites) on pitch-black screens to signal incoming notifications. It balances high visual personality with hyper-efficient battery consumption and AMOLED burn-in protection.

---

## 2. Product Objectives & Target Audience
* **Objectives:** 
  * Provide customizable, retro-style visual indicators for screen-off alerts.
  * Ensure true-black background rendering ($#000000$) to optimize battery life on OLED/AMOLED displays.
  * Incorporate active burn-in protection and smart sensor controls (pocket detection).
* **Target Audience:** Android power-users, retro gaming enthusiasts, and users seeking novel notification indicators on modern bezel-less phones.

---

## 3. Core Feature Set

### 3.1 Pixel-Art Sprite Engine
* **Asset Specifications:** Crisp 16x16 or 24x24 pixel icons rendered with nearest-neighbor canvas filtering (`filterBitmap = false`).
* **App-Specific Mapping:** Automatic sprite selection based on the notifying package name (e.g., chat bubble for messaging, pixel envelope for email, pixel heart for priority contacts).
* **Multi-Notification Stacking:** If multiple notifications are active, the app cycles through corresponding sprites every few seconds or displays side-by-side micro-sprites.

### 3.2 Battery & Screen Protections
* **AMOLED Pure-Black Canvas:** All pixels outside the active sprite boundary are set to true black ($#000000$), keeping unused screen areas completely powered off.
* **Burn-In Offset Engine:** A background handler randomly adjusts sprite position by $\pm 5\text{px}$ every 60 seconds to prevent OLED pixel degradation.
* **Proximity Sensor Integration:** Automatically pauses overlay rendering when the proximity sensor detects a covered display (e.g., face-down on a desk or placed inside a pocket).

### 3.3 Dynamic Visual FX & Customization
* **Dynamic Palette Extraction:** Extracts primary colors from the app icon using Android’s `Palette` API to tint monochrome fallback sprites automatically.
* **Animation Modes:** Includes multiple sprite behaviors:
  * **Breathing:** Smooth alpha fading ($20\% \rightarrow 100\%$).
  * **Pixel Pulse:** Scale expansion/contraction.
  * **Static retro blink:** Classical hardware LED toggling.

---

## 4. Technical Architecture & Component Specs

```
   ┌────────────────────────────────────────────────────────┐
   │             Android System Notifications               │
   └───────────────────────────┬────────────────────────────┘
                               │
                               ▼
   ┌────────────────────────────────────────────────────────┐
   │  NotificationListenerService (MyNotificationListener) │
   │  - Filters package names & extracts app alerts          │
   └───────────────────────────┬────────────────────────────┘
                               │
                               ▼
   ┌────────────────────────────────────────────────────────┐
   │                  Overlay Manager                       │
   │  - Evaluates Proximity Sensor (SensorManager)          │
   │  - Retrieves App Sprite Mapping                        │
   └───────────────────────────┬────────────────────────────┘
                               │
                               ▼
   ┌────────────────────────────────────────────────────────┐
   │       WindowManager System Overlay (TYPE_APPLICATION)   │
   │  - Displays Custom PixelSpriteView (Canvas Drawing)    │
   │  - Executes Burn-In Protection Position Shifter        │
   └────────────────────────────────────────────────────────┘
```

### 4.1 Required Android Permissions
```xml
<!-- Overlay Drawing over Dark Screen -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

<!-- CPU Wakelock during Alert Events -->
<uses-permission android:name="android.permission.WAKE_LOCK" />

<!-- System Notification Listener Access -->
<service 
    android:name=".services.NotiSpriteListener"
    android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE"
    android:exported="true">
    <intent-filter>
        <action android:name="android.service.notification.NotificationListenerService" />
    </intent-filter>
</service>
```

---

## 5. Implementation Blueprints

### 5.1 Custom Pixel View Component (`PixelSpriteView.kt`)
```kotlin
package com.notisprite.app.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class PixelSpriteView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var spriteBitmap: Bitmap? = null
    private val paint = Paint().apply {
        isAntiAlias = false // Retains sharp pixel boundaries
        filterBitmap = false
    }

    private var currentAlpha = 255
    private var isFadingOut = true

    fun setSprite(bitmap: Bitmap) {
        this.spriteBitmap = bitmap
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bitmap = spriteBitmap ?: return
        
        paint.alpha = currentAlpha

        // Center calculation with 4x scaling factor for crisp presentation
        val spriteSize = 96 
        val left = (width - spriteSize) / 2
        val top = (height - spriteSize) / 2
        val destRect = Rect(left, top, left + spriteSize, top + spriteSize)

        canvas.drawBitmap(bitmap, null, destRect, paint)
    }

    fun stepBreathingAnimation() {
        if (isFadingOut) {
            currentAlpha -= 10
            if (currentAlpha <= 50) isFadingOut = false
        } else {
            currentAlpha += 10
            if (currentAlpha >= 255) isFadingOut = true
        }
        invalidate()
    }
}
```

### 5.2 Proximity & Burn-In Manager Logic
```kotlin
package com.notisprite.app.managers

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager

class OverlayProtectionManager(
    private val windowManager: WindowManager,
    private val overlayView: View,
    private val layoutParams: WindowManager.LayoutParams,
    sensorManager: SensorManager
) : SensorEventListener {

    private val handler = Handler(Looper.getMainLooper())
    private val shiftInterval = 60_000L // 1-minute offset interval
    private val proximitySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

    private val burnInRunnable = object : Runnable {
        override fun run() {
            // Random offset within -5 to +5 pixels
            layoutParams.x += (-5..5).random()
            layoutParams.y += (-5..5).random()
            windowManager.updateViewLayout(overlayView, layoutParams)
            handler.postDelayed(this, shiftInterval)
        }
    }

    fun startProtection() {
        handler.post(burnInRunnable)
    }

    fun stopProtection() {
        handler.removeCallbacks(burnInRunnable)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val distance = event?.values?.get(0) ?: return
        val maxRange = proximitySensor?.maximumRange ?: return

        // Hide overlay completely when device is in a pocket or face-down
        if (distance < maxRange) {
            overlayView.visibility = View.GONE
        } else {
            overlayView.visibility = View.VISIBLE
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
```

---

## 6. Roadmap & Future Scope
* **Phase 1 (MVP):** Core notification listener service, basic 16x16 sprite renderer, pure black screen-off overlay, proximity sensor guard.
* **Phase 2:** Custom Sprite Pack Editor (allowing users to draw their own 16x16 pixel sprites in-app).
* **Phase 3:** Tap-to-preview notifications directly over the dark canvas without waking the full display.
