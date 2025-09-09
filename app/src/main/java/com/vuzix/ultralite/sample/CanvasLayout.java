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
        
        int maxLines = 5; // Further reduced for better spacing
        int textFieldWidth = UltraliteSDK.Canvas.WIDTH - 40; // More padding
        int textFieldHeight = 35; // Conservative height to prevent overlapping
        int[] textIds = new int[maxLines];
        int startY = 25; // Top margin
        int lineSpacing = 60; // Generous spacing between lines to prevent overlap
        
        // Create text fields for chapter content with better positioning
        for (int i = 0; i < maxLines; i++) {
            int yPosition = startY + (i * lineSpacing);
            textIds[i] = ultralite.getCanvas().createText("", TextAlignment.LEFT, UltraliteColor.WHITE, 
                Anchor.TOP_LEFT, 20, yPosition, textFieldWidth, textFieldHeight, 
                TextWrapMode.WRAP, true); // Back to WRAP since NONE doesn't exist
        }
        ultralite.getCanvas().commit();
        
        // Display content parts
        int totalParts = contentParts.length;
        for (int partIndex = 0; partIndex < totalParts; partIndex++) {
            String currentPart = contentParts[partIndex];
            
            // Split the current part into lines that fit the display
            String[] lines = splitTextIntoLines(currentPart, textFieldWidth);
            
            // Display lines in chunks that fit on screen
            for (int lineStart = 0; lineStart < lines.length; lineStart += maxLines) {
                // Clear all text fields first
                for (int i = 0; i < maxLines; i++) {
                    ultralite.getCanvas().updateText(textIds[i], "");
                }
                
                // Fill text fields with current lines
                for (int i = 0; i < maxLines && (lineStart + i) < lines.length; i++) {
                    String line = lines[lineStart + i].trim();
                    ultralite.getCanvas().updateText(textIds[i], line);
                }
                ultralite.getCanvas().commit();
                
                try {
                    Thread.sleep(15000); // Wait 15 seconds before next screen
                } catch (InterruptedException ignored) {}
            }
        }
        
        // Clean up text fields after done
        for (int id : textIds) {
            ultralite.getCanvas().removeText(id);
        }
        ultralite.getCanvas().commit();
    }
    
    /**
     * Split text into lines that fit within the specified width
     */
    private static String[] splitTextIntoLines(String text, int maxWidth) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();
        
        int approximateCharWidth = 14; // More conservative character width estimate
        int maxCharsPerLine = (maxWidth / approximateCharWidth) - 2; // Extra safety margin
        
        for (String word : words) {
            if (word.isEmpty()) continue;
            
            // Check if adding this word would exceed the line length
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            
            if (testLine.length() <= maxCharsPerLine) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            } else {
                // Add current line if it has content
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                
                // Handle very long words
                if (word.length() > maxCharsPerLine) {
                    // Split long words
                    for (int i = 0; i < word.length(); i += maxCharsPerLine) {
                        int endIndex = Math.min(i + maxCharsPerLine, word.length());
                        lines.add(word.substring(i, endIndex));
                    }
                } else {
                    currentLine.append(word);
                }
            }
        }
        
        // Add the last line if it has content
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines.toArray(new String[0]);
    }
}
