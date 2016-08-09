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

import com.imgtec.creator.petunia.data.api.pojo.Api;
import com.imgtec.creator.petunia.data.api.pojo.OauthToken;
import com.imgtec.creator.petunia.data.api.requests.GetRequest;
import com.imgtec.creator.petunia.data.api.requests.OauthRequest;
import com.imgtec.creator.petunia.data.api.requests.RefreshTokenRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 *
 */
public class OauthManager implements Authenticator {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final HttpUrl url;
  private final OauthTokenWrapper token;

  public OauthManager(HttpUrl url, OauthTokenWrapper token) {
    super();
    this.url = url;
    this.token = token;
  }

  @Override
  public Request authenticate(Route route, Response response) throws IOException {

    logger.debug("Performing authentication...");
    if (responseCount(response) >= 3) {
      logger.debug("Max attempt number reached, skipping");
      return null;
    }

    if (authorize()) {

      return response.request().newBuilder()
          .removeHeader("Authorization")
          .addHeader("Authorization", String.format("%s %s", token.getAuthToken().getTokenType(),
              token.getAuthToken().getAccessToken()))
          .build();
    }

    return null;
  }

  private boolean authorize() {
    try {
      OkHttpClient client = new OkHttpClient.Builder().build();

      authorize(client, getOauthToken().getRefreshToken());
      return true;
    }
    catch (final Exception e) {
      logger.warn("Authorization failed!",e);
    }

    return false;
  }

  private int responseCount(Response response) {
    int result = 1;
    while ((response = response.priorResponse()) != null) {
      result++;
    }
    logger.debug("Attempt number: {}", result);
    return result;
  }

  public final void authorize(OkHttpClient client, String key, String secret) throws IOException {

    Api api = new GetRequest<Api>(url.toString()).execute(client, Api.class);

    OauthToken oauthToken = new OauthRequest(api.getLinkByRel("authenticate").getHref(),
        key, secret).execute(client, OauthToken.class);

    //this token will be used by oauth interceptor
    synchronized (token) {
      token.setAuthToken(oauthToken);
    }
  }

  public final void authorize(OkHttpClient client, String refreshToken) throws IOException {

    Api api = new GetRequest<Api>(url.toString()).execute(client, Api.class);

    OauthToken oauthToken = new RefreshTokenRequest(api.getLinkByRel("authenticate").getHref(),
        refreshToken).execute(client, OauthToken.class);

    //this token will be used by oauth interceptor
    synchronized (token) {
      token.setAuthToken(oauthToken);
    }
  }

  public final OauthToken getOauthToken() {
    synchronized (token) {
      return token.getAuthToken();
    }
  }
}
