package com.colorvariants.platform;

import com.colorvariants.platform.services.IConfigHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FabricConfigHelper implements IConfigHelper {

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("colorvariants.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final ConfigData data;

    public FabricConfigHelper() {
        this.data = loadConfig();
    }

    private ConfigData loadConfig() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                return GSON.fromJson(Files.newBufferedReader(CONFIG_PATH), ConfigData.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ConfigData defaultData = new ConfigData();
        saveConfig(defaultData);
        return defaultData;
    }

    private void saveConfig(ConfigData data) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isCacheEnabled() {
        return data.cacheEnabled;
    }

    @Override
    public int getMaxCacheSize() {
        return data.maxCacheSize;
    }

    @Override
    public boolean isAsyncGenerationEnabled() {
        return data.asyncGeneration;
    }

    private static class ConfigData {
        boolean cacheEnabled = true;
        int maxCacheSize = 1000;
        boolean asyncGeneration = true;
    }
}
