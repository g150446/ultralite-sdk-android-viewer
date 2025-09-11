package com.vuzix.ultralite.sample;

import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.WindowManager;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.InputStream;

import android.view.Menu;
import android.view.MenuItem;
import androidx.lifecycle.ViewModelProvider;
// ...existing imports...

public class ChapterDetailActivity extends AppCompatActivity {
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_detail);
        
        // Keep screen awake - multiple approaches for maximum compatibility
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        // Initialize wake lock as backup method
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "UltraliteSDK:ChapterDetailKeepScreenAwake");
        
        // Acquire wake lock
        if (!wakeLock.isHeld()) {
            wakeLock.acquire();
        }

        String chapterFilePath = getIntent().getStringExtra("chapter_file_path");
        String chapterTitle = getIntent().getStringExtra("chapter_title");
        WebView webView = findViewById(R.id.webViewChapterDetail);

        String xhtmlContent = loadChapterXHTML(chapterFilePath);
        if (xhtmlContent != null) {
            webView.loadDataWithBaseURL(null, xhtmlContent, "application/xhtml+xml", "UTF-8", null);
        } else {
            webView.loadData("<html><body><h2>Chapter not found</h2></body></html>", "text/html", "UTF-8");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_chapter_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_send_content) {
            sendChapterContentToGlasses();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendChapterContentToGlasses() {
        String chapterFilePath = getIntent().getStringExtra("chapter_file_path");
        String xhtmlContent = loadChapterXHTML(chapterFilePath);
        if (xhtmlContent == null) return;

        // Extract text inside <p> tags only
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
                    
                    // Only create a new chunk if the current chunk has substantial content (at least 50 chars)
                    // This prevents tiny fragments from becoming separate chunks
                    if (currentChunk.length() >= 50) {
                        partsList.add(currentChunk.toString().trim());
                        currentChunk = new StringBuilder();
                    }
                    // If current chunk is too small, keep adding to it even if it exceeds maxChunkLength
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
        
        String[] parts = partsList.toArray(new String[0]);
        ChapterDetailViewModel model = new ViewModelProvider(this).get(ChapterDetailViewModel.class);
        model.sendContentToGlasses(parts);
    }

    // ...existing code...

    private String loadChapterXHTML(String chapterFilePath) {
        // Strip fragment if present
        String cleanPath = chapterFilePath;
        int hashIndex = chapterFilePath.indexOf('#');
        if (hashIndex != -1) {
            cleanPath = chapterFilePath.substring(0, hashIndex);
        }
        try (InputStream is = getAssets().open("alice-xhtml/" + cleanPath)) {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            return new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Re-acquire wake lock when activity resumes
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Keep wake lock active even when paused to maintain screen awake
        // Only release it in onDestroy to ensure screen stays awake
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release wake lock when activity is destroyed
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }
}
