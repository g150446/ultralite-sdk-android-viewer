package com.vuzix.ultralite.sample;

import android.content.Context;
import com.vuzix.ultralite.Anchor;
import com.vuzix.ultralite.Layout;
import com.vuzix.ultralite.TextAlignment;
import com.vuzix.ultralite.TextWrapMode;
import com.vuzix.ultralite.UltraliteColor;
import com.vuzix.ultralite.UltraliteSDK;

/**
 * CanvasLayout for chapter content only (no demo content)
 */
public class CanvasLayout {
    public static void showChapterContent(Context context, UltraliteSDK ultralite, String[] contentParts) {
        // Ensure we have a clean canvas layout
        ultralite.setLayout(Layout.CANVAS, 0, true);
        
        // Wait a moment for the layout to be set
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {}
        
        int maxLines = 8;
        int textFieldWidth = UltraliteSDK.Canvas.WIDTH;
        int textFieldHeight = 48; // Adjust as needed
        int[] textIds = new int[maxLines];
        
        // Create text fields for chapter content
        for (int i = 0; i < maxLines; i++) {
            textIds[i] = ultralite.getCanvas().createText("", TextAlignment.LEFT, UltraliteColor.WHITE, 
                Anchor.TOP_LEFT, 10, 10 + (i * textFieldHeight), textFieldWidth - 20, textFieldHeight, 
                TextWrapMode.WRAP, true);
        }
        ultralite.getCanvas().commit();
        
        // Display content parts
        int totalLines = contentParts.length;
        for (int i = 0; i < totalLines; i += maxLines) {
            for (int j = 0; j < maxLines; j++) {
                int idx = i + j;
                String line = (idx < totalLines) ? contentParts[idx].trim() : "";
                ultralite.getCanvas().updateText(textIds[j], line);
            }
            ultralite.getCanvas().commit();
            try {
                Thread.sleep(10000); // Wait 10 seconds before next section
            } catch (InterruptedException ignored) {}
        }
        
        // Clean up text fields after done
        for (int id : textIds) {
            ultralite.getCanvas().removeText(id);
        }
        ultralite.getCanvas().commit();
    }
}
