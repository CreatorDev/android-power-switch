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

package com.imgtec.creator.petunia.presentation.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.imgtec.creator.petunia.R;
import com.imgtec.creator.petunia.data.DataService;
import com.imgtec.creator.petunia.data.Gateway;
import com.imgtec.creator.petunia.data.RelayDevice;
import com.imgtec.creator.petunia.presentation.ActivityComponent;
import com.imgtec.creator.petunia.presentation.adapters.RelaysAdapter;
import com.imgtec.creator.petunia.presentation.views.HorizontalItemDecoration;
import com.imgtec.di.HasComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

/**
 *
 */
public class RelayToggleFragment extends BaseFragment implements ItemSwitchListener {


  private static final String GATEWAY = "GATEWAY";
  @Inject Logger logger;
  @Inject DataService dataService;

  @BindView(R.id.relays_recycler_view)
  RecyclerView recyclerView;

  private RelaysAdapter adapter;
  private Gateway gateway;

  public static RelayToggleFragment newInstance(Gateway gateway) {
    RelayToggleFragment fragment = new RelayToggleFragment();
    Bundle args = new Bundle();
    args.putString(GATEWAY, gateway.serialize(new GsonBuilder().create()));
    fragment.setArguments(args);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      fragment.setExitTransition(new Slide(Gravity.LEFT));
    }
    return fragment;
  }

  @Override
  protected void setComponent() {
    ((HasComponent<ActivityComponent>) getActivity()).getComponent().inject(this);
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (getArguments() == null || !getArguments().containsKey(GATEWAY)) {
      throw new IllegalArgumentException("Serialized gateway device missing!");
    }
    gateway = Gateway.deserialize(getArguments().getString(GATEWAY), new GsonBuilder().create());
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_relay_toggle, container, false);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    adapter = new RelaysAdapter(this);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    recyclerView.addItemDecoration(new HorizontalItemDecoration(getContext()));

    loadRelays();
  }

  private void loadRelays() {
    dataService.requestRelays(gateway, new RelaysCallback(this));
  }

  @Override
  public void onResume() {
    super.onResume();
    setupToolbar();
    dataService.startPollingForRelayChanges(gateway, new RelaysCallback(this));
  }

  @Override
  public void onPause() {
    dataService.stopPolling();
    super.onPause();
  }

  @Override
  public void onSwitch(View v, int position, final boolean isOn) {
    final RelayDevice device = adapter.getItem(position);
    dataService.changeRelayState(device, isOn, new RelaySwitchCallback(this));
  }

  private void setupToolbar() {
    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    if (actionBar == null) {
      return;
    }
    actionBar.show();
    actionBar.setTitle(R.string.relay_toggle_title);
    actionBar.setDisplayHomeAsUpEnabled(false);
    actionBar.setHomeButtonEnabled(true);
    actionBar.setDisplayUseLogoEnabled(true);

    setHasOptionsMenu(false);
  }

  private static class RelaysCallback implements DataService.DataCallback2<Gateway, List<RelayDevice>> {

    final WeakReference<RelayToggleFragment> fragment;

    public RelaysCallback(RelayToggleFragment fragment) {
      super();
      this.fragment = new WeakReference<>(fragment);
    }

    @Override
    public void onSuccess(DataService service, Gateway param, List<RelayDevice> result) {
      RelayToggleFragment f = fragment.get();
      if (f != null) {
        f.adapter.clear();
        f.adapter.addAll(result);
        f.adapter.notifyDataSetChanged();
      }
    }

    @Override
    public void onFailure(DataService service, Gateway param, Throwable t) {
      RelayToggleFragment f = fragment.get();
      if (f != null && f.getContext() != null) {

        Toast.makeText(f.getContext(),
            String.format("Requesting relays for gateway %s failed!", param.getName()),
            Toast.LENGTH_LONG).show();
      }
    }
  }

  private static class RelaySwitchCallback implements DataService.DataCallback2<RelayDevice, Boolean> {

    static final Logger logger = LoggerFactory.getLogger(RelaySwitchCallback.class);

    final WeakReference<RelayToggleFragment> fragment;

    public RelaySwitchCallback(RelayToggleFragment fragment) {
      this.fragment = new WeakReference<>(fragment);
    }

    @Override
    public void onSuccess(final DataService service, final RelayDevice param, Boolean result) {
      logger.debug("Changing RelayDevice state from {} to {}",
          param.getState().isOn(), result);

      param.getState().setOn(result);
      RelayToggleFragment f = fragment.get();
      if (f != null) {
        f.adapter.updateItem(param);
      }
    }

    @Override
    public void onFailure(DataService service, RelayDevice param, Throwable t) {
      logger.debug("Changing RelayDevice state (from {} to ...) failed!",
          param.getState().isOn(), t);
      RelayToggleFragment f = fragment.get();
      if (f != null) {
        f.adapter.updateItem(param);
      }
    }
  }

}
