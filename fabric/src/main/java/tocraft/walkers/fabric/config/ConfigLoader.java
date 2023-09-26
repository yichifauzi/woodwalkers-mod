package tocraft.walkers.fabric.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.architectury.platform.Platform;

public class ConfigLoader {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static WalkersFabricConfig read() {
		Path configFolder = Platform.getConfigFolder();
		Path configFile = Paths.get(configFolder.toString(), "walkers.json");

		// Write & return a new config file if it does not exist.
		if (!Files.exists(configFile)) {
			WalkersFabricConfig config = new WalkersFabricConfig();
			writeConfigFile(configFile, config);
			return config;
		} else {
			try {
				WalkersFabricConfig newConfig = GSON.fromJson(Files.readString(configFile), WalkersFabricConfig.class);

				// At this point, the config has been read, but there is a chance the config
				// class has new values which are not in the file.
				// We simply re-save the config file to save these values (because they will be
				// filled in at this point).
				writeConfigFile(configFile, newConfig);

				return newConfig;
			} catch (IOException exception) {
				exception.printStackTrace();
			}
		}

		// We should not get to this spot... might want to log something?
		// TODO: log
		return new WalkersFabricConfig();
	}

	private static void writeConfigFile(Path file, WalkersFabricConfig config) {
		try {
			if (!Files.exists(file)) {
				Files.createFile(file);
			}

			Files.writeString(file, GSON.toJson(config));
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}
}
