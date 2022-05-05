/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package com.huawei.kitsample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.account.service.AccountAuthService;

public class AuthWithCode extends AppCompatActivity {
	private AccountAuthService mAuthService;

	private AccountAuthParams mAuthParam;

	private static final int REQUEST_CODE_SIGN_IN = 1000;


	private static final String TAG = "Account";
	private TextView logTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_huawei_code);
		findViewById(R.id.HuaweiIdAuthButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				silentSignInByHwId();
			}
		});
		findViewById(R.id.HuaweiIdSignOutButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				signOut();
			}
		});

		findViewById(R.id.HuaweiIdCancelAuthButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				cancelAuthorization();
			}
		});

		logTextView = (TextView) findViewById(R.id.LogText);
	}



	private void silentSignInByHwId() {
		mAuthParam = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
				.setEmail()
				.setAuthorizationCode()
				.createParams();


		mAuthService = AccountAuthManager.getService(this, mAuthParam);


		Task<AuthAccount> task = mAuthService.silentSignIn();
		task.addOnSuccessListener(new OnSuccessListener<AuthAccount>() {
			@Override
			public void onSuccess(AuthAccount authAccount) {

				dealWithResultOfSignIn(authAccount);
			}
		});
		task.addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(Exception e) {

				if (e instanceof ApiException) {
					ApiException apiException = (ApiException) e;
					Intent signInIntent = mAuthService.getSignInIntent();
					startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN);
				}
			}
		});
	}

	/**
	 * 处理返回的AuthAccount，获取帐号信息
	 * Process the returned AuthAccount and get account information
	 *
	 * @param authAccount AccountAccount对象，用于记录帐号信息(AccountAccount object, used to record account information)
	 */
	private void dealWithResultOfSignIn(AuthAccount authAccount) {
		showLog("code:" + authAccount.getAuthorizationCode());
		Log.i(TAG, "code:" + authAccount.getAuthorizationCode());
		//TODO 获取到Code信息后，应用需要发送给应用服务器
		//TODO After obtaining the Code information, the application needs to send it to the application server

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_SIGN_IN) {
			Log.i(TAG, "onActivitResult of sigInInIntent, request code: " + REQUEST_CODE_SIGN_IN);
			Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data);
			if (authAccountTask.isSuccessful()) {
				showLog("sign in success");
				// 登录成功，获取到登录帐号信息对象authAccount
				// The login is successful, and the login account information object authAccount is obtained
				AuthAccount authAccount = authAccountTask.getResult();
				dealWithResultOfSignIn(authAccount);
				Log.i(TAG, "onActivitResult of sigInInIntent, request code: " + REQUEST_CODE_SIGN_IN);
			} else {
				// 登录失败，status code标识了失败的原因，请参考API中的错误码参考了解详细错误原因
				// Login failed. The status code identifies the reason for the failure. Please refer to the error
				// code reference in the API for detailed error reasons.
				Log.e(TAG, "sign in failed : " + ((ApiException) authAccountTask.getException()).getStatusCode());
				showLog("sign in failed : " + ((ApiException) authAccountTask.getException()).getStatusCode());
			}
		}
	}

	private void signOut() {
		if (mAuthService == null) {
			return;
		}
		Task<Void> signOutTask = mAuthService.signOut();
		signOutTask.addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				Log.i(TAG, "signOut Success");
				showLog("signOut Success");
			}
		}).addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(Exception e) {
				Log.i(TAG, "signOut fail");
				showLog("signOut fail");
			}
		});
	}

	private void cancelAuthorization() {
		if (mAuthService == null) {
			return;
		}
		Task<Void> task = mAuthService.cancelAuthorization();
		task.addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void aVoid) {
				Log.i(TAG, "cancelAuthorization success");
				showLog("cancelAuthorization success");
			}
		});
		task.addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(Exception e) {
				Log.i(TAG, "cancelAuthorization failure：" + e.getClass().getSimpleName());
				showLog("cancelAuthorization failure：" + e.getClass().getSimpleName());
			}
		});
	}

	private void showLog(String log) {
		logTextView.setText("log:" + "\n" + log);
	}

}
