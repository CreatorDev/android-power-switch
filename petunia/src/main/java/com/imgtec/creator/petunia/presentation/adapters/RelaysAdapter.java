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
