// this class analyzes a set of mappings between words of different languages and creates
// connections, allowing us to input our own word in one language and use probabilities to
// determine a likely transition
import java.util.*;
import java.io.*;

public class Main {
    public static void main(String[] args) throws FileNotFoundException {
        Scanner console = new Scanner(System.in);
        Random rand = new Random();

        System.out.println("Welcome! Press any key to begin. To exit, press 'E'");
        Map<String, Map<String, Double>> diachrons = new TreeMap<>();
        createMatrix(diachrons);
        String choice = console.nextLine();

        while (!choice.equalsIgnoreCase("E")) {
            System.out.println("Enter a latin root, following conventions:");
            choice = console.nextLine();
            if (!choice.equalsIgnoreCase("E")) {
                testWord(diachrons, rand, choice);
            }
        }
    }

    // helper method that creates a map of each possible Latin to component to all English possibilities
    public static void createMap(Map<String, List<String>> transitions) {
        try {
            File inFile = new File("./resources/words.txt"); // hard coded input file
            Scanner s = new Scanner(inFile);

            while (s.hasNext()) {
                // cuts up the line into two words
                String long_line = s.nextLine().trim();
                int space = long_line.indexOf(" ");
                String long_latin = long_line.substring(0, space);
                String long_english = long_line.substring(space + 1);

                // compares each syllable and makes a new mapping if applicable
                boolean hasNextComponent = true;
                String short_latin = long_latin; //initializers
                String short_english = long_english;

                while (hasNextComponent) {
                    if (long_latin.contains("-")) { // if more syllables to look at
                        int dash = long_latin.indexOf("-");
                        short_latin = long_latin.substring(0, dash);

                        int eng_dash = long_english.indexOf("-");
                        short_english = long_english.substring(0, eng_dash);

                        long_latin = long_latin.substring(dash+1);
                        long_english = long_english.substring(eng_dash+1);

                    } else {
                        short_latin = long_latin;
                        short_english = long_english;
                        hasNextComponent = false; // reached end of the word
                    }

                    // make connections
                    if (!transitions.containsKey(short_latin)) { //create new mapping
                        transitions.put(short_latin, new ArrayList<>());
                    }
                    transitions.get(short_latin).add(short_english);
                }
            }

        } catch (FileNotFoundException e){
            //ERROR crash program
            System.out.println("Error: " + e);
            e.printStackTrace();
            System.exit(1);
        }
    }

    // creates a transition matrix (one sided) from a Latin component to possible English components
    public static void createMatrix(Map<String, Map<String, Double>> diachrons) {
        Map<String, List<String>> transitions = new TreeMap<>();
        createMap(transitions);

        for (String key : transitions.keySet()) {
            Map<String, Double> counts = new TreeMap<>();

            // organizes all possible transitions to get counts
            for (int i = 0; i < transitions.get(key).size(); i++) {
                String option = transitions.get(key).get(i);
                if (!counts.containsKey(option)) {
                    counts.put(option, 1.0);
                } else {
                    counts.put(option, counts.get(option) + 1);
                }
            }
            Map<String, Double> percents = new TreeMap<>();
            for (String nested_key : counts.keySet()) { // divide counts by total for percents
                double percent = counts.get(nested_key) / transitions.get(key).size();
                percents.put(nested_key, percent);
                diachrons.put(key, percents);
            }
        }

        // System.out.println(diachrons); //(uncomment this to see what the transition probabilities look like!)
    }

    // given a word, splits it up into components and checks whether we have a mapping, then
    // changes the component based on transition probability
    public static void testWord(Map<String, Map<String, Double>> diachrons, Random rand, String root) {
        StringBuilder leaf = new StringBuilder();

        while (root.contains("-")) {
            String short_root = root.substring(0, root.indexOf("-"));
            root = root.substring(root.indexOf("-")+1);

            if (diachrons.containsKey(short_root)) { // if we have a mapping, use it!
                leaf.append(changeComponent(diachrons.get(short_root), rand));
            } else {
                leaf.append(short_root); // otherwise word remains unchanged
            }
        }

        // repeat above for final part of word, not included in while loop
        if (diachrons.containsKey(root)) {
            leaf.append(changeComponent(diachrons.get(root), rand));
        } else {
            leaf.append(root);
        }

        System.out.println("English word: " + leaf);
        System.out.println();
    }

    // helper method that figures out how to change components based on transition matrix
    public static String changeComponent(Map<String, Double> row, Random rand) {
        double trans = rand.nextDouble();
        double placehold = 0.0;

         for (String key : row.keySet()) {
             placehold += row.get(key);
             if (trans < placehold) {
                 return key;
             }
         }

         return ""; // if something went wrong
    }
}