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

import com.imgtec.creator.petunia.data.api.pojo.AccessKey;

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

  //Access key
  private static final String AK_NAME = "ak_name";
  private static final String AK_DEFAULT_NAME = EMPTY;
  private static final String AK_KEY = "ak_key";
  private static final String AK_DEFAULT_KEY = EMPTY;
  private static final String AK_SECRET = "ak_secret";
  private static final String AK_DEFAULT_SECRET = EMPTY;

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

  public void setAccessKey(AccessKey ak) {
    setAccessKey(ak.getName(), ak.getKey(), ak.getSecret());
  }

  public AccessKey getAccessKey() {
    AccessKey ak = new AccessKey();
    ak.setName(sharedPreferences.getString(AK_NAME, AK_DEFAULT_NAME));
    ak.setKey(sharedPreferences.getString(AK_KEY, AK_DEFAULT_KEY));
    ak.setSecret(sharedPreferences.getString(AK_SECRET, AK_DEFAULT_SECRET));
    return ak;
  }

  public void resetAccessKey() {
    setAccessKey("", "", "");
  }

  private void setAccessKey(String name, String key, String secret) {
    final SharedPreferences.Editor editor = sharedPreferences.edit();
    editor.putString(AK_NAME, name);
    editor.putString(AK_KEY, key);
    editor.putString(AK_SECRET, secret);
    editor.commit();
  }
}
