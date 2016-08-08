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

package com.imgtec.creator.petunia.presentation.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
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

import java.lang.ref.WeakReference;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

public class ChooseDeviceFragment extends BaseFragment {

  @Inject DataService dataService;
  @Inject ToolbarHelper toolbarHelper;

  @BindView(R.id.gateways)
  RecyclerView recyclerView;

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
            FragmentHelper.replaceFragmentAndClearBackStack(
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
    toolbarHelper.hideProgress();
    super.onPause();
  }

  private void showRetryDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
    builder
        .setMessage(R.string.no_device_found)
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
          }
        })
    .setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        loadGateways();
        dialog.dismiss();
      }
    });

    builder.create().show();
  }

  private static class GatewayRequestor implements DataService.DataCallback<List<Gateway>> {

    private WeakReference<ChooseDeviceFragment> fragment;

    public GatewayRequestor(ChooseDeviceFragment fragment) {
      super();
      this.fragment = new WeakReference<>(fragment);
    }

    @Override
    public void onSuccess(final DataService service, final List<Gateway> result) {
      final ChooseDeviceFragment f = fragment.get();
      if (f != null && f.getActivity() != null) {
        f.getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {

            f.toolbarHelper.hideProgress();
            if (result.size() == 0) {
              f.showRetryDialog();
              return;
            }
            f.adapter.addAll(result);
            f.adapter.notifyDataSetChanged();
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
