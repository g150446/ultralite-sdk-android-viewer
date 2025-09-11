import java.util.List;
import java.util.ArrayList;

public class TestTextSplitting {
    /**
     * Split text into lines that fit within the specified width, ensuring no sentences are lost
     * This is a standalone version of the fixed method for testing
     */
    private static String[] splitTextIntoLines(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        
        // Use more conservative estimates to prevent text overflow
        int approximateCharWidth = 16; // Increased from 14 to be more conservative
        int maxCharsPerLine = Math.max(20, (maxWidth / approximateCharWidth) - 4); // Increased safety margin
        
        // Since we're using TextWrapMode.WRAP, we can be less aggressive with line splitting
        // Split text into sentences first to preserve sentence boundaries
        String[] sentences = text.split("(?<=[.!?])\\s+");
        StringBuilder currentLine = new StringBuilder();
        
        for (String sentence : sentences) {
            sentence = sentence.trim();
            if (sentence.isEmpty()) continue;
            
            // If the sentence is very long, split by words within that sentence
            String[] wordsInSentence = sentence.split("\\s+");
            StringBuilder currentSentencePart = new StringBuilder();
            
            for (String word : wordsInSentence) {
                if (word.isEmpty()) continue;
                
                String testSentencePart = currentSentencePart.length() == 0 ? word : currentSentencePart + " " + word;
                String testLine = currentLine.length() == 0 ? testSentencePart : currentLine + " " + testSentencePart;
                
                // Check if adding this word would exceed line length
                if (testLine.length() <= maxCharsPerLine) {
                    if (currentSentencePart.length() > 0) {
                        currentSentencePart.append(" ");
                    }
                    currentSentencePart.append(word);
                } else {
                    // If current line has content, add it to lines
                    if (currentLine.length() > 0) {
                        lines.add(currentLine.toString().trim());
                        currentLine = new StringBuilder();
                    }
                    
                    // If current sentence part has content, start new line with it
                    if (currentSentencePart.length() > 0) {
                        currentLine.append(currentSentencePart.toString());
                        currentSentencePart = new StringBuilder();
                    }
                    
                    // Handle very long single words by character splitting as last resort
                    if (word.length() > maxCharsPerLine) {
                        if (currentLine.length() > 0) {
                            lines.add(currentLine.toString().trim());
                            currentLine = new StringBuilder();
                        }
                        
                        // Split the long word into chunks
                        for (int i = 0; i < word.length(); i += maxCharsPerLine) {
                            int endIndex = Math.min(i + maxCharsPerLine, word.length());
                            lines.add(word.substring(i, endIndex));
                        }
                    } else {
                        currentSentencePart.append(word);
                    }
                }
            }
            
            // Add completed sentence part to current line
            if (currentSentencePart.length() > 0) {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(currentSentencePart.toString());
            }
        }
        
        // Add the final line if it has content
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString().trim());
        }
        
        return lines.toArray(new String[0]);
    }

    public static void main(String[] args) {
        String sampleText = "This is a sample text with multiple sentences. " +
                            "Some sentences are short. Others are much longer and contain more words that need to be carefully handled. " +
                            "We want to ensure that no sentences are lost when splitting text into lines for display.";
        
        int maxWidth = 640; // Same as UltraliteSDK.Canvas.WIDTH - 40
        String[] lines = splitTextIntoLines(sampleText, maxWidth);
        
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
        }
    }
}
