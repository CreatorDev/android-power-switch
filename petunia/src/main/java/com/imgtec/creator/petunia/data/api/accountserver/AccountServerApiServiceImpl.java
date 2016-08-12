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
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.imgtec.creator.petunia.data.api.ApiCallback;
import com.imgtec.creator.petunia.data.api.deviceserver.DeviceServerApiServiceImpl;
import com.imgtec.creator.petunia.data.api.pojo.AccessKey;
import com.imgtec.creator.petunia.data.api.pojo.AccessKeys;
import com.imgtec.creator.petunia.data.api.pojo.Api;
import com.imgtec.creator.petunia.data.api.pojo.Developer;
import com.imgtec.creator.petunia.data.api.pojo.OauthToken;
import com.imgtec.creator.petunia.data.api.pojo.UserCreatedResponse;
import com.imgtec.creator.petunia.data.api.pojo.UserData;
import com.imgtec.creator.petunia.data.api.requests.GetRequest;
import com.imgtec.creator.petunia.data.api.requests.OauthRequest;
import com.imgtec.creator.petunia.data.api.requests.TokenIdRequest;
import com.imgtec.creator.petunia.data.api.requests.UserCreatedResponseRequest;
import com.imgtec.creator.petunia.data.api.oauth.OauthTokenWrapper;

import java.util.concurrent.ExecutorService;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 *
 */
public class AccountServerApiServiceImpl implements AccountServerApiService {

  private final Context appContext;
  private final HttpUrl accountServerUrl;
  private final OkHttpClient client;
  private final ExecutorService executorService;
  private final IdConfig config;

  public AccountServerApiServiceImpl(Context appContext,
                                     HttpUrl accountServer,
                                     OkHttpClient client,
                                     ExecutorService executorService,
                                     IdConfig config) {
    super();
    this.appContext = appContext;
    this.accountServerUrl = accountServer;
    this.client = client;
    this.executorService = executorService;
    this.config = config;
  }

  @Override
  public void loginOrSignup(Context context) {
    Intent browserIntent = new Intent(Intent.ACTION_VIEW, config.getOauthUri());
    browserIntent.setFlags(browserIntent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
    context.startActivity(browserIntent);
  }

  @Override
  public void loginWithIdToken(final String idToken,
                               final ApiCallback<AccountServerApiService, AccessKey> callback) {
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {

          AccessKey keys = new TokenIdRequest(accountServerUrl.toString(),
              idToken).execute(client, AccessKey.class);

          callback.onSuccess(AccountServerApiServiceImpl.this, keys);
        }
        catch (Exception e) {
          callback.onFailure(AccountServerApiServiceImpl.this, e);
        }
      }
    });
  }
}
