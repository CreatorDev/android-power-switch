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

import com.imgtec.creator.petunia.data.Preferences;
import com.imgtec.creator.petunia.data.api.pojo.AccessKey;
import com.imgtec.creator.petunia.data.api.pojo.Api;
import com.imgtec.creator.petunia.data.api.pojo.OauthToken;
import com.imgtec.creator.petunia.data.api.requests.GetRequest;
import com.imgtec.creator.petunia.data.api.requests.OauthRequest;

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
  private final Preferences prefs;

  public OauthManager(HttpUrl url, OauthTokenWrapper token, Preferences prefs) {
    super();
    this.url = url;
    this.token = token;
    this.prefs = prefs;
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
      AccessKey ak = prefs.getAccessKey();
      authorize(client, ak.getKey(), ak.getSecret());
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

  public final OauthToken getOauthToken() {
    synchronized (token) {
      return token.getAuthToken();
    }
  }
}
