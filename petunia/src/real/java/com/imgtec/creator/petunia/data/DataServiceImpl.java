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

package com.imgtec.creator.petunia.data;

import android.content.Context;
import android.os.Handler;

import com.imgtec.creator.petunia.data.api.deviceserver.DeviceServerApiService;
import com.imgtec.creator.petunia.data.api.pojo.Client;
import com.imgtec.creator.petunia.data.api.pojo.Clients;
import com.imgtec.creator.petunia.data.api.pojo.Instances;
import com.imgtec.creator.petunia.data.api.pojo.ObjectType;
import com.imgtec.creator.petunia.data.api.pojo.ObjectTypes;
import com.imgtec.creator.petunia.data.api.pojo.RelayState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class DataServiceImpl implements DataService {

  final Logger logger = LoggerFactory.getLogger(getClass());
  final Context context;
  final ScheduledExecutorService executor;
  final Handler mainHandler;
  final DeviceServerApiService deviceServerApi;
  boolean isPolling;

  DataServiceImpl(Builder builder) {
    super();
    this.context = builder.getAppContext();
    this.executor = builder.getExecutor();
    this.mainHandler = builder.getMainHandler();
    this.deviceServerApi = builder.getDeviceServerApi();
    this.isPolling = false;
  }

  public void requestGateways(final DataCallback<List<Gateway>> callback) {

    executor.execute(new Runnable() {
      @Override
      public void run() {
        try {
          Clients result = deviceServerApi.getClients(new DeviceServerApiService.Filter<Client>() {
            @Override
            public boolean accept(Client client) {
              return client.getName().startsWith("RelayDevice");
            }
          });

          final List<Gateway> list = new ArrayList<>();
          for (Client client: result.getItems()) {
            list.add(new Gateway(client));
          }

          mainHandler.post(new Runnable() {
            @Override
            public void run() {
              callback.onSuccess(DataServiceImpl.this, list);
            }
          });

        }
        catch (final Exception e) {
          postFailure(callback, e);
        }
      }
    });
  }

  @Override
  public void requestRelays(final Gateway gateway, final DataCallback2<Gateway, List<RelayDevice>> callback) {

    executor.execute(new Runnable() {
      @Override
      public void run() {
        try {
          ObjectTypes result = deviceServerApi.getObjectTypes(gateway.getClient(),
              new DeviceServerApiService.Filter<ObjectType>() {

                @Override
                public boolean accept(ObjectType filter) {
                  return filter.getObjectTypeID().equals(RelayDevice.getObjectTypeID()); //IPSO
                }
              });

          final List<RelayDevice> devices = new ArrayList<>();

          for (ObjectType obj : result.getItems()) {  //should be one element
            Instances<RelayState> instances = deviceServerApi.getInstancesForObject(obj);
            for (int i = 0; i < instances.getItems().size(); ++i) {
              RelayState state = instances.getItems().get(i);
              devices.add(new RelayDevice(obj, i, state));
            }
          }

          mainHandler.post(new Runnable() {
            @Override
            public void run() {
              callback.onSuccess(DataServiceImpl.this, gateway, devices);
            }
          });
        }
        catch (final Exception e) {

          mainHandler.post(new Runnable() {
            @Override
            public void run() {
              callback.onFailure(DataServiceImpl.this, gateway, e);
            }
          });
        }
      }
    });

  }

  @Override
  public void changeRelayState(final RelayDevice device, final boolean isOn,
                               final DataCallback2<RelayDevice, Boolean> callback) {

    executor.execute(new Runnable() {
      @Override
      public void run() {
        try {

          deviceServerApi.putInstanceForObject(device.getObjectType(),
              device.getInstanceId(), isOn);



          mainHandler.post(new Runnable() {
            @Override
            public void run() {
              callback.onSuccess(DataServiceImpl.this, device, isOn);
            }
          });
        }
        catch (final Exception e) {
          mainHandler.post(new Runnable() {
          @Override
          public void run() {
            callback.onFailure(DataServiceImpl.this, device, e);
          }
        });
        }
      }
    });
  }

  @Override
  public void startPollingForRelayChanges(final Gateway gateway,
                                          final DataCallback2<Gateway, List<RelayDevice>> callback) {
    synchronized (this) {
      if (!isPolling) {
        isPolling = true;
        executor.schedule(new PollingTask(gateway, callback), 10, TimeUnit.SECONDS);
        logger.debug("-> Polling started");
      }
    }
  }

  @Override
  public void stopPolling() {
    synchronized (this) {
      if (isPolling) {
        isPolling = false;
        logger.debug("-> Polling stopped");
      }
    }
  }

  private <T> void postFailure(final DataCallback<T> callback, final Throwable t) {
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        callback.onFailure(DataServiceImpl.this, t);
      }
    });
  }

  class PollingTask implements Runnable {

    private final Gateway gateway;
    private final DataCallback2<Gateway, List<RelayDevice>> callback;

    public PollingTask( Gateway gateway, DataCallback2<Gateway, List<RelayDevice>> callback) {
      this.gateway = gateway;
      this.callback = callback;
    }

    @Override
    public void run() {
      logger.debug("Executing polling task");
      requestRelays(gateway, callback);
      if (isPolling) {
        executor.schedule(new PollingTask(gateway, callback), 10, TimeUnit.SECONDS);
      }
    }
  }

  public static class Builder {

    private Context context;
    private ScheduledExecutorService executor;
    private Handler handler;
    private DeviceServerApiService deviceServerApi;

    public DataService build() {
      return new DataServiceImpl(this);
    }

    public Builder setAppContext(Context context) {
      this.context = context;
      return this;
    }

    public Context getAppContext() {
      return context;
    }

    public Builder setExecutor(ScheduledExecutorService executor) {
      this.executor = executor;
      return this;
    }

    public ScheduledExecutorService getExecutor() {
      return executor;
    }

    public Builder setMainHandler(Handler handler) {
      this.handler = handler;
      return this;
    }

    public Handler getMainHandler() {
      return handler;
    }

    public Builder setDeviceServerApi(DeviceServerApiService deviceServerApi) {
      this.deviceServerApi = deviceServerApi;
      return this;
    }

    DeviceServerApiService getDeviceServerApi() {
      return deviceServerApi;
    }
  }
}
