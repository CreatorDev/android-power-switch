/*
 * <b>Copyright 2016 by Imagination Technologies Limited
 * and/or its affiliated group companies.</b>
 *
 * All rights reserved.  No part of this software, either
 * material or conceptual may be copied or distributed,
 * transmitted, transcribed, stored in a retrieval system
 * or translated into any human or computer language in any
 * form by any means, electronic, mechanical, manual or
 * other-wise, or disclosed to the third parties without the
 * express written permission of Imagination Technologies
 * Limited, Home Park Estate, Kings Langley, Hertfordshire,
 * WD4 8LZ, U.K.
 */

package com.imgtec.creator.petunia.data.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.imgtec.creator.petunia.app.App;
import com.imgtec.di.PerApp;

import java.io.File;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;

/**
 *
 */
@Module
public class ApiModule {

  private static final long CACHE_DISK_SIZE = 50 * 1024 * 1024;

  @Provides @PerApp
  Cache provideCache(App app) {
    File cacheDir = new File(app.getCacheDir(), "http");
    return new Cache(cacheDir, CACHE_DISK_SIZE);
  }

  @Provides @PerApp
  Gson provideGson() {
    return new GsonBuilder().create();
  }

}
