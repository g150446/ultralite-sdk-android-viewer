package com.vuzix.ultralite.sample;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import com.vuzix.ultralite.UltraliteSDK;
import com.vuzix.ultralite.Layout;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ChaptersViewModel extends AndroidViewModel {
    private final UltraliteSDK ultralite;
    private boolean haveControlOfGlasses;
    private List<ChapterItem> chapterItems;
    private int currentChapterIndex = 0;
    private final MutableLiveData<Boolean> sending = new MutableLiveData<>();
    private final MutableLiveData<String> currentChapterTitle = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentChapterProgress = new MutableLiveData<>();
    private boolean shouldStop = false;

    public ChaptersViewModel(@NonNull Application application) {
        super(application);
        ultralite = UltraliteSDK.get(application);
        ultralite.getControlledByMe().observeForever(controlledObserver);
    }

    @Override
    protected void onCleared() {
        ultralite.releaseControl();
        shouldStop = true;
        new Handler(Looper.getMainLooper()).postDelayed(() ->
                ultralite.getControlledByMe().removeObserver(controlledObserver), 500);
    }

    public MutableLiveData<Boolean> getSending() {
        return sending;
    }

    public MutableLiveData<String> getCurrentChapterTitle() {
        return currentChapterTitle;
    }

    public MutableLiveData<Integer> getCurrentChapterProgress() {
        return currentChapterProgress;
    }

    public void sendAllChaptersToGlasses(List<ChapterItem> chapters) {
        this.chapterItems = chapters;
        this.currentChapterIndex = 0;
        this.shouldStop = false;
        if (haveControlOfGlasses) {
            startSendAllChaptersThread();
        } else {
            ultralite.requestControl();
        }
    }

    public void stopSending() {
        shouldStop = true;
        sending.postValue(false);
    }

    private void startSendAllChaptersThread() {
        // Force stop any running demo and clear demo content
        MainActivity.DemoActivityViewModel.stopDemoIfRunning(ultralite);
        
        // Ensure proper clearing and content display on main thread
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(() -> {
            if (haveControlOfGlasses && chapterItems != null && !chapterItems.isEmpty()) {
                clearCanvas();
                
                // Wait a moment for clearing to complete
                new Handler().postDelayed(() -> {
                    new Thread(() -> {
                        sending.postValue(true);
                        try {
                            // Set layout to CANVAS
                            ultralite.setLayout(Layout.CANVAS, 0, true);
                            Thread.sleep(200);
                            
                            sendChaptersSequentially();
                        } catch (InterruptedException ignored) {
                            // Handle interruption gracefully
                        } finally {
                            sending.postValue(false);
                            currentChapterTitle.postValue("");
                            currentChapterProgress.postValue(0);
                        }
                    }).start();
                }, 500);
            }
        });
    }

    private void clearCanvas() {
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
    }

    private void sendChaptersSequentially() {
        for (currentChapterIndex = 0; currentChapterIndex < chapterItems.size() && !shouldStop; currentChapterIndex++) {
            ChapterItem chapter = chapterItems.get(currentChapterIndex);
            
            // Update current chapter info on main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                currentChapterTitle.postValue(chapter.title);
                currentChapterProgress.postValue(currentChapterIndex + 1);
            });
            
            // Load and send chapter content
            String[] contentParts = loadChapterContentParts(chapter.filePath);
            if (contentParts != null && contentParts.length > 0) {
                CanvasLayout.showChapterContent(getApplication().getApplicationContext(), ultralite, contentParts);
            }
            
            // Small pause between chapters if not stopped
            if (!shouldStop && currentChapterIndex < chapterItems.size() - 1) {
                try {
                    Thread.sleep(2000); // 2 second pause between chapters
                } catch (InterruptedException ignored) {
                    break;
                }
            }
        }
    }

    private String[] loadChapterContentParts(String chapterFilePath) {
        String xhtmlContent = loadChapterXHTML(chapterFilePath);
        if (xhtmlContent == null) return null;

        // Extract text inside <p> tags only (same logic as ChapterDetailActivity)
        java.util.regex.Pattern pTag = java.util.regex.Pattern.compile("<p[^>]*>(.*?)</p>", java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = pTag.matcher(xhtmlContent);
        StringBuilder allText = new StringBuilder();
        
        // Collect all paragraph text in order
        while (matcher.find()) {
            String text = matcher.group(1).replaceAll("<[^>]+>", "").trim(); // Remove any nested tags
            if (!text.isEmpty()) {
                allText.append(text);
                if (!text.endsWith(".") && !text.endsWith("!") && !text.endsWith("?")) {
                    allText.append(" ");
                } else {
                    allText.append(" ");
                }
            }
        }
        
        // Split text into properly sized chunks for display, ensuring sentences stay intact
        String fullText = allText.toString().trim();
        java.util.List<String> partsList = new java.util.ArrayList<>();
        
        if (!fullText.isEmpty()) {
            // Split by sentences, keeping punctuation
            String[] sentences = fullText.split("(?<=[.!?])\\s+");
            StringBuilder currentChunk = new StringBuilder();
            int maxChunkLength = 400; // Adjust based on display capacity
            
            for (String sentence : sentences) {
                sentence = sentence.trim();
                if (sentence.isEmpty()) continue;
                
                // If adding this sentence would exceed max length, start a new chunk
                if (currentChunk.length() > 0 && 
                    (currentChunk.length() + sentence.length() + 1) > maxChunkLength) {
                    partsList.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();
                }
                
                if (currentChunk.length() > 0) {
                    currentChunk.append(" ");
                }
                currentChunk.append(sentence);
            }
            
            // Add the last chunk if it has content
            if (currentChunk.length() > 0) {
                partsList.add(currentChunk.toString().trim());
            }
        }
        
        return partsList.toArray(new String[0]);
    }

    private String loadChapterXHTML(String chapterFilePath) {
        // Strip fragment if present
        String cleanPath = chapterFilePath;
        int hashIndex = chapterFilePath.indexOf('#');
        if (hashIndex != -1) {
            cleanPath = chapterFilePath.substring(0, hashIndex);
        }
        try (InputStream is = getApplication().getAssets().open("alice-xhtml/" + cleanPath)) {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            return new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private final Observer<Boolean> controlledObserver = controlled -> {
        haveControlOfGlasses = controlled;
        if (controlled && chapterItems != null && !chapterItems.isEmpty()) {
            startSendAllChaptersThread();
        }
    };
}
