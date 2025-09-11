public class TestSentenceSplitting {
    /**
     * Test our sentence-aware text splitting approach
     */
    private static String[] splitTextIntoLines(String text, int maxWidth) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        
        // Use more conservative character limit for smart glasses
        int maxCharsPerLine = 40; // Fixed conservative limit for smart glasses
        
        // First try to split by sentences to preserve meaning
        String[] sentences = text.split("(?<=[.!?])\\s+");
        StringBuilder currentLine = new StringBuilder();
        
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
        }
        
        return lines.toArray(new String[0]);
    }

    public static void main(String[] args) {
        String sampleText = "Alice was beginning to get very tired of sitting by her sister on the bank. " +
                            "She had nothing to do. In another moment down went Alice after it! " +
                            "The rabbit-hole went straight on like a tunnel for some way and then dipped suddenly down.";
        
        String[] lines = splitTextIntoLines(sampleText, 400); // maxWidth parameter (ignored in our implementation)
        
        System.out.println("Original text: " + sampleText);
        System.out.println("Split into " + lines.length + " lines:");
        
        for (int i = 0; i < lines.length; i++) {
            System.out.println("Line " + (i+1) + ": [" + lines[i] + "] (" + lines[i].length() + " chars)");
        }
        
        // Verify all text is preserved
        StringBuilder reconstructed = new StringBuilder();
        for (String line : lines) {
            if (reconstructed.length() > 0) {
                reconstructed.append(" ");
            }
            reconstructed.append(line);
        }
        
        String reconstructedText = reconstructed.toString();
        boolean isEqual = sampleText.equals(reconstructedText);
        
        System.out.println("\nText preservation check: " + (isEqual ? "PASSED" : "FAILED"));
        
        if (!isEqual) {
            System.out.println("Original: " + sampleText);
            System.out.println("Reconstructed: " + reconstructedText);
        }
    }
}
