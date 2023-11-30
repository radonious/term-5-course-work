package com.course.term5cw.Model;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class DictionaryFabric {
    // Map contains Adapter.class -> Word)
    private HashMap<String,AdapterFlyweight> map = new HashMap<>();

    // Fill the map, change counters and return text as adapters array
    public ArrayList<AdapterFlyweight> processFile(File file) throws Exception {
        // Result array
        ArrayList<AdapterFlyweight> res = new ArrayList<>();

        Scanner input = new Scanner(file);

        // Read the file word by word
        while (input.hasNext()) {
            String word = input.next();
            // Split {word,} to {word} and {,} with regex
            String[] split = word.split("(?=[\\p{Punct}\\s])");
            for (String s : split) {
                if (map.get(s) != null) {
                    // Change counter if map already contain a word
                    map.get(s).count++;
                } else {
                    // Create a new adapter, change it's counter and add to the map
                    AdapterFlyweight a = new AdapterFlyweight();
                    a.count++;
                    map.put(s, a);
                }
                // Add adapter to the result array
                res.add(map.get(s));
            }
        }

        // Return text as an adapters array
        return res;
    }
}
