package wordHelper;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.ArrayList;

public class jeffgoldblum {
	
    public static void main(String[] args) {
        String file = "wordle.csv";
        String result = "resultFile.csv";
        Scanner scan = new Scanner(System.in);
        String resultFile = "";
        ArrayList<String> arr = new ArrayList<>();

        try (
            BufferedReader reader = new BufferedReader(new FileReader(file));
        ) {
        	String line;
        	while ((line = reader.readLine()) != null) {
                String word = line.trim();
                arr.add(line);
            }
        	
            char nonLetter = 'a';
            System.out.println("input all letters that are not in this word.");
            while (nonLetter != '0') {
	            nonLetter = scan.next().charAt(0);
	            for (int i = 0; i < arr.size(); i++) {
	                String c = String.valueOf(nonLetter);
	                if (arr.get(i).indexOf(c) != -1) {
	                	arr.remove(i);
	                	i--;
	                }
	            }
            }
            for (String a : arr) {
            	System.out.println(a);
            }
            System.out.println(arr.size() + " words left");
            
            char letter = 'a';
            System.out.println("\ninput all letters that are in this word.");
            while (letter != '0') {
	            letter = scan.next().charAt(0);
	            if (letter == '0')
	            	break;
	            for (int i = 0; i < arr.size(); i++) {
	                String c = String.valueOf(letter);
	                if (arr.get(i).indexOf(c) == -1) {
	                	arr.remove(i);
	                	i--;
	                }
	            }
            }
            for (String a : arr) {
            	System.out.println(a);
            }
            System.out.println(arr.size() + " words left");
            
            
            int position = 1;
            System.out.println("\ninput the position of the letter followed by the letter");
            while (position != 0) {
	            position = scan.nextInt();
	            if (position == 0) {
	                break;
	            }
	            letter = scan.next().charAt(0);
	            System.out.println(arr.size());
	            for (int i = 0; i < arr.size(); i++) {
	                if (arr.get(i).charAt(position - 1) != letter) {
	                	arr.remove(i);
	                	i--;
	                }
	            }
            }
            for (String a : arr) {
            	System.out.println(a);
            }
            System.out.println(arr.size() + " words left");
            
            position = 1;
            System.out.println("\ninput the position of a nonletter followed by the nonletter");
            while (position != 0) {
	            position = scan.nextInt();
	            if (position == 0) {
	                break;
	            }
	            letter = scan.next().charAt(0);
	            for (int i = 0; i < arr.size(); i++) {
	                if (arr.get(i).charAt(position - 1) == letter) {
	                	arr.remove(i);
	                	i--;
	                }
	            }
            }
            for (String a : arr) {
            	System.out.println(a);
            }
            System.out.println(arr.size() + " words left");

            

        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
            e.printStackTrace();
        }
    }
}