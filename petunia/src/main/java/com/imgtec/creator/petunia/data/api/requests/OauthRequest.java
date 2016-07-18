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

package com.imgtec.creator.petunia.data.api.requests;

import com.imgtec.creator.petunia.data.api.pojo.OauthToken;

import okhttp3.FormBody;
import okhttp3.Request;

/**
 *
 */
public class OauthRequest extends BaseRequest<OauthToken> {

  private final String username;
  private final String password;

  public OauthRequest(String url, String username, String password) {
    super(url);
    this.username = username;
    this.password = password;
  }

  @Override
  public Request prepareRequest() {
    return new Request.Builder()
        .url(getUrl())
        .addHeader("Accept", "application/vnd.imgtec.com.oauthtoken+json")
        .post(new FormBody.Builder()
            .addEncoded("grant_type", "password")
            .addEncoded("username", username)
            .addEncoded("password", password)
            .build()) //
        .build();
  }
}
