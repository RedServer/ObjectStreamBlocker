package me.theandrey.objectstream;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraftforge.common.config.Configuration;

public final class Config {

    public static final Set<String> excludeClass = new HashSet<>();

    public static void load(File file) {
        Configuration config = new Configuration(file);
        config.load();

        String[] classNames = config.getStringList("ExcludeClass", "general", new String[0],
            "Список полных названий классов для которых не будет применяться исправление");

        excludeClass.clear();
        excludeClass.addAll(filterStringList(classNames));

        if (config.hasChanged()) {
            config.save();
        }
    }

    private static List<String> filterStringList(String[] strings) {
        return Arrays.stream(strings)
            .map(String::trim)
            .distinct()
            .collect(Collectors.toList());
    }
}
