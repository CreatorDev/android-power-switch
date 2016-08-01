/*
 * <b>Copyright (c) 2016, Imagination Technologies Limited and/or its affiliated group companies
 *  and/or licensors. </b>
 *
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are permitted
 *  provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this list of conditions
 *      and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice, this list of
 *      conditions and the following disclaimer in the documentation and/or other materials provided
 *      with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors may be used to
 *      endorse or promote products derived from this software without specific prior written
 *      permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *  DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 *  WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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
