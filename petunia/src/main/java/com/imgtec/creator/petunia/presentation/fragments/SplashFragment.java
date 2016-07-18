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

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.imgtec.creator.petunia.R;
import com.imgtec.creator.petunia.data.Preferences;
import com.imgtec.creator.petunia.presentation.ActivityComponent;
import com.imgtec.di.HasComponent;

import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.OnClick;

/**
 *
 */
public class SplashFragment extends BaseFragment {

  private static final long DELAY = 3 * 1000;
  @Inject @Named("Main") Handler handler;
  @Inject Preferences preferences;
  @Inject Logger logger;

  @BindView(R.id.settings) ImageButton settings;
  @BindView(R.id.retry) Button retry;
  @BindView(R.id.error_msg) TextView errorMsg;

  boolean started = false;

  public static Fragment newInstance() {
    return new SplashFragment();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    //No call for super.
  }

  @Override
  protected void setComponent() {
    ((HasComponent<ActivityComponent>) getActivity()).getComponent().inject(this);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_splash_screen, container, false);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    hideActionBar();

    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        if (getActivity() != null) {
          if (!started) {
            //we shouldn't commit transaction after onStop
            logger.warn("Fragment is in STOPPED state, skipping.");
            return;
          }

          FragmentHelper.replaceFragmentAndClearBackStack(
              getActivity().getSupportFragmentManager(),
              LoginFragment.newInstance());

        }
      }
    }, DELAY);
  }

  @Override
  public void onStart() {
    super.onStart();
    started = true;
  }

  @Override
  public void onStop() {
    started = false;
    super.onStop();
  }

  private void hideActionBar() {

    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    if (actionBar == null) {
      return;
    }
    actionBar.hide();
  }

  @OnClick(R.id.retry)
  void onRetry() {
    retry.setVisibility(View.GONE);
    errorMsg.setVisibility(View.GONE);
    settings.setVisibility(View.GONE);
  }

  @TargetApi(21)
  @OnClick(R.id.settings)
  void onSettings() {
    Transition transition;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      transition = new Slide();
    } else {
      transition = null;
    }
    //TODO: show settings screen...
  }
}
