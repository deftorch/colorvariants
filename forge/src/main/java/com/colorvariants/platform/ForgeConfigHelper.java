package com.colorvariants.platform;

import com.colorvariants.platform.services.IConfigHelper;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class ForgeConfigHelper implements IConfigHelper {

    public static final ForgeConfigSpec CONFIG_SPEC;
    public static final Config CONFIG;

    static {
        final Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config::new);
        CONFIG_SPEC = specPair.getRight();
        CONFIG = specPair.getLeft();
    }

    public static class Config {
        public final ForgeConfigSpec.BooleanValue cacheEnabled;
        public final ForgeConfigSpec.IntValue maxCacheSize;
        public final ForgeConfigSpec.BooleanValue asyncGeneration;

        public Config(ForgeConfigSpec.Builder builder) {
            builder.push("general");
            cacheEnabled = builder
                    .comment("Enable texture caching")
                    .define("cacheEnabled", true);
            maxCacheSize = builder
                    .comment("Maximum cache size")
                    .defineInRange("maxCacheSize", 1000, 1, 5000);
            asyncGeneration = builder
                    .comment("Enable async texture generation")
                    .define("asyncGeneration", true);
            builder.pop();
        }
    }

    @Override
    public boolean isCacheEnabled() {
        return CONFIG.cacheEnabled.get();
    }

    @Override
    public int getMaxCacheSize() {
        return CONFIG.maxCacheSize.get();
    }

    @Override
    public boolean isAsyncGenerationEnabled() {
        return CONFIG.asyncGeneration.get();
    }
}
