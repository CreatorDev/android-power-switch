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

import com.imgtec.creator.petunia.data.api.pojo.Clients;

import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 *
 */
public class ClientsRequest extends BaseRequest<Clients> {

  private final int pageSize;

  public ClientsRequest(String url) {
    this(url, 0);
  }

  public ClientsRequest(String url, int pageSize) {
    super(url);
    this.pageSize = pageSize;
  }

  @Override
  public Request prepareRequest() {

    HttpUrl url = HttpUrl.parse(getUrl());

    Request.Builder builder = new Request.Builder();
    builder.url(url
                .newBuilder()
                .addQueryParameter("pageSize", String.valueOf(pageSize))
                .build())
        .addHeader("ContentType", "application/vnd.imgtec.com.clients+xml")
        .get();

    return builder.build();
  }
}
