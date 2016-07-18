/*
 * <b>Copyright 2015 by Imagination Technologies Limited
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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.imgtec.creator.petunia.R;
import com.imgtec.creator.petunia.data.DataService;
import com.imgtec.creator.petunia.data.Gateway;
import com.imgtec.creator.petunia.presentation.ActivityComponent;
import com.imgtec.creator.petunia.presentation.adapters.GatewaysAdapter;
import com.imgtec.creator.petunia.presentation.utils.ToolbarHelper;
import com.imgtec.creator.petunia.presentation.views.HorizontalItemDecoration;
import com.imgtec.creator.petunia.presentation.views.RecyclerItemClickSupport;
import com.imgtec.di.HasComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

public class ChooseDeviceFragment extends BaseFragment {

  @Inject DataService dataService;
  @Inject ToolbarHelper toolbarHelper;

  @BindView(R.id.gateways)
  RecyclerView recyclerView;

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private LinearLayoutManager layoutManager;
  private GatewaysAdapter adapter;

  public static ChooseDeviceFragment newInstance() {
    ChooseDeviceFragment fragment = new ChooseDeviceFragment();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      fragment.setExitTransition(new Slide(Gravity.LEFT));
    }
    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_choose_device, container, false);
  }


  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setupToolbar();

    final DefaultItemAnimator itemAnimator = new DefaultItemAnimator();
    itemAnimator.setAddDuration(200);
    layoutManager = new LinearLayoutManager(getActivity());
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.addItemDecoration(new HorizontalItemDecoration(getActivity()));
    recyclerView.setHasFixedSize(true);
    recyclerView.setItemAnimator(itemAnimator);

    adapter = new GatewaysAdapter();
    recyclerView.setAdapter(adapter);

    RecyclerItemClickSupport.addTo(recyclerView)
        .setOnItemClickListener(new RecyclerItemClickSupport.OnItemClickListener() {
          @Override
          public void onItemClicked(RecyclerView recyclerView, int position, View view) {
            Gateway gateway = adapter.getItem(position);
            logger.debug("Gateway: {} selected", gateway);
            FragmentHelper.replaceFragment(
                getActivity().getSupportFragmentManager(),
                RelayToggleFragment.newInstance(gateway));
          }
        });

    loadGateways();
  }

  private void loadGateways() {
    dataService.requestGateways(new GatewayRequestor(this));
  }

  @Override
  protected void setComponent() {
    ((HasComponent<ActivityComponent>) getActivity()).getComponent().inject(this);
  }

  private void setupToolbar() {
    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    if (actionBar == null) {
      return;
    }
    actionBar.show();
    actionBar.setTitle(R.string.choose_client);
    actionBar.setDisplayHomeAsUpEnabled(false);
    actionBar.setHomeButtonEnabled(true);
    setHasOptionsMenu(false);
  }

  @Override
  public void onResume() {
    super.onResume();
    toolbarHelper.showProgress();
  }

  @Override
  public void onPause() {
    super.onPause();
  }

  private static class GatewayRequestor implements DataService.DataCallback<List<Gateway>> {

    private WeakReference<ChooseDeviceFragment> fragment;

    public GatewayRequestor(ChooseDeviceFragment fragment) {
      super();
      this.fragment = new WeakReference<ChooseDeviceFragment>(fragment);
    }

    @Override
    public void onSuccess(final DataService service, final List<Gateway> result) {
      final ChooseDeviceFragment f = fragment.get();
      if (f != null && f.getActivity() != null) {
        f.getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            f.adapter.addAll(result);
            f.adapter.notifyDataSetChanged();

            f.toolbarHelper.hideProgress();
          }
        });
      }
    }

    @Override
    public void onFailure(final DataService service, final Throwable t) {
      final ChooseDeviceFragment f = fragment.get();
      if (f != null && f.getActivity() != null) {
        f.getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(f.getActivity(),
                "Requesting gateways failed! " + t.getMessage(),
                Toast.LENGTH_LONG).show();

            f.toolbarHelper.hideProgress();
          }
        });
      }
    }
  }
}
