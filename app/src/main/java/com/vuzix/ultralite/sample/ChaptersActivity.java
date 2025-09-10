package com.vuzix.ultralite.sample;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import java.util.List;

public class ChaptersActivity extends AppCompatActivity {
    private static final String TAG = "ChaptersActivity";
    private List<ChapterItem> chapterItems;
    private ChaptersViewModel chaptersViewModel;
    private TextView statusTextView;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapters);
        
        ListView listView = findViewById(R.id.listViewChapters);
        statusTextView = findViewById(R.id.statusTextView);
        
        // Initialize ViewModel
        chaptersViewModel = new ViewModelProvider(this).get(ChaptersViewModel.class);

        // Parse toc.xhtml to get chapter titles and file paths
        chapterItems = TocParser.parseToc(this);
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
        
        // Observe ViewModel states
        observeViewModelStates();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.activity_chapters_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_send_all_content) {
            sendAllChaptersToGlasses();
            return true;
        } else if (item.getItemId() == R.id.action_stop_sending) {
            stopSending();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void sendAllChaptersToGlasses() {
        if (chapterItems != null && !chapterItems.isEmpty()) {
            chaptersViewModel.sendAllChaptersToGlasses(chapterItems);
        }
    }
    
    private void stopSending() {
        chaptersViewModel.stopSending();
    }
    
    private void updateMenuVisibility(boolean isSending) {
        if (menu != null) {
            MenuItem sendItem = menu.findItem(R.id.action_send_all_content);
            MenuItem stopItem = menu.findItem(R.id.action_stop_sending);
            
            sendItem.setVisible(!isSending);
            stopItem.setVisible(isSending);
        }
    }
    
    private void observeViewModelStates() {
        chaptersViewModel.getSending().observe(this, sending -> {
            if (sending != null) {
                android.util.Log.d(TAG, "Sending state: " + sending);
                updateMenuVisibility(sending);
                if (sending) {
                    statusTextView.setVisibility(android.view.View.VISIBLE);
                } else {
                    statusTextView.setVisibility(android.view.View.GONE);
                    statusTextView.setText("");
                }
            }
        });
        
        chaptersViewModel.getCurrentChapterTitle().observe(this, title -> {
            if (title != null && !title.isEmpty()) {
                setTitle("Sending: " + title);
                statusTextView.setText("Currently sending: " + title);
            } else {
                setTitle("Chapters");
                statusTextView.setText("");
            }
        });
        
        chaptersViewModel.getCurrentChapterProgress().observe(this, progress -> {
            if (progress != null && chapterItems != null && progress > 0) {
                String progressText = "Chapter " + progress + " of " + chapterItems.size();
                if (statusTextView.getText().toString().isEmpty()) {
                    statusTextView.setText(progressText);
                } else {
                    statusTextView.setText(statusTextView.getText() + " (" + progressText + ")");
                }
                android.util.Log.d(TAG, "Progress: " + progressText);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chaptersViewModel != null) {
            chaptersViewModel.stopSending();
        }
    }
}
