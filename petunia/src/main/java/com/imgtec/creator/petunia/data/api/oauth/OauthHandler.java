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
