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

package com.imgtec.creator.petunia.presentation.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.imgtec.creator.petunia.R;
import com.imgtec.creator.petunia.data.RelayDevice;
import com.imgtec.creator.petunia.presentation.fragments.ItemSwitchListener;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 *
 */
public class RelaysAdapter extends BaseAdapter<RelayDevice, RelaysAdapter.RelayViewHolder>{

  private ItemSwitchListener itemSwitchListener;

  public RelaysAdapter(@NonNull ItemSwitchListener listener) {
    super();
    itemSwitchListener = listener;
  }

  @Override
  public RelayViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.relay_list_item, parent, false);
    return new RelayViewHolder(v);
  }

  @Override
  public void onBindViewHolder(final RelayViewHolder holder, int position) {
    final RelayDevice device = data.get(position);
    holder.relayName.setText(device.getDeviceName());
    holder.relayDesc.setText(String.format("%s(%s)",device.getDeviceID(), device.getInstanceId()));
    holder.aSwitch.setChecked(device.isOn());
    holder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.isPressed()) {
          itemSwitchListener.onSwitch(holder.itemView, holder.getAdapterPosition(), isChecked);
        }
      }
    });
  }

  public void updateItem(RelayDevice result) {
    int pos = data.indexOf(result);
    if (pos > -1) {
      data.set(pos, result);
      notifyItemChanged(pos);
    }
  }

  static class RelayViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.relay_name) TextView relayName;
    @BindView(R.id.relay_desc) TextView relayDesc;
    @BindView(R.id.relay_switch) Switch aSwitch;

    public RelayViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }
}
