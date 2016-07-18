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

package com.imgtec.creator.petunia.data.api.deviceserver;

import com.imgtec.creator.petunia.data.api.ApiCallback;
import com.imgtec.creator.petunia.data.api.pojo.Client;
import com.imgtec.creator.petunia.data.api.pojo.Clients;
import com.imgtec.creator.petunia.data.api.pojo.Instances;
import com.imgtec.creator.petunia.data.api.pojo.OauthToken;
import com.imgtec.creator.petunia.data.api.pojo.ObjectType;
import com.imgtec.creator.petunia.data.api.pojo.ObjectTypes;
import com.imgtec.creator.petunia.data.api.pojo.RelayState;

import java.io.IOException;
import java.util.List;

/**
 *
 */
public interface DeviceServerApiService {

  void login(final String key, final String secret,
             ApiCallback<DeviceServerApiService, OauthToken> callback);


  interface Filter<T> {
    boolean accept(T filter);
  }

  Clients getClients(Filter<Client> filter) throws IOException;
  ObjectTypes getObjectTypes(Client client, Filter<ObjectType> filter) throws IOException;
  Instances<RelayState> getInstancesForObject(ObjectType obj) throws IOException;

  void putInstanceForObject(ObjectType objectType, int instanceId, boolean isOn) throws IOException;

  void requestClients(Filter<Client> filter, ApiCallback<DeviceServerApiService, Clients> callback);

  void requestObjectTypes(Client client, Filter<ObjectType> filter,
                          ApiCallback<DeviceServerApiService, ObjectTypes> apiCallback);
}
