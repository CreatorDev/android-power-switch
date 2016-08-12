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

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.reflect.TypeToken;
import com.imgtec.creator.petunia.data.Preferences;
import com.imgtec.creator.petunia.data.api.ApiCallback;
import com.imgtec.creator.petunia.data.api.oauth.OauthManager;
import com.imgtec.creator.petunia.data.api.pojo.AccessKey;
import com.imgtec.creator.petunia.data.api.pojo.Api;
import com.imgtec.creator.petunia.data.api.pojo.Client;
import com.imgtec.creator.petunia.data.api.pojo.Clients;
import com.imgtec.creator.petunia.data.api.pojo.Instances;
import com.imgtec.creator.petunia.data.api.pojo.OauthToken;
import com.imgtec.creator.petunia.data.api.pojo.ObjectType;
import com.imgtec.creator.petunia.data.api.pojo.ObjectTypes;
import com.imgtec.creator.petunia.data.api.pojo.RelayState;
import com.imgtec.creator.petunia.data.api.requests.ClientsRequest;
import com.imgtec.creator.petunia.data.api.requests.GetRequest;
import com.imgtec.creator.petunia.data.api.requests.InstancesRequest;
import com.imgtec.creator.petunia.data.api.requests.PutInstancesRequest;
import com.imgtec.creator.petunia.data.api.requests.RefreshTokenRequest;
import com.imgtec.creator.petunia.data.api.requests.TokenIdRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 *
 */
public class DeviceServerApiServiceImpl implements DeviceServerApiService {

  final Context appContext;
  final HttpUrl url;
  final OkHttpClient client;
  final OauthManager oauthManager;
  final ExecutorService executorService;
  private final Preferences preferences;

  public DeviceServerApiServiceImpl(Context appContext,
                                    HttpUrl url,
                                    OkHttpClient client,
                                    OauthManager oauthManager,
                                    ExecutorService executorService,
                                    Preferences preferences) {
    super();
    this.appContext = appContext;
    this.url = url;
    this.client = client;
    this.oauthManager = oauthManager;
    this.executorService = executorService;
    this.preferences = preferences;
  }

  @Override
  public void login(@NonNull final String key, @NonNull final String secret, final boolean rememberMe,
                    @NonNull final ApiCallback<DeviceServerApiService, OauthToken> callback) {
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          oauthManager.authorize(client, key, secret);

          //store settings and notify
          preferences.setKeepMeLoggedIn(rememberMe);
          preferences.setRefreshToken(oauthManager.getOauthToken().getRefreshToken());

          callback.onSuccess(DeviceServerApiServiceImpl.this, oauthManager.getOauthToken());
        }
        catch (Exception e) {
          callback.onFailure(DeviceServerApiServiceImpl.this, e);
        }
      }
    });
  }

  @Override
  public void login(final String refreshToken, final ApiCallback<DeviceServerApiService, OauthToken> callback) {
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          oauthManager.authorize(client, refreshToken);
          callback.onSuccess(DeviceServerApiServiceImpl.this, oauthManager.getOauthToken());
        }
        catch (Exception e) {
          callback.onFailure(DeviceServerApiServiceImpl.this, e);
        }
      }
    });
  }

  @Override
  public final Clients getClients(Filter<Client> filter) throws IOException {

    Api api = new GetRequest<Api>(url.toString()).execute(client, Api.class);

    //Get clients count
    Clients clients = new ClientsRequest(api.getLinkByRel("clients").getHref())
        .execute(client, Clients.class);

    int clientsCount = clients.getPageInfo().getTotalCount();
    clients = new ClientsRequest(api.getLinkByRel("clients").getHref(), clientsCount)
        .execute(client, Clients.class);


    if (filter != null) {
      List<Client> list = new ArrayList<>();
      for (Client client: clients.getItems()) {
        if (filter.accept(client)) {
          list.add(client);
        }
      }
      clients.setItems(list);
    }

    return clients;
  }

  @Override
  public void requestClients(final Filter<Client> filter,
                             final ApiCallback<DeviceServerApiService, Clients> callback) {
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          Clients clients = getClients(filter);
          callback.onSuccess(DeviceServerApiServiceImpl.this, clients);
        }
        catch (Exception e) {
          callback.onFailure(DeviceServerApiServiceImpl.this, e);
        }
      }
    });
  }

  @Override
  public void requestObjectTypes(final Client client,
                                 final Filter<ObjectType> filter,
                                 final ApiCallback<DeviceServerApiService, ObjectTypes> callback) {
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          ObjectTypes objectTypes = getObjectTypes(client, filter);

          callback.onSuccess(DeviceServerApiServiceImpl.this, objectTypes);
        } catch (Exception e) {
          callback.onFailure(DeviceServerApiServiceImpl.this, e);
        }
      }
    });
  }

  @Override
  public final ObjectTypes getObjectTypes(Client client, Filter<ObjectType> filter) throws IOException {

    ObjectTypes objectTypes = new GetRequest<ObjectTypes>(client.getLinkByRel("objecttypes").getHref())
        .execute(DeviceServerApiServiceImpl.this.client, ObjectTypes.class);

    if (filter != null) {
      List<ObjectType> list = new ArrayList<>();
      for (ObjectType obj: objectTypes.getItems()) {
        if (filter.accept(obj)) {
          list.add(obj);
        }
      }
      objectTypes.setItems(list);
    }
    return objectTypes;
  }

  @Override
  public Instances<RelayState> getInstancesForObject(ObjectType obj) throws IOException {
    Instances<RelayState> instances = new InstancesRequest<RelayState>(obj.getLinkByRel("instances").getHref())
        .execute(client, new TypeToken<Instances<RelayState>>(){});
    return instances;
  }

  @Override
  public void putInstanceForObject(ObjectType obj, int instanceId, boolean isOn) throws IOException {
    new PutInstancesRequest(obj.getLinkByRel("instances").getHref(),
        instanceId, isOn)
        .execute(client, new TypeToken<Instances<RelayState>>(){});
  }
}
