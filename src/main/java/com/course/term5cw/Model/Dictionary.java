package com.course.term5cw.Model;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Dictionary implements Serializable {
    // Map contains: Adapter.class -> Word)
    private HashMap<String, Adapter> dict;

    public Dictionary() {
        dict = new HashMap<>();
    }

    public Dictionary(HashMap<String, Adapter> map) {
        dict = map;
    }

    public HashMap<String, Adapter> getDict() {
        return dict;
    }

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
                if (dict.get(s) != null) {
                    dict.get(s).count++;
                } else {
                    Adapter a = new Adapter();
                    a.count++;
                    dict.put(s, a);
                }
                // Add adapter to the result array
                res.add(dict.get(s));
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
                dict.remove(getWordByAdapter(adapter));
            }
        }
    }

    public String getWordByAdapter(Adapter adapter) {
        for (Map.Entry<String, Adapter> i : dict.entrySet()) {
            if (adapter.equals(i.getValue())) {
                return i.getKey();
            }
        }
        return null;
    }

    public void printDict() {
        Set<String> keys = dict.keySet();
        for (String key : keys) {
            System.out.println(key + " : " + dict.get(key).count);
        }
    }
}
