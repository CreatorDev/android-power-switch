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

package com.imgtec.creator.petunia.data.api.accountserver;

import android.net.Uri;

import java.util.UUID;

/**
 *
 */
public class IdConfig {


  private final String url;
  private final String clientId;
  private final String scope;
  private final Uri redirectUri;
  private final String state;
  private final String responseType;

  public IdConfig(String url, String client_id, String scope, Uri redirectUri, String state,
                  String response_type) {
    super();
    this.url = url;
    this.clientId = client_id;
    this.scope = scope;
    this.redirectUri = redirectUri;
    this.state = state;
    this.responseType = response_type;
  }

  public Uri getOauthUri() {

    final String nonce = UUID.randomUUID().toString();

    return Uri.parse(getUrl() + "?" +
        "client_id=" + getClientId() + "&" +
        "scope=" + getScope() + "&" +
        "redirect_uri=" + getRedirectUri() + "&" +
        "state=" + getState() + "&" +
        "nonce=" + nonce + "&" +
        "response_type=" + getResponseType());
  }

  public String getUrl() {
    return url;
  }

  public String getClientId() {
    return clientId;
  }

  public String getScope() {
    return scope;
  }

  public Uri getRedirectUri() {
    return redirectUri;
  }

  public String getState() {
    return state;
  }

  public String getResponseType() {
    return responseType;
  }
}
