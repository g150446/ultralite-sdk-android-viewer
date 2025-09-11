public class TestNewTextSplitting {
    /**
     * Test the new conservative text splitting approach
     */
    private static String[] splitTextIntoLines(String text) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        
        // Use very conservative estimates based on actual smart glasses display constraints
        int maxCharsPerLine = 35; // Very conservative - roughly 35-40 chars per line for smart glasses
        
        System.out.println("Original text length: " + text.length() + " chars");
        System.out.println("Max chars per line: " + maxCharsPerLine);
        
        // Split by sentences to maintain readability and context
        String[] sentences = text.split("(?<=[.!?])\\s+");
        StringBuilder currentLine = new StringBuilder();
        
        for (int sentenceIndex = 0; sentenceIndex < sentences.length; sentenceIndex++) {
            String sentence = sentences[sentenceIndex].trim();
            if (sentence.isEmpty()) continue;
            
            System.out.println("Processing sentence " + (sentenceIndex + 1) + ": " + sentence);
            
            // Check if we can fit the entire sentence on the current line
            String testLine = currentLine.length() == 0 ? sentence : currentLine + " " + sentence;
            
            if (testLine.length() <= maxCharsPerLine) {
                // Entire sentence fits on current line
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(sentence);
                System.out.println("Sentence fits on current line. Line now: " + currentLine);
            } else {
                // Sentence doesn't fit, start new line
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    System.out.println("Added line " + lines.size() + ": " + currentLine);
                    currentLine = new StringBuilder();
                }
                
                // If sentence itself is too long, split it by words
                if (sentence.length() > maxCharsPerLine) {
                    String[] words = sentence.split("\\s+");
                    StringBuilder currentSentencePart = new StringBuilder();
                    
                    for (String word : words) {
                        if (word.isEmpty()) continue;
                        
                        String testSentencePart = currentSentencePart.length() == 0 ? word : currentSentencePart + " " + word;
                        
                        if (testSentencePart.length() <= maxCharsPerLine) {
                            if (currentSentencePart.length() > 0) {
                                currentSentencePart.append(" ");
                            }
                            currentSentencePart.append(word);
                        } else {
                            // Current word would exceed limit, finalize current part
                            if (currentSentencePart.length() > 0) {
                                if (currentLine.length() > 0) {
                                    lines.add(currentLine.toString());
                                    System.out.println("Added line " + lines.size() + ": " + currentLine);
                                    currentLine = new StringBuilder();
                                }
                                currentLine.append(currentSentencePart.toString());
                                lines.add(currentLine.toString());
                                System.out.println("Added line " + lines.size() + ": " + currentLine);
                                currentLine = new StringBuilder();
                                currentSentencePart = new StringBuilder();
                            }
                            
                            // Handle very long words by splitting them
                            if (word.length() > maxCharsPerLine) {
                                for (int i = 0; i < word.length(); i += maxCharsPerLine) {
                                    int endIndex = Math.min(i + maxCharsPerLine, word.length());
                                    String wordPart = word.substring(i, endIndex);
                                    lines.add(wordPart);
                                    System.out.println("Added line " + lines.size() + " (long word): " + wordPart);
                                }
                            } else {
                                currentSentencePart.append(word);
                            }
                        }
                    }
                    
                    // Add any remaining sentence part
                    if (currentSentencePart.length() > 0) {
                        currentLine.append(currentSentencePart.toString());
                    }
                } else {
                    // Sentence fits within limit, add it to current line
                    currentLine.append(sentence);
                }
            }
        }
        
        // Add the final line if it has content
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
            System.out.println("Added final line " + lines.size() + ": " + currentLine);
        }
        
        System.out.println("Total lines created: " + lines.size());
        
        return lines.toArray(new String[0]);
    }

    public static void main(String[] args) {
        String sampleText = "Alice was beginning to get very tired of sitting by her sister on the bank. " +
                            "She had nothing to do. In another moment down went Alice after it! " +
                            "The rabbit-hole went straight on like a tunnel for some way and then dipped suddenly down. " +
                            "Alice found herself falling down a very deep well.";
        
        String[] lines = splitTextIntoLines(sampleText);
        
        System.out.println("\n=== FINAL RESULT ===");
        System.out.println("Original text: " + sampleText);
        System.out.println("\nSplit into " + lines.length + " lines:");
        
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
        
        System.out.println("\nReconstructed text: " + reconstructedText);
        System.out.println("Text preservation check: " + (isEqual ? "PASSED" : "FAILED"));
        
        if (!isEqual) {
            System.out.println("Original length: " + sampleText.length());
            System.out.println("Reconstructed length: " + reconstructedText.length());
            System.out.println("Difference detected!");
        }
        
        // Simulate screen display
        System.out.println("\n=== SCREEN SIMULATION ===");
        int maxLinesPerScreen = 6;
        int screenNum = 1;
        
        for (int lineStart = 0; lineStart < lines.length; lineStart += maxLinesPerScreen) {
            System.out.println("--- SCREEN " + screenNum + " ---");
            int linesInScreen = Math.min(maxLinesPerScreen, lines.length - lineStart);
            
            for (int i = 0; i < linesInScreen; i++) {
                int lineIndex = lineStart + i;
                System.out.println((i + 1) + ": " + lines[lineIndex]);
            }
            
            if (lineStart + maxLinesPerScreen < lines.length) {
                System.out.println("(20 second pause for reading...)");
            }
            screenNum++;
        }
    }
}
