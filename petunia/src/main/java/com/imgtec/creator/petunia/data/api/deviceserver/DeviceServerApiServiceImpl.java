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

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.reflect.TypeToken;
import com.imgtec.creator.petunia.data.api.ApiCallback;
import com.imgtec.creator.petunia.data.api.oauth.OauthManager;
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

  public DeviceServerApiServiceImpl(Context appContext,
                                    HttpUrl url,
                                    OkHttpClient client,
                                    OauthManager oauthManager,
                                    ExecutorService executorService) {
    super();
    this.appContext = appContext;
    this.url = url;
    this.client = client;
    this.oauthManager = oauthManager;
    this.executorService = executorService;
  }

  @Override
  public void login(@NonNull final String key, @NonNull final String secret,
                    @NonNull final ApiCallback<DeviceServerApiService, OauthToken> callback) {
    executorService.execute(new Runnable() {
      @Override
      public void run() {
        try {
          oauthManager.authorize(client, key, secret);
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
