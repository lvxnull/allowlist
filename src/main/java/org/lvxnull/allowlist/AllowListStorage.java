package org.lvxnull.allowlist;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

public final class AllowListStorage implements AutoCloseable, Iterable<String> {
    private static final Pattern nameRegex = Pattern.compile("^\\w{3,16}$");
    private Set<String> allowed = new LinkedHashSet<>();
    private final File listFile;

    public AllowListStorage(Path configRoot) throws IOException {
        var ignored = configRoot.toFile().mkdirs();
        listFile = configRoot.resolve("allowlist.txt").toFile();
        if(!listFile.exists() && !listFile.createNewFile()) {
            // TODO: Throw a better exception here
            throw new IOException("Cannot create allowlist file");
        }
    }

    private Set<String> load(Set<String> set) throws IOException {
        //noinspection ResultOfMethodCallIgnored
        listFile.createNewFile();
        try(var reader = new BufferedReader(new FileReader(listFile))) {
            int nth = 1;
            var line = reader.readLine();

            while(line != null) {
                line = line.trim();
                if(nameRegex.matcher(line).matches()) {
                    set.add(line);
                } else {
                    AllowList.LOGGER.warn("Line {}: Ignoring invalid name '{}'", nth, line);
                }
                line = reader.readLine();
                ++nth;
            }
        }

        return set;
    }

    public void load() throws IOException {
        load(allowed);
    }

    public void reload() throws IOException {
        allowed = load(new LinkedHashSet<>());
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
