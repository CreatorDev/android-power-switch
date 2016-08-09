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

/**
 *
 */
public interface DeviceServerApiService {

  void login(final String key, final String secret,
             ApiCallback<DeviceServerApiService, OauthToken> callback);

  void login(final String refreshToken, ApiCallback<DeviceServerApiService, OauthToken> callback);


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
