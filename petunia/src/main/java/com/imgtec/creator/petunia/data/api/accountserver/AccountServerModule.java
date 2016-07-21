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

package com.imgtec.creator.petunia.data.api.accountserver;

import android.content.Context;

import com.imgtec.creator.petunia.app.App;
import com.imgtec.creator.petunia.data.api.ApiModule;
import com.imgtec.creator.petunia.data.api.oauth.OauthInterceptor;
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
public class AccountServerModule {

  @Provides @PerApp @Named("AccountServer")
  OauthTokenWrapper provideOauthTokenWrapper() {
    return new OauthTokenWrapper();
  }

  @Provides @PerApp @Named("AccountServer")
  OauthInterceptor provideOauthInterceptor(@Named("AccountServer") OauthTokenWrapper tokenWrapper) {
    return new OauthInterceptor(tokenWrapper);
  }

  @Provides @PerApp @Named("AccountServer")
  OkHttpClient provideAccountServerOkHttpClient(App app, Cache cache,
                                                @Named("AccountServer") OauthInterceptor oauthInterceptor) {

    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

    OkHttpClient okHttpClient = new OkHttpClient
        .Builder()
        .cache(cache)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(oauthInterceptor)
        .build();
    return okHttpClient;
  }

  @Provides @PerApp @Named("AccountServer")
  HttpUrl provideAccountServerUrl() {
    return HttpUrl.parse("https://developeraccounts.flowcloud.systems");
  }

  @Provides @PerApp
  AccountServerApiService provideAccountServerApiService(Context appContext,
                                                         @Named("AccountServer") HttpUrl url,
                                                         @Named("AccountServer") OkHttpClient client,
                                                         @Named("AccountServer") OauthTokenWrapper tokenWrapper) {
    return new AccountServerApiServiceImpl(appContext,
                                           url,
                                           client,
                                           tokenWrapper,
                                           Executors.newSingleThreadExecutor());
  }
}
