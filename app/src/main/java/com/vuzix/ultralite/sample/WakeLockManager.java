package com.vuzix.ultralite.sample;

import android.content.Context;
import android.os.PowerManager;
import android.view.Window;
import android.view.WindowManager;

/**
 * Utility class to manage wake locks and keep screen awake functionality
 * across the application to prevent screen timeout during long reading sessions.
 */
public class WakeLockManager {
    private static final String TAG = "WakeLockManager";
    
    private PowerManager.WakeLock wakeLock;
    private final Context context;
    private final String wakeLockTag;
    
    /**
     * Constructor for WakeLockManager
     * @param context The activity context
     * @param tag Unique tag for this wake lock instance
     */
    public WakeLockManager(Context context, String tag) {
        this.context = context;
        this.wakeLockTag = "UltraliteSDK:" + tag;
        initializeWakeLock();
    }
    
    /**
     * Initialize the wake lock
     */
    private void initializeWakeLock() {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, 
                wakeLockTag
            );
        }
    }
    
    /**
     * Apply screen awake flags to the window and acquire wake lock
     * @param window The activity window
     */
    public void keepScreenAwake(Window window) {
        // Method 1: Use window flags (preferred for most cases)
        if (window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        
        // Method 2: Use wake lock as backup
        acquireWakeLock();
    }
    
    /**
     * Acquire the wake lock if not already held
     */
    public void acquireWakeLock() {
        try {
            if (wakeLock != null && !wakeLock.isHeld()) {
                wakeLock.acquire();
                android.util.Log.d(TAG, "Wake lock acquired: " + wakeLockTag);
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error acquiring wake lock: " + e.getMessage());
        }
    }
    
    /**
     * Release the wake lock and clear window flags
     * @param window The activity window
     */
    public void allowScreenSleep(Window window) {
        // Method 1: Clear window flags
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        
        // Method 2: Release wake lock
        releaseWakeLock();
    }
    
    /**
     * Release the wake lock if currently held
     */
    public void releaseWakeLock() {
        try {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
                android.util.Log.d(TAG, "Wake lock released: " + wakeLockTag);
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error releasing wake lock: " + e.getMessage());
        }
    }
    
    /**
     * Check if the wake lock is currently held
     * @return true if wake lock is held, false otherwise
     */
    public boolean isWakeLockHeld() {
        return wakeLock != null && wakeLock.isHeld();
    }
    
    /**
     * Clean up resources - should be called in onDestroy()
     */
    public void cleanup() {
        releaseWakeLock();
        wakeLock = null;
    }
}
