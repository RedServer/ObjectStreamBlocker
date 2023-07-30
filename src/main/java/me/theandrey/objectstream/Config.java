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
	public static final Set<String> excludeMethods = new HashSet<>();

	public static void load(File file) {
		Configuration config = new Configuration(file);
		config.load();

		String[] classNames = config.getStringList("ExcludeClass", "general", new String[0],
				"List of full class name for which the inheritance fix will not apply");
		String[] methodList = config.getStringList("ExcludeMethods", "general", new String[0],
				"List of methods in the format 'class_name#method_name' to which the fix will not apply.");

		excludeClass.clear();
		excludeClass.addAll(filterStringList(classNames));
		excludeMethods.clear();
		excludeMethods.addAll(filterStringList(methodList));

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
