# Keep Screen Awake Feature

This document describes the implementation of the "keep screen awake" feature in the Ultralite SDK Android Viewer app.

## Problem
The device is set to turn off the screen after 30 minutes without touch interaction. This can interrupt long reading sessions when users are reading chapters on their smart glasses.

## Solution
The app now implements multiple layers of screen wake functionality to ensure the screen stays awake during use:

### 1. Permissions Added
- `android.permission.WAKE_LOCK` permission added to AndroidManifest.xml

### 2. Implementation Approach
The solution uses a dual approach for maximum compatibility:

#### Method 1: Window Flags (Primary)
- Uses `WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON`
- Applied to activity windows in `onCreate()`
- Most efficient and recommended approach

#### Method 2: Wake Lock (Backup)
- Uses `PowerManager.WakeLock` with `SCREEN_DIM_WAKE_LOCK | ACQUIRE_CAUSES_WAKEUP`
- Provides additional insurance against screen timeout
- Managed through activity lifecycle methods

### 3. Activities Modified

#### MainActivity.java
- Added wake lock initialization in `onCreate()`
- Added lifecycle management (`onResume()`, `onPause()`, `onDestroy()`)
- Screen stays awake while on main activity

#### ChaptersActivity.java
- Same wake lock implementation as MainActivity
- Ensures screen stays awake while browsing chapter list
- Critical for long chapter selection sessions

#### ChapterDetailActivity.java
- Same wake lock implementation
- Keeps screen awake while reading individual chapters
- Essential for long reading sessions

#### CanvasLayout.java
- Added screen awake functionality for glasses content display
- Ensures phone screen stays awake during content transmission to glasses

### 4. Utility Class Created

#### WakeLockManager.java
A utility class that provides centralized wake lock management:
- Consistent wake lock handling across activities
- Error handling and logging
- Easy to maintain and extend
- Can be used for future activities

### 5. Lifecycle Management
The implementation properly manages wake locks through the Android activity lifecycle:

- **onCreate()**: Initialize and acquire wake lock
- **onResume()**: Re-acquire wake lock if needed
- **onPause()**: Keep wake lock active (don't release)
- **onDestroy()**: Release wake lock to prevent memory leaks

### 6. Benefits
- **Uninterrupted Reading**: Screen will not turn off during long reading sessions
- **Battery Efficient**: Uses the most efficient methods available
- **Robust**: Multiple fallback methods ensure compatibility
- **Proper Cleanup**: Prevents memory leaks through proper lifecycle management
- **Logged Operations**: Debug logging helps with troubleshooting

### 7. Usage
The feature works automatically - no user interaction required:

1. Launch the app → Screen stays awake
2. Browse chapters → Screen stays awake  
3. Read chapter content → Screen stays awake
4. Content displays on glasses → Phone screen stays awake
5. Exit app → Wake lock properly released

### 8. Technical Details
- **Wake Lock Type**: `SCREEN_DIM_WAKE_LOCK | ACQUIRE_CAUSES_WAKEUP`
- **Window Flags**: `FLAG_KEEP_SCREEN_ON`
- **Scope**: Per-activity implementation
- **Error Handling**: Try-catch blocks prevent crashes
- **Logging**: Debug logs track wake lock operations

This implementation ensures that users can have extended reading sessions without screen interruption while maintaining proper Android resource management.
