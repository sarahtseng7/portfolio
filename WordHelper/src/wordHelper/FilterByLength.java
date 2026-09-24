package wordHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FilterByLength {

    public static void main(String[] args) {
        String inputFile = "words_alpha.csv";
        String outputFile = "random.csv";

        try (
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))
        ) {
            String line;
            int count = 0;

            // Loop through the input file line by line
            while ((line = reader.readLine()) != null) {
                String word = line.trim();

                // Only keep words that are exactly 5 letters long
                if (word.length() == 7) {
                    writer.write(word);
                    writer.newLine();
                    count++;
                }
            }

            System.out.println("Done! Wrote " + count + " twelve-letter words to " + outputFile);

        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
            e.printStackTrace();
        }
    }
}