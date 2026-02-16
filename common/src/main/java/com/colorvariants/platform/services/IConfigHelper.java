package com.colorvariants.platform.services;

public interface IConfigHelper {

    boolean isCacheEnabled();

    int getMaxCacheSize();

    boolean isAsyncGenerationEnabled();

}
