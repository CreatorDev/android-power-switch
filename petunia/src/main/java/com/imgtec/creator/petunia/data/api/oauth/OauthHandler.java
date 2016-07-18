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

package com.imgtec.creator.petunia.data.api.oauth;


import com.google.gson.GsonBuilder;
import com.imgtec.creator.petunia.data.api.pojo.OauthToken;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class OauthHandler {

  OkHttpClient client;

  @Inject OauthHandler(OkHttpClient client) {
    super();
    this.client = client;
  }

  public OauthToken authorize(HttpUrl url) throws IOException, OauthException {

    Request request = new Request.Builder()
        .url(url)
        .addHeader("Accept", "application/vnd.imgtec.com.oauthtoken+json")
        .post(new FormBody.Builder()
            .addEncoded("grant_type", "password")
            .addEncoded("username", "6NsmHTPtwuyoFazyH272ivmWEzXdMWCkVAxsxxfskJFEI3-8u7GFwiSAkCTAOXgSc7WNDDwMFRzJM7qu1vsedg")
            .addEncoded("password", "KQH2VVjrrYKb-ER6pbwws2qs7t6kIgMZGQsJfy1wIybd_so0jtUT7C6-aPcxlgX56CU9G4uVEhP15Amg_JVq_A")
            .build()) //
        .build();
    okhttp3.Response response = client.newCall(request).execute();
    if (response.isSuccessful()) {

      OauthToken t = new GsonBuilder()
          .create()
          .fromJson(response.body().string(), OauthToken.class);
      return t;
    }
    throw new OauthException("Authorization failed!");
  }
}
