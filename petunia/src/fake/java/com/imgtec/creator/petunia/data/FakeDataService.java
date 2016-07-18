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

import com.imgtec.creator.petunia.data.api.pojo.Client;
import com.imgtec.creator.petunia.data.api.pojo.ObjectType;
import com.imgtec.creator.petunia.data.api.pojo.RelayState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FakeDataService implements DataService {

  private static final List<Gateway> GATEWAYS;
  private static final int RELAY_COUNT = 5;
  static {
    GATEWAYS = new ArrayList<>(2);
    GATEWAYS.add(new Gateway(new Client() {{setName("Test Gateway 1");}}));
    GATEWAYS.add(new Gateway(new Client() {{setName("Test Gateway 2");}}));
  }

  final Context context;
  final ScheduledExecutorService executor;
  final Handler mainHandler;
  private final Random random = new Random();

  private FakeDataService(Builder builder) {
    super();
    this.context = builder.getAppContext();
    this.executor = builder.getExecutor();
    this.mainHandler = builder.getMainHandler();
  }

  @Override
  public void requestGateways(final DataCallback<List<Gateway>> callback) {
    executor.schedule(new Runnable() {
      @Override
      public void run() {
        callback.onSuccess(FakeDataService.this, GATEWAYS);
      }
    }, 5, TimeUnit.SECONDS);
  }

  @Override
  public void requestRelays(final Gateway gateway, final DataCallback2<Gateway, List<RelayDevice>> callback) {
    executor.submit(new Runnable() {

      @Override
      public void run() {

        final List<RelayDevice> list = prepareRelayList(RELAY_COUNT);

        mainHandler.post(new Runnable() {
          @Override
          public void run() {
            callback.onSuccess(FakeDataService.this, gateway, list);
          }
        });
      }

      private List<RelayDevice> prepareRelayList(int relayCount) {
        List<RelayDevice> list = new ArrayList<RelayDevice>();

        for (int i = 0; i < relayCount; ++i) {
          final int j = i;
          list.add(new RelayDevice(new ObjectType(){{setObjectTypeID("" + j);}}, j,
              new RelayState(){{setOn(random.nextBoolean());}} ));
        }
        return list;
      }
    });
  }

  @Override
  public void changeRelayState(final RelayDevice device, final boolean isOn,
                               final DataCallback2<RelayDevice, Boolean> dataCallback) {
    mainHandler.post(new Runnable() {
      @Override
      public void run() {
        if (random.nextBoolean()) {
          dataCallback.onSuccess(FakeDataService.this, device, isOn);
        }
        else {
          dataCallback.onFailure(FakeDataService.this, device, null);
        }
      }
    });
  }

  @Override
  public void startPollingForRelayChanges(Gateway gateway, DataCallback2<Gateway, List<RelayDevice>> callback) {

  }

  @Override
  public void stopPolling() {

  }

  public static class Builder {

    private Context context;
    private ScheduledExecutorService executor;
    private Handler handler;

    public DataService build() {
      return new FakeDataService(this);
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

  }
}
