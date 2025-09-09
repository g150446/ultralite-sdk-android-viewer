package com.vuzix.ultralite.sample;

import android.content.Context;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TocParser {
    public static List<ChapterItem> parseToc(Context context) {
        List<ChapterItem> chapters = new ArrayList<>();
        try {
            android.util.Log.d("TocParser", "Opening toc.xhtml");
            InputStream is = context.getAssets().open("alice-xhtml/toc.xhtml");
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(is, "UTF-8");
            int eventType = parser.getEventType();
            String title = null, filePath = null;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();
                    if ("a".equals(tagName)) {
                        filePath = parser.getAttributeValue(null, "href");
                        title = parser.nextText();
                        android.util.Log.d("TocParser", "Found <a>: title=" + title + ", filePath=" + filePath);
                        if (filePath != null && title != null && filePath.contains(".xhtml") && title.startsWith("CHAPTER")) {
                            chapters.add(new ChapterItem(title, filePath));
                        }
                    }
                }
                eventType = parser.next();
            }
            is.close();
            android.util.Log.d("TocParser", "Total chapters found: " + chapters.size());
        } catch (Exception e) {
            android.util.Log.e("TocParser", "Error parsing toc.xhtml", e);
        }
        return chapters;
    }
}

class ChapterItem {
    public String title;
    public String filePath;
    public ChapterItem(String title, String filePath) {
        this.title = title;
        this.filePath = filePath;
    }
}
