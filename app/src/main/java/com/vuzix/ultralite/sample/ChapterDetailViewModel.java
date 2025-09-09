package com.vuzix.ultralite.sample;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.vuzix.ultralite.UltraliteSDK;
import com.vuzix.ultralite.Layout;
import com.vuzix.ultralite.utils.scroll.LiveText;

public class ChapterDetailViewModel extends AndroidViewModel {
    private final UltraliteSDK ultralite;
    private boolean haveControlOfGlasses;
    private String[] contentParts;
    private final MutableLiveData<Boolean> sending = new MutableLiveData<>();

    public ChapterDetailViewModel(@NonNull Application application) {
        super(application);
        ultralite = UltraliteSDK.get(application);
        ultralite.getControlledByMe().observeForever(controlledObserver);
    }

    @Override
    protected void onCleared() {
        ultralite.releaseControl();
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                ultralite.getControlledByMe().removeObserver(controlledObserver), 500);
    }

    public void sendContentToGlasses(String[] parts) {
        contentParts = parts;
        if (haveControlOfGlasses) {
            startSendThread();
        } else {
            ultralite.requestControl();
        }
    }

    private void startSendThread() {
        // Force stop any running demo and clear demo content
        MainActivity.DemoActivityViewModel.stopDemoIfRunning(ultralite);
        
        // Ensure proper clearing and content display on main thread
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(() -> {
            if (haveControlOfGlasses && contentParts != null) {
                // Clear any existing content
                ultralite.getCanvas().clearBackground();
                
                // Remove text elements that might exist
                for (int textId = 0; textId < 20; textId++) {
                    ultralite.getCanvas().removeText(textId);
                }
                
                // Remove images that might exist
                for (int imageId = 0; imageId < 20; imageId++) {
                    ultralite.getCanvas().removeImage(imageId);
                }
                
                // Remove animations that might exist
                for (int animId = 0; animId < 20; animId++) {
                    ultralite.getCanvas().removeAnimation(animId);
                }
                
                ultralite.getCanvas().commit();
                
                // Wait a moment for clearing to complete
                new Handler().postDelayed(() -> {
                    new Thread(() -> {
                        sending.postValue(true);
                        try {
                            // Set layout to CANVAS
                            ultralite.setLayout(Layout.CANVAS, 0, true);
                            Thread.sleep(200);
                            
                            CanvasLayout.showChapterContent(getApplication().getApplicationContext(), ultralite, contentParts);
                        } catch (InterruptedException ignored) {
                            // Handle interruption gracefully
                        } finally {
                            sending.postValue(false);
                        }
                    }).start();
                }, 500);
            }
        });
    }

    private void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
        if (!haveControlOfGlasses) {
            throw new RuntimeException("Lost control of glasses");
        }
    }

    private final Observer<Boolean> controlledObserver = controlled -> {
        haveControlOfGlasses = controlled;
        if (controlled && contentParts != null) {
            startSendThread();
        }
    };
}