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

package com.imgtec.creator.petunia.data.api.deviceserver;

import android.content.Context;

import com.imgtec.creator.petunia.app.App;
import com.imgtec.creator.petunia.data.Preferences;
import com.imgtec.creator.petunia.data.api.ApiModule;
import com.imgtec.creator.petunia.data.api.oauth.OauthInterceptor;
import com.imgtec.creator.petunia.data.api.oauth.OauthManager;
import com.imgtec.creator.petunia.data.api.oauth.OauthTokenWrapper;
import com.imgtec.di.PerApp;

import java.util.concurrent.Executors;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

@Module(
    includes = {
        ApiModule.class
    }
)
public class DeviceServerModule {

  @Provides @PerApp @Named("DeviceServer")
  OauthTokenWrapper provideOauthTokenWrapper() {
    return new OauthTokenWrapper();
  }

  @Provides @PerApp @Named("DeviceServer")
  OauthInterceptor provideOauthInterceptor(@Named("DeviceServer") OauthTokenWrapper tokenWrapper) {
    return new OauthInterceptor(tokenWrapper);
  }

  @Provides @PerApp @Named("DeviceServer")
  OauthManager provideOauthManager(@Named("DeviceServer") HttpUrl url,
                                   @Named("DeviceServer")  OauthTokenWrapper token,
                                   Preferences prefs) {
    return new OauthManager(url, token, prefs);
  }

  @Provides @PerApp @Named("DeviceServer")
  OkHttpClient provideOkHttpClient(App app,
                                   Cache cache,
                                   @Named("DeviceServer") OauthInterceptor oauthInterceptor,
                                   @Named("DeviceServer") OauthManager oauthManager) {

    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

    OkHttpClient okHttpClient = new OkHttpClient
        .Builder()
        .cache(cache)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(oauthInterceptor)
        .authenticator(oauthManager)
        .build();
    return okHttpClient;
  }

  @Provides @PerApp @Named("DeviceServer")
  HttpUrl provideBaseUrl() {
    return HttpUrl.parse("https://deviceserver.flowcloud.systems");
  }

  @Provides @PerApp
  DeviceServerApiService provideDeviceServerApiService(Context appContext,
                                                       @Named("DeviceServer") HttpUrl url,
                                                       @Named("DeviceServer") OkHttpClient client,
                                                       @Named("DeviceServer") OauthManager oauthManager) {
    return new DeviceServerApiServiceImpl(appContext,
                                          url,
                                          client,
                                          oauthManager,
                                          Executors.newSingleThreadExecutor());
  }

}
