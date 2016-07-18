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
import android.support.annotation.NonNull;

import com.imgtec.creator.petunia.data.api.ApiCallback;
import com.imgtec.creator.petunia.data.api.pojo.AccessKeys;
import com.imgtec.creator.petunia.data.api.pojo.Api;
import com.imgtec.creator.petunia.data.api.pojo.Developer;
import com.imgtec.creator.petunia.data.api.pojo.OauthToken;
import com.imgtec.creator.petunia.data.api.pojo.UserCreatedResponse;
import com.imgtec.creator.petunia.data.api.pojo.UserData;
import com.imgtec.creator.petunia.data.api.requests.GetRequest;
import com.imgtec.creator.petunia.data.api.requests.OauthRequest;
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
  private final OauthTokenWrapper tokenWrapper;

  private final ExecutorService executorService;

  public AccountServerApiServiceImpl(Context appContext,
                                     HttpUrl accountServer,
                                     OkHttpClient client,
                                     OauthTokenWrapper tokenWrapper,
                                     ExecutorService executorService) {
    super();
    this.appContext = appContext;
    this.accountServerUrl = accountServer;
    this.client = client;
    this.tokenWrapper = tokenWrapper;
    this.executorService = executorService;
  }

  @Override
  public void createAccount(@NonNull final String username,
                            @NonNull final String password,
                            @NonNull final String email,
                            @NonNull final ApiCallback<AccountServerApiService, UserCreatedResponse> callback) {

    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          Api api = new GetRequest<Api>(accountServerUrl.toString()).execute(client, Api.class);

          UserData data = new UserData();
          data.setUsername(username);
          data.setPassword(password);
          data.setEmail(email);

          UserCreatedResponse ucr =
              new UserCreatedResponseRequest(api.getLinkByRel("developers").getHref(), data)
                  .execute(client, UserCreatedResponse.class);

          callback.onSuccess(AccountServerApiServiceImpl.this, ucr);
        }
        catch (Exception e) {
          callback.onFailure(AccountServerApiServiceImpl.this, e);
        }
      }
    });
  }

  @Override
  public void login(@NonNull final String username,
                    @NonNull final String password,
                    @NonNull final ApiCallback<AccountServerApiService, AccessKeys> callback) {
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          Api api = new GetRequest<Api>(accountServerUrl.toString()).execute(client, Api.class);

          OauthToken oauthToken = new OauthRequest(api.getLinkByRel("authenticate").getHref(),
              username, password).execute(client, OauthToken.class);

          //this token will be used by oauth interceptor
          tokenWrapper.setAuthToken(oauthToken);

          api = new GetRequest<Api>(accountServerUrl.toString()).execute(client, Api.class);

          Developer developer = new GetRequest<Developer>(api.getLinkByRel("developer").getHref())
              .execute(client, Developer.class);

          AccessKeys accessKeys = new GetRequest<AccessKeys>(developer.getLinkByRel("accesskeys").getHref())
              .execute(client, AccessKeys.class);

          callback.onSuccess(AccountServerApiServiceImpl.this, accessKeys);
        }
        catch (Exception e) {
          callback.onFailure(AccountServerApiServiceImpl.this, e);
        }
      }
    });
  }
}
