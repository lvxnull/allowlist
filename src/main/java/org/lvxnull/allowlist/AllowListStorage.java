package org.lvxnull.allowlist;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

public final class AllowListStorage implements AutoCloseable, Iterable<String> {
    private static final Pattern nameRegex = Pattern.compile("^\\w{3,16}$");
    private final Set<String> allowed = new LinkedHashSet<>();
    private final File listFile;
    private boolean loaded = false;

    public AllowListStorage(Path configRoot) {
        listFile = configRoot.resolve("allowlist.txt").toFile();
    }

    public void load() throws IOException {
        if(loaded) return;
        //noinspection ResultOfMethodCallIgnored
        listFile.createNewFile();
        try(var reader = new BufferedReader(new FileReader(listFile))) {
            int nth = 1;
            var line = reader.readLine();

            while(line != null) {
                line = line.trim();
                try {
                    add(line);
                } catch(IllegalArgumentException e) {
                    AllowList.LOGGER.warn("Line {}: Ignoring invalid name '{}'", nth, line);
                }
                line = reader.readLine();
                ++nth;
            }
        }

        loaded = true;
    }

    public void save() throws IOException {
        try(var writer = new BufferedWriter(new FileWriter(listFile))) {
            for(var s: this) {
                writer.write(s);
                writer.newLine();
            }
        }
    }

    @Override
    public void close() throws IOException {
        save();
    }

    public boolean contains(String name) {
        return allowed.contains(name);
    }

    public boolean add(String name) {
        if(!nameRegex.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid player name.");
        }
        return allowed.add(name);
    }

    public boolean remove(String name) {
        return allowed.remove(name);
    }

    public int size() {
        return allowed.size();
    }

    @NotNull
    @Override
    public Iterator<String> iterator() {
        return allowed.iterator();
    }

    public void clear() {
        allowed.clear();
    }
}
