package com.course.term5cw.Model;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Dictionary {
    // Map contains: Adapter.class -> Word)
    private HashMap<String, Adapter> map = new HashMap<>();

    // Fill the map, change counters and return text as adapters array
    public ArrayList<Adapter> addVersion(File file) throws Exception {
        ArrayList<Adapter> res = new ArrayList<>();
        Scanner input = new Scanner(file, StandardCharsets.UTF_8);
        while (input.hasNext()) {
            String line = input.nextLine();
            ArrayList<String> split = new ArrayList<>(List.of(line.split(
                    "(?<=[!\"#$%&()*+-,./:;<=>?@^_`{|}~])|(?=[ !\"#$%&()*+-,./:;<=>?@^_`{|}~])"
            ))); // Split {,word,} to {,} + {word} + {,} with regex
            split.replaceAll(String::trim);
            split.removeIf(String::isBlank);
            split.add("\n");
            for (String s : split) {
                if (map.get(s) != null) {
                    map.get(s).count++;
                } else {
                    Adapter a = new Adapter();
                    a.count++;
                    map.put(s, a);
                }
                // Add adapter to the result array
                res.add(map.get(s));
            }
        }
        input.close();
        // Return text as an adapters array
        return res;
    }

    public void removeVersion(ArrayList<Adapter> array) {
        for (Adapter adapter : array) {
            adapter.count--;
            // OPTIONAL: delete word from dictionary if count = 0
            if (adapter.count == 0) {
                map.remove(getWordByAdapter(adapter));
            }
        }
    }

    public String getWordByAdapter(Adapter adapter) {
        for (Map.Entry<String, Adapter> i : map.entrySet()) {
            if (adapter.equals(i.getValue())) {
                return i.getKey();
            }
        }
        return null;
    }

    public void printDict() {
        Set<String> keys = map.keySet();
        for (String key : keys) {
            System.out.println(key + " : " + map.get(key).count);
        }
    }
}
