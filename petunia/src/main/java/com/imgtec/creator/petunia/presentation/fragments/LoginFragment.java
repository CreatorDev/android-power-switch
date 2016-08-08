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


import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.transition.Slide;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.imgtec.creator.petunia.R;
import com.imgtec.creator.petunia.data.Credentials;
import com.imgtec.creator.petunia.data.Preferences;
import com.imgtec.creator.petunia.data.api.ApiCallback;
import com.imgtec.creator.petunia.data.api.accountserver.AccountServerApiService;
import com.imgtec.creator.petunia.data.api.deviceserver.DeviceServerApiService;
import com.imgtec.creator.petunia.data.api.oauth.OauthTokenWrapper;
import com.imgtec.creator.petunia.data.api.pojo.AccessKey;
import com.imgtec.creator.petunia.data.api.pojo.AccessKeys;
import com.imgtec.creator.petunia.data.api.pojo.OauthToken;
import com.imgtec.creator.petunia.presentation.ActivityComponent;
import com.imgtec.creator.petunia.presentation.utils.DrawerHelper;
import com.imgtec.creator.petunia.presentation.views.ProgressButton;
import com.imgtec.di.HasComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.OnClick;

public class LoginFragment extends BaseFragment {

  private static final String STATE = "STATE";
  private Credentials credentials;

  enum LoginState {
    NONE, IN_PROGRESS, COMPLETED
  }

  @BindView(R.id.username_email_et) EditText usernameEmailET;
  @BindView(R.id.password_et) EditText passwordET;
  @BindView(R.id.login_btn) ProgressButton loginBtn;
  @BindView(R.id.link_tv) TextView linkTv;

  @Inject @Named("Main") Handler handler;
  @Inject @Named("DeviceServer") OauthTokenWrapper token;
  @Inject Preferences prefs;
  @Inject AccountServerApiService accountService;
  @Inject DeviceServerApiService deviceService;

  final Logger logger = LoggerFactory.getLogger(getClass());
  LoginState state = LoginState.NONE;

  public static LoginFragment newInstance() {
    LoginFragment fragment = new LoginFragment();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      fragment.setExitTransition(new Slide(Gravity.LEFT));
    }
    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_login, container, false);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    handleState(savedInstanceState);
    setupToolbar();
    setupCredentials();

    drawerHelper.lockDrawer();
    loginBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        performLogin();
      }
    });
    linkTv.setText(Html.fromHtml("<a href=\"#\">Visit us to learn more</a>"));
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putSerializable(STATE, state);
  }

  @Override
  protected void setComponent() {
    ((HasComponent<ActivityComponent>) getActivity()).getComponent().inject(this);
  }

  @UiThread
  private void performLogin() {
    final String username = usernameEmailET.getText().toString();
    final String password = passwordET.getText().toString();

    verifyWithCredentials(username, password);

    loginBtn.setProgress(true);
    final Credentials c = prefs.getCredentials();
    drawerHelper.updateHeader(c.getUsername(), c.getEmail());

    AccessKey ak = prefs.getAccessKey();
    if (ak.getKey().isEmpty()) {
      state = LoginState.IN_PROGRESS;
      accountService.login(username, password, new AccountServerLoginCallback(this, prefs, username, password));
    }
    else {
      deviceService.login(ak.getKey(), ak.getSecret(), new DeviceServerLoginCallback(this, prefs));
    }
  }

  @OnClick(R.id.link_tv)
  void openLink() {
    Uri uri = Uri.parse("http://beta.creator.io");
    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    startActivity(intent);
  }

  private void verifyWithCredentials(String username, String password) {
    if (credentials.getUsername().equals(username) && credentials.getPassword().equals(password))
      return;

    //if username & password differ, clear all
    prefs.resetCredentials();
    prefs.resetAccessKey();
  }

  private void handleState(Bundle savedInstanceState) {
    if (savedInstanceState != null) {
      state = (LoginState) savedInstanceState.get(STATE);
    }

    loginBtn.setProgress(state != LoginState.NONE);
  }

  private void setupToolbar() {
    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
    if (actionBar == null) {
      return;
    }
    actionBar.hide();
  }

  private void setupCredentials() {
    credentials = prefs.getCredentials();
    usernameEmailET.setText(credentials.getUsername());
    passwordET.setText(credentials.getPassword());
  }

  private void notifyAccountLoginSuccessful(String key, String secret) {
    final Credentials c = prefs.getCredentials();
    drawerHelper.updateHeader(c.getUsername(), c.getEmail());

    deviceService.login(key, secret, new DeviceServerLoginCallback(this, prefs));
  }

  private void showToast(final String msg, final int duration) {
    if (getActivity() != null) {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Toast.makeText(getActivity(), msg, duration).show();
        }
      });
    }
  }

  private void updateLoginState(LoginState loginState) {
    this.state = loginState;
  }

  private void refreshLoginButton() {
    loginBtn.setProgress(state != LoginState.NONE);
  }

  private void requestFailed(String msg, Throwable t) {
    updateLoginState(LoginState.NONE);
    refreshLoginButton();
    showToast(msg + t.getMessage(), Toast.LENGTH_LONG);
  }

  static class AccountServerLoginCallback implements ApiCallback<AccountServerApiService,AccessKeys> {

    private final WeakReference<LoginFragment> fragment;
    private final Preferences preferences;
    private final String username;
    private final String password;

    public AccountServerLoginCallback(LoginFragment fragment, Preferences prefs,
                                      String username, String password) {
      super();
      this.fragment = new WeakReference<>(fragment);
      this.preferences = prefs;
      this.username = username;
      this.password = password;
    }

    @Override
    public void onSuccess(final AccountServerApiService service, final AccessKeys result) {

      AccessKey ak = result.getItems().get(0);
      preferences.setAccessKey(ak);
      preferences.setCredentials(new Credentials(username, password));

      LoginFragment f = fragment.get();
      if (f != null) {
        f.notifyAccountLoginSuccessful(ak.getKey(), ak.getSecret());
      }
    }

    @Override
    public void onFailure(final AccountServerApiService service, final Throwable t) {
      final LoginFragment f = fragment.get();
      if (f != null && f.getActivity() != null) {
        f.getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            f.requestFailed("Logging to account server failed! ", t);
          }
        });

      }
    }
  }

  static class DeviceServerLoginCallback implements ApiCallback<DeviceServerApiService,OauthToken> {

    final Logger logger = LoggerFactory.getLogger(DeviceServerLoginCallback.class);
    final WeakReference<LoginFragment> fragment;
    final Preferences preferences;

    public DeviceServerLoginCallback(LoginFragment fragment, Preferences prefs) {
      super();
      this.fragment = new WeakReference<>(fragment);
      this.preferences = prefs;
    }

    @Override
    public void onSuccess(DeviceServerApiService service, OauthToken result) {
      final LoginFragment f = fragment.get();
      if (f != null && f.getActivity() != null) {
        FragmentHelper.replaceFragmentAndClearBackStack(f.getActivity().getSupportFragmentManager(),
            ChooseDeviceFragment.newInstance());
      }
      else {
        logger.warn("DS login successfull, but activity is null! Skipping.");
      }
    }

    @Override
    public void onFailure(DeviceServerApiService service, final Throwable t) {
      final LoginFragment f = fragment.get();
      if (f != null && f.getActivity() != null) {
        f.getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            f.requestFailed("Log in to device server failed! ", t);
          }
        });
      }
    }
  }
}
