package com.vuzix.ultralite.sample;

import android.os.Bundle;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.InputStream;

import android.view.Menu;
import android.view.MenuItem;
import androidx.lifecycle.ViewModelProvider;
// ...existing imports...

public class ChapterDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_detail);

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
        java.util.List<String> partsList = new java.util.ArrayList<>();
        while (matcher.find()) {
            String text = matcher.group(1).replaceAll("<[^>]+>", "").trim(); // Remove any nested tags
            if (!text.isEmpty()) {
                partsList.add(text);
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
}
