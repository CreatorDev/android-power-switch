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

package com.imgtec.creator.petunia.presentation;


import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.imgtec.creator.petunia.R;
import com.imgtec.creator.petunia.data.Preferences;
import com.imgtec.creator.petunia.presentation.fragments.AboutFragment;
import com.imgtec.creator.petunia.presentation.fragments.ChooseDeviceFragment;
import com.imgtec.creator.petunia.presentation.fragments.FragmentHelper;
import com.imgtec.creator.petunia.presentation.fragments.LoginFragment;
import com.imgtec.creator.petunia.presentation.fragments.RelayToggleFragment;
import com.imgtec.creator.petunia.presentation.utils.DrawerHelper;
import com.imgtec.creator.petunia.presentation.utils.ToolbarHelper;
import com.imgtec.di.HasComponent;

import org.slf4j.Logger;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.R.id.toggle;

/**
 *
 */
public class MainActivity extends BaseActivity implements HasComponent<ActivityComponent>,
    NavigationView.OnNavigationItemSelectedListener {

  private ActivityComponent component;

  @BindView(R.id.app_bar) AppBarLayout appBar;
  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.drawer_layout) DrawerLayout drawer;
  @BindView(R.id.nav_view) NavigationView navigationView;

  Fragment currentFragment;

  @Inject Logger logger;

  @Inject DrawerHelper drawerHelper;
  @Inject ToolbarHelper toolbarHelper;
  @Inject Preferences preferences;

  ActionBarDrawerToggle toggle;
  Unbinder unbinder;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    unbinder = ButterKnife.bind(this);

    setSupportActionBar(toolbar);
    toolbarHelper.setToolbar(toolbar);
    setupNavigationDrawer();

    if (savedInstanceState == null) {
      showFragmentWithClearBackstack(LoginFragment.newInstance());
    }
  }

  private void setupNavigationDrawer() {
    toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
        R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
    };
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    navigationView.setNavigationItemSelectedListener(this);
    navigationView.getMenu().getItem(0).setChecked(true);
    drawerHelper.setDrawer(drawer);
    drawerHelper.setDrawerToggle(toggle);
  }

  @Override
  protected void setComponent() {
    component = ActivityComponent.Initializer.init(this);
    component.inject(this);
  }

  @Override
  public ActivityComponent getComponent() {
    return component;
  }

  @Override
  public void onAttachFragment(Fragment fragment) {
    super.onAttachFragment(fragment);
    currentFragment = fragment;
  }

  @Override
  protected void onDestroy() {
    drawer.removeDrawerListener(toggle);
    unbinder.unbind();
    toolbarHelper = null;
    super.onDestroy();
  }


  @Override
  public void onBackPressed() {
    final FragmentManager fm = getSupportFragmentManager();
    if (fm.getBackStackEntryCount() > 0) {
      fm.popBackStack();
    }
    else {
      super.onBackPressed();
    }
    currentFragment = null;
  }

  private void showFragmentWithClearBackstack(Fragment f) {
    currentFragment = f;
    FragmentHelper.replaceFragmentAndClearBackStack(getSupportFragmentManager(), currentFragment);
  }

  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    int id = item.getItemId();
    item.setChecked(true);

    switch (id) {
      case R.id.choose_device: {
        showFragmentWithClearBackstack(ChooseDeviceFragment.newInstance());
        break;
      }
      case R.id.about: {
        showFragmentWithClearBackstack(AboutFragment.newInstance());
        break;
      }
      case R.id.logout: {

      }
      default:
        break;
    }
    drawer.closeDrawer(GravityCompat.START);
    return false;
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    toggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    toggle.onConfigurationChanged(newConfig);
  }
}
