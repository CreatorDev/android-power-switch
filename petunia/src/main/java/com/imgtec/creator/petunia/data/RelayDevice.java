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

import com.imgtec.creator.petunia.data.api.pojo.ObjectType;
import com.imgtec.creator.petunia.data.api.pojo.RelayState;

/**
 *
 */
public class RelayDevice {

  private final ObjectType obj;
  private final int instanceId;
  private final RelayState state;

  public RelayDevice(ObjectType obj, int instanceId, RelayState state) {
    this.obj = obj;
    this.instanceId = instanceId;
    this.state = state;
  }

  public String getDeviceName() {
    return "Relay";
  }

  public String getDeviceID() {
    return obj.getObjectTypeID();
  }

  public boolean isOn() {
    return state.isOn();
  }

  public ObjectType getObjectType() {
    return obj;
  }

  public int getInstanceId() {
    return instanceId;
  }

  public RelayState getState() {
    return state;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RelayDevice)) return false;

    RelayDevice that = (RelayDevice) o;

    if (instanceId != that.instanceId) return false;
    return obj != null ? obj.equals(that.obj) : that.obj == null;

  }

  @Override
  public int hashCode() {
    int result = obj != null ? obj.hashCode() : 0;
    result = 31 * result + instanceId;
    return result;
  }

  public static String getObjectTypeID() {
    return "3201";
  }
}
