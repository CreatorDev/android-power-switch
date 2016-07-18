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
