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
        
        // Filter out very short content parts that might cause display issues
        java.util.List<String> filteredParts = new java.util.ArrayList<>();
        for (String part : contentParts) {
            if (part != null && part.trim().length() > 10) { // Minimum 10 characters
                filteredParts.add(part);
            } else {
                android.util.Log.d("CanvasLayout", "Filtering out short part: \"" + part + "\" (length: " + (part != null ? part.length() : 0) + ")");
            }
        }
        
        // Convert all parts into a single continuous stream of lines
        java.util.List<String> allLines = new java.util.ArrayList<>();
        for (int partIndex = 0; partIndex < filteredParts.size(); partIndex++) {
            String currentPart = filteredParts.get(partIndex);
            
            // Log the actual content part being processed
            android.util.Log.d("CanvasLayout", "Part " + (partIndex + 1) + "/" + filteredParts.size() + ": \"" + currentPart + "\" (length: " + currentPart.length() + ")");
            
            // Split the current part into lines that fit the display
            String[] lines = splitTextIntoLines(currentPart, textFieldWidth);
            
            android.util.Log.d("CanvasLayout", "Part " + (partIndex + 1) + "/" + filteredParts.size() + " split into " + lines.length + " lines");
            
            // Add all lines from this part to the continuous stream
            for (String line : lines) {
                if (line != null && !line.trim().isEmpty()) {
                    allLines.add(line.trim());
                }
            }
        }
        
        android.util.Log.d("CanvasLayout", "Total lines to display: " + allLines.size());
        
        // Display all lines continuously, filling each screen completely
        int lineIndex = 0;
        while (lineIndex < allLines.size()) {
            // Clear all text fields first
            for (int i = 0; i < maxLines; i++) {
                ultralite.getCanvas().updateText(textIds[i], "");
            }
            
            // Fill text fields with lines, maximizing screen usage
            int displayIndex = 0;
            while (lineIndex < allLines.size() && displayIndex < maxLines) {
                String line = allLines.get(lineIndex);
                ultralite.getCanvas().updateText(textIds[displayIndex], line);
                android.util.Log.d("CanvasLayout", "Displaying line " + (displayIndex + 1) + ": \"" + line + "\" (length: " + line.length() + ")");
                displayIndex++;
                lineIndex++;
            }
            ultralite.getCanvas().commit();
            
            try {
                Thread.sleep(15000); // Wait 15 seconds before next screen
            } catch (InterruptedException ignored) {}
        }
        
        // Clean up text fields after done
        for (int id : textIds) {
            ultralite.getCanvas().removeText(id);
        }
        ultralite.getCanvas().commit();
    }
    
    /**
     * Split text into lines that fit within the specified width, preserving sentence boundaries
     */
    private static String[] splitTextIntoLines(String text, int maxWidth) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        
        // Use more conservative character limit for smart glasses
        int maxCharsPerLine = 40; // Fixed conservative limit for smart glasses
        
        android.util.Log.d("CanvasLayout", "Splitting text of length " + text.length() + " with max " + maxCharsPerLine + " chars per line");
        
        // First try to split by sentences to preserve meaning
        String[] sentences = text.split("(?<=[.!?])\\s+");
        StringBuilder currentLine = new StringBuilder();
        
        android.util.Log.d("CanvasLayout", "Found " + sentences.length + " sentences to process");
        
        // Process sentences to maintain context and meaning
        for (String sentence : sentences) {
            if (sentence.trim().isEmpty()) continue;
            
            // Check if entire sentence fits on current line
            String testLine = currentLine.length() == 0 ? sentence.trim() : currentLine + " " + sentence.trim();
            
            if (testLine.length() <= maxCharsPerLine) {
                // Entire sentence fits
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(sentence.trim());
            } else {
                // Sentence doesn't fit, finalize current line if it has content
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                
                // Now handle the sentence - split by words if needed
                if (sentence.trim().length() > maxCharsPerLine) {
                    String[] words = sentence.trim().split("\\s+");
                    
                    for (String word : words) {
                        if (word.isEmpty()) continue;
                        
                        String testWordLine = currentLine.length() == 0 ? word : currentLine + " " + word;
                        
                        if (testWordLine.length() <= maxCharsPerLine) {
                            if (currentLine.length() > 0) {
                                currentLine.append(" ");
                            }
                            currentLine.append(word);
                        } else {
                            // Word doesn't fit, finalize current line
                            if (currentLine.length() > 0) {
                                lines.add(currentLine.toString());
                                currentLine = new StringBuilder();
                            }
                            
                            // Handle very long words by splitting them
                            if (word.length() > maxCharsPerLine) {
                                for (int i = 0; i < word.length(); i += maxCharsPerLine) {
                                    int endIndex = Math.min(i + maxCharsPerLine, word.length());
                                    lines.add(word.substring(i, endIndex));
                                }
                            } else {
                                currentLine.append(word);
                            }
                        }
                    }
                } else {
                    // Sentence fits within limit, add it to current line
                    currentLine.append(sentence.trim());
                }
            }
        }
        
        // Add the last line if it has content
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
            android.util.Log.d("CanvasLayout", "Added final line: " + currentLine.toString());
        }
        
        // Filter out any empty lines that might have been created
        java.util.List<String> filteredLines = new java.util.ArrayList<>();
        for (String line : lines) {
            if (line != null && !line.trim().isEmpty()) {
                filteredLines.add(line.trim());
            }
        }
        
        android.util.Log.d("CanvasLayout", "Text splitting complete. Created " + filteredLines.size() + " non-empty lines (filtered from " + lines.size() + " total lines)");
        
        return filteredLines.toArray(new String[0]);
    }
}
