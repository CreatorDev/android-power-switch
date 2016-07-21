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

import com.google.gson.GsonBuilder;
import com.imgtec.creator.petunia.data.api.pojo.RelayState;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 *
 */
public class PutInstancesRequest extends InstancesRequest<RelayState> {

  private final int instanceId;
  private final boolean isOn;

  public PutInstancesRequest(String url, int instanceId, boolean isOn) {
    super(url);
    this.instanceId = instanceId;
    this.isOn = isOn;
  }

  @Override
  public Request prepareRequest() {

    RelayState newState = new RelayState();
    newState.setOn(isOn);
    HttpUrl url = HttpUrl.parse(getUrl());

    Request.Builder builder = new Request.Builder();
    builder.url(url
        .newBuilder()
        .addEncodedPathSegment(String.valueOf(instanceId))
        .build())
        .put(RequestBody.create(
              MediaType.parse("application/json; charset=utf-8"),
              new GsonBuilder().create().toJson(newState)))
        .build();

    return builder.build();
  }
}
