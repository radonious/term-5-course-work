package com.course.term5cw.Model;

import com.course.term5cw.Common.Operation;
import com.course.term5cw.Common.OperationUnit;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.Chunk;
import com.github.difflib.patch.DeltaType;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Dictionary implements Serializable {
    // Map: Dictionary of words and it's adapters (flyweights)
    private static final HashMap<String, Adapter> dict = new HashMap<>();

    // LinkedList: to see order of versions and iterate through them in both ways
    // Pair: to save name of version and version
    // Array list: for remove and insert operations
    private final LinkedList<ArrayList<OperationUnit>> versions = new LinkedList<>();

    // Version offset from latest version (-2 same as 2 version before)
    private Integer versionOffset = 0;

    // Current text version as array of lexemes to compare it with new version without restore
    private LinkedList<String> currentTextVersion = new LinkedList<>();

    // Get word string from dictionary by it adapter
    private static String getWordByAdapter(Adapter adapter) {
        for (Map.Entry<String, Adapter> i : dict.entrySet()) {
            if (adapter.equals(i.getValue())) {
                return i.getKey();
            }
        }
        return null;
    }

    // Get words array from adapters array
    private static LinkedList<String> getWordsByAdapters(ArrayList<Adapter> arr) {
        LinkedList<String> res = new LinkedList<>();
        for (Adapter adapter : arr) {
            res.add(getWordByAdapter(adapter));
        }
        return res;
    }

    // Get current number of available versions
    public Integer getVersionsCount() {
        return versions.size();
    }

    // Get index of current version (starts from 0)
    public Integer getCurrentVersionIndex() {
        return versions.size() - 1 + versionOffset;
    }

    // Increase counter in adapter
    private void plusCounter(String word) {
        if (dict.get(word) != null) {
            dict.get(word).plus();
        } else {
            Adapter a = new Adapter();
            a.plus();
            dict.put(word, a);
        }
    }

    // Decrease counter in adapter
    private void minusCounter(String word) {
        if (dict.get(word) != null) {
            dict.get(word).minus();
        }
        if (dict.get(word).getCount() == 0) {
            dict.remove(word);
        }
    }

    // Generate ArrayList of OperationUnits from delta array from module
    private ArrayList<OperationUnit> makeOperationsArray(List<AbstractDelta<String>> deltas, LinkedList<String> fileTextVersion) {
        ArrayList<OperationUnit> res = new ArrayList<>();
        for (AbstractDelta<String> delta : deltas) {
            ArrayList<Adapter> tmp = new ArrayList<>();
            // Data from delta
            DeltaType type = delta.getType();
            Chunk<String> source = delta.getSource();
            Chunk<String> target = delta.getTarget();
            // Process every operation type
            if (type == DeltaType.INSERT) {
                for (String word : target.getLines()) {
                    tmp.add(dict.get(word));
                }
                res.add(new OperationUnit(source.getPosition(), target.getPosition(), Operation.INSERT, tmp));
            } else if (type == DeltaType.DELETE) {
                for (String word : source.getLines()) {
                    tmp.add(dict.get(word));
                }
                res.add(new OperationUnit(source.getPosition(), target.getPosition(), Operation.REMOVE, tmp));
            } else if (type == DeltaType.CHANGE) {
                for (String word : source.getLines()) {
                    tmp.add(dict.get(word));
                }
                res.add(new OperationUnit(source.getPosition(), target.getPosition(), Operation.REMOVE, tmp));
                tmp = new ArrayList<>();
                for (String word : target.getLines()) {
                    tmp.add(dict.get(word));
                }
                res.add(new OperationUnit(source.getPosition(), target.getPosition(), Operation.INSERT, tmp));
            }
        }
        return res;
    }

    // Add new version to versions list
    public void addVersion(File file) throws Exception {
        // Iterate to latest version to add a new one
        while (versionOffset < 0) {
            nextVersion();
        }
        // Read whole file, split text by lexemes and convert to List.
        LinkedList<String> fileTextVersion = new LinkedList<>();
        Scanner in = new Scanner(file, StandardCharsets.UTF_8);
        while (in.hasNextLine()) {
            if (!fileTextVersion.isEmpty()) fileTextVersion.add("\\n");
            fileTextVersion.addAll(List.of(in.nextLine().trim().split(
                    "(?<=[!\"#$%&()*+-,./:;<=>?@^_`{|}~])|(?=[!\"#$%&()*+-,./:;<=>?@^_`{|}~])| "
            )));
        }

        // Update dictionary with new version of text
        fileTextVersion.forEach(this::plusCounter);

        // Contains positions and operation types and words to transform source text into current text
        List<AbstractDelta<String>> deltas = DiffUtils.diff(currentTextVersion, fileTextVersion).getDeltas();

        // Transform deltas (Change/Inset/Remove/Update) to operationUnits (only Remove/Insert) and add to versions list
        versions.add(makeOperationsArray(deltas, fileTextVersion));

        // NOTE: Lighter version of deltas (no positions)
        // List<DiffRow> diffs = DiffRowGenerator.create().build().generateDiffRows(source, current);

        // Update text version and offset
        currentTextVersion = fileTextVersion;
        versionOffset = 0;

        printDict(); // DEBUG
    }

    // Remove current version from versions list (delete all versions after current)
    public void removeVersion() {
        // Save current offset
        int offset = versionOffset;
        // Restore latest version
        while (versionOffset < 0) {
            nextVersion();
        }
        // Remove all version from latest to current offset
        for (int i = 0; i < Math.abs(offset); ++i) {
            currentTextVersion.forEach(this::minusCounter); // Decrease words count in dictionary
            prevVersion(); // Go one version earlier
            versions.removeLast(); // Remove last version
            versionOffset++;
        }
        // After loop, we need to remove current version
        currentTextVersion.forEach(this::minusCounter);
        if (versions.size() > 1) {
            prevVersion(); // If it's not only/first version, then go to previous one
            versionOffset++;
            versions.removeLast();
        } else {
            versions.clear();
            versionOffset = 0;
            currentTextVersion.clear();
        }

        printDict(); // DEBUG
    }

//    public void printDict() {
//        System.out.println("Dictionary:");
//        Set<String> keys = dict.keySet();
//        for (String key : keys) {
//            System.out.println(key + " : " + dict.get(key).getCount());
//        }
//    }

    // Print full dictionary map
    public void printDict() {
        System.out.println("Dictionary:");
        Set<String> keys = dict.keySet();
        for (String key : keys) {
            System.out.println(key + " : " + dict.get(key));
        }
        System.out.println("Versions:");
        System.out.println(versions.size());
        versions.forEach(e -> e.forEach(f -> System.out.println(f.getAdapters())));
        System.out.println(versionOffset);
        System.out.println(currentTextVersion.toString());
    }

    // Get sorted operations list for next version
    private ArrayList<OperationUnit> getNextOperations() {
        ArrayList<OperationUnit> operations = versions.get(versions.size() - 1 + versionOffset);
        // Sort by index in source text
        operations.sort((o1, o2) -> {
            if (o1.getSourcePosition() < o2.getSourcePosition()) {
                return 1;
            } else if (o1.getSourcePosition() > o2.getSourcePosition()) {
                return -1;
            } else {
                if (o2.getType() == o1.getType()) {
                    return 0;
                } else if (o2.getType() == Operation.REMOVE) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return operations;
    }

    // Process operation to restore next version
    public void nextVersion() {
        versionOffset++;
        for (OperationUnit operation : getNextOperations()) {
            if (operation.getType() == Operation.INSERT) {
                if (operation.getSourcePosition() >= currentTextVersion.size()) {
                    currentTextVersion.addAll(getWordsByAdapters(operation.getAdapters()));
                } else {
                    currentTextVersion.addAll(operation.getSourcePosition(), getWordsByAdapters(operation.getAdapters()));
                }
            } else if (operation.getType() == Operation.REMOVE) {
                if (operation.getSourcePosition() >= currentTextVersion.size()) {
                    operation.getAdapters().forEach(e -> currentTextVersion.removeLast());
                } else {
                    operation.getAdapters().forEach(e -> currentTextVersion.remove(operation.getSourcePosition()));
                }
            }
        }
    }

    // Get sorted operations list for previous version
    private ArrayList<OperationUnit> getPrevOperations() {
        ArrayList<OperationUnit> operations = versions.get(versions.size() - 1 + versionOffset);
        // Sort by index in target text
        operations.sort((o1, o2) -> {
            if (o1.getTargetPosition() < o2.getTargetPosition()) {
                return 1;
            } else if (o1.getTargetPosition() > o2.getTargetPosition()) {
                return -1;
            } else {
                if (o2.getType() == o1.getType()) {
                    return 0;
                } else if (o2.getType() == Operation.INSERT) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        return operations;
    }

    //  Process operation to restore previous version
    public void prevVersion() {
        for (OperationUnit operation : getPrevOperations()) {
            if (operation.getType() == Operation.INSERT) {
                if (operation.getTargetPosition() >= currentTextVersion.size()) {
                    operation.getAdapters().forEach(e -> currentTextVersion.removeLast());
                } else {
                    operation.getAdapters().forEach(e -> currentTextVersion.remove(operation.getTargetPosition()));
                }
            } else if (operation.getType() == Operation.REMOVE) {
                if (operation.getTargetPosition() >= currentTextVersion.size()) {
                    currentTextVersion.addAll(getWordsByAdapters(operation.getAdapters()));
                } else {
                    currentTextVersion.addAll(operation.getTargetPosition(), getWordsByAdapters(operation.getAdapters()));
                }
            }
        }
        versionOffset--;
    }

    // Compile full text into String from List
    public String getText() {
        StringBuffer buffer = new StringBuffer();
        for (String str : currentTextVersion) {
            if (str.equals("\\n")) {
                buffer.append("\n");
            } else {
                buffer.append(str);
                buffer.append(' ');
            }
        }
        return buffer.toString();
    }

    // Get json text for current versions list
    public String generateJSON() throws IOException {
        if (!versions.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
            return mapper.writeValueAsString(versions);
        } else {
            return "";
        }
    }

    // Load versions list from json file
    public void loadJSON(File f) throws IOException {
        versions.clear();
        currentTextVersion.clear();
        versionOffset = 0;

        ObjectMapper mapper = new ObjectMapper();
        JsonNode list = mapper.readTree(f);
        for (JsonNode array : list) {
            ArrayList<OperationUnit> version = new ArrayList<>();
            for (JsonNode unit : array) {
                String type = unit.get("type").asText();
                int source_pos = unit.get("source_pos").asInt();
                int target_pos = unit.get("target_pos").asInt();
                JsonNode words = unit.get("words");
                ArrayList<Adapter> tmp = new ArrayList<>();
                for (JsonNode word : words) {
                    plusCounter(word.asText());
                    tmp.add(dict.get(word.asText()));
                }
                version.add(new OperationUnit(source_pos, target_pos, Operation.valueOf(type), tmp));
            }
            versions.add(version);
            versionOffset--;
            nextVersion();
        }
    }

    public void syncAdaptersBeforeSerialization() {
        for (ArrayList<OperationUnit> version : versions) {
            for (OperationUnit unit : version) {
                LinkedList<String> words = getWordsByAdapters(unit.getAdapters());
                unit.setAdapters_words(words);
            }
        }
    }

    public void syncAdaptersAfterSerialization() {
        dict.clear();
        for (ArrayList<OperationUnit> version : versions) {
            for (OperationUnit unit : version) {
                for (int i = 0; i < unit.getAdapters().size(); ++i) {
                    String word = unit.getAdapters_words().get(i);
                    plusCounter(word);
                    unit.getAdapters().set(i, dict.get(word));
                }
            }
        }
    }

    // Custom jackson serializer for OperationUnit array
    public static class CustomOperationUnitSerializer extends StdSerializer<OperationUnit> {

        public CustomOperationUnitSerializer() {
            this(null);
        }

        public CustomOperationUnitSerializer(Class<OperationUnit> a) {
            super(a);
        }

        @Override
        public void serialize(OperationUnit value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeObjectField("type", value.getType());
            gen.writeObjectField("source_pos", value.getSourcePosition());
            gen.writeObjectField("target_pos", value.getTargetPosition());
            gen.writeObjectField("words", getWordsByAdapters(value.getAdapters()));
            gen.writeEndObject();
        }
    }
}
