package com.vuzix.ultralite.sample;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class ChaptersActivity extends AppCompatActivity {
    private static final String TAG = "ChaptersActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapters);
        ListView listView = findViewById(R.id.listViewChapters);

        // Parse toc.xhtml to get chapter titles and file paths
        List<ChapterItem> chapterItems = TocParser.parseToc(this);
        android.util.Log.d(TAG, "Parsed chapters: " + (chapterItems != null ? chapterItems.size() : 0));
        if (chapterItems != null && !chapterItems.isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
            for (ChapterItem item : chapterItems) {
                android.util.Log.d(TAG, "Chapter: " + item.title + " | File: " + item.filePath);
                adapter.add(item.title);
            }
            listView.setAdapter(adapter);
            listView.setOnItemClickListener((parent, view, position, id) -> {
                ChapterItem item = chapterItems.get(position);
                Intent intent = new Intent(ChaptersActivity.this, ChapterDetailActivity.class);
                intent.putExtra("chapter_file_path", item.filePath);
                intent.putExtra("chapter_title", item.title);
                startActivity(intent);
            });
        } else {
            android.util.Log.e(TAG, "No chapters found or failed to parse toc.xhtml");
        }
    }
}
