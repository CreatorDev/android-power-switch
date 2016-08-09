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

package com.imgtec.creator.petunia.data;

import android.content.SharedPreferences;

import javax.inject.Inject;

/**
 *
 */
public class Preferences {

  //credentials
  private static final String EMPTY = "";
  private static final String USERNAME = "username";
  private static final String DEFAULT_USERNAME = EMPTY;
  private static final String PASSWORD = "password";
  private static final String DEFAULT_PASSWORD = EMPTY;
  private static final String EMAIL = "email";
  private static final String DEFAULT_EMAIL = EMPTY;

  //OAuth
  private static final String OAUTH_REFRESH_TOKEN = "oauth_refresh_token";
  private static final String OAUTH_DEFAULT_REFRESH_TOKEN = EMPTY;

  private final SharedPreferences sharedPreferences;

  @Inject
  Preferences(SharedPreferences prefs) {
    this.sharedPreferences = prefs;
  }

  public Credentials getCredentials() {
    final String username = sharedPreferences.getString(USERNAME, DEFAULT_USERNAME);
    final String password = sharedPreferences.getString(PASSWORD, DEFAULT_PASSWORD);
    final String email = sharedPreferences.getString(EMAIL, DEFAULT_EMAIL);
    return new Credentials(username, password, email);
  }

  public void setCredentials(final Credentials credentials) {
    setCredentials(credentials.getUsername(), credentials.getPassword(), credentials.getEmail());
  }

  public void resetCredentials() {
    setCredentials("", "", "");
  }

  private void setCredentials(String username, String password, String email) {
    final SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(USERNAME, username);
    editor.putString(PASSWORD, password);
    editor.putString(EMAIL, email);
    editor.commit();
  }

  public void setRefreshToken(String refreshToken) {
    final SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(OAUTH_REFRESH_TOKEN, refreshToken);
    editor.commit();
  }

  public String getRefreshToken() {
    return sharedPreferences.getString(OAUTH_REFRESH_TOKEN, OAUTH_DEFAULT_REFRESH_TOKEN);
  }

  public void resetRefreshToken() {
    setRefreshToken("");
  }
}
