package app.kabinka.social;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import app.kabinka.social.api.requests.accounts.GetOwnAccount;
import app.kabinka.social.api.requests.oauth.GetOauthToken;
import app.kabinka.social.api.session.AccountSessionManager;
import app.kabinka.social.model.Account;
import app.kabinka.social.model.Application;
import app.kabinka.social.model.Instance;
import app.kabinka.social.model.Token;
import app.kabinka.social.ui.utils.UiUtils;
import app.kabinka.social.utils.PostAuthNavigator;

import androidx.annotation.Nullable;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;

public class OAuthActivity extends Activity{
	private static final String TAG = "OAuth:Callback";
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState){
		UiUtils.setUserPreferredTheme(this);
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "OAuth callback received");
		
		Uri uri=getIntent().getData();
		if(uri==null || isTaskRoot()){
			Log.e(TAG, "Invalid callback: uri null or isTaskRoot");
			finish();
			return;
		}
		
		// Handle OAuth errors
		if(uri.getQueryParameter("error")!=null){
			String error=uri.getQueryParameter("error_description");
			if(TextUtils.isEmpty(error))
				error=uri.getQueryParameter("error");
			Log.e(TAG, "OAuth error: " + error);
			Toast.makeText(this, error, Toast.LENGTH_LONG).show();
			finish();
			restartMainActivity();
			return;
		}
		
		String code=uri.getQueryParameter("code");
		if(TextUtils.isEmpty(code)){
			Log.e(TAG, "No authorization code in callback");
			finish();
			return;
		}
		
		// Log code receipt (first 8 chars only for security)
		Log.d(TAG, "Authorization code received: " + code.substring(0, Math.min(8, code.length())) + "...");
		
		Instance instance=AccountSessionManager.getInstance().getAuthenticatingInstance();
		Application app=AccountSessionManager.getInstance().getAuthenticatingApp();
		if(instance==null || app==null){
			Log.e(TAG, "Missing authenticating instance or app - session lost");
			Toast.makeText(this, R.string.auth_session_lost, Toast.LENGTH_LONG).show();
			finish();
			restartMainActivity();
			return;
		}
		
		Log.d(TAG, "Pending login loaded: instance=" + instance.getDomain());
		
		ProgressDialog progress=new ProgressDialog(this);
		progress.setMessage(getString(R.string.finishing_auth));
		progress.setCancelable(false);
		progress.show();
		
		// Exchange authorization code for access token
		Log.d(TAG, "Exchanging code for token");
		new GetOauthToken(app.clientId, app.clientSecret, code, GetOauthToken.GrantType.AUTHORIZATION_CODE)
				.setCallback(new Callback<>(){
					@Override
					public void onSuccess(Token token){
						Log.d(TAG, "Token exchange successful, tokenType=" + token.tokenType);
						
						// Verify credentials to get account info
						Log.d(TAG, "Verifying credentials");
						new GetOwnAccount()
								.setCallback(new Callback<>(){
									@Override
									public void onSuccess(Account account){
										Log.d(TAG, "VerifyCredentials success: accountId=" + account.id + 
												", username=" + account.username);
										
										// Persist session atomically
										Log.d(TAG, "Persisting session to database");
										AccountSessionManager.getInstance().addAccount(instance, token, account, app, null);
										Log.d(TAG, "Session persisted successfully");
										
										// Explicitly set active account
										String accountId = account.id + "@" + instance.getDomain();
										AccountSessionManager.getInstance().setLastActiveAccountID(accountId);
										Log.d(TAG, "Active account set: " + accountId);
										
										progress.dismiss();
										finish();
										
										// Navigate to main activity
										Log.d(TAG, "Navigating to post-auth activity");
										Intent intent = PostAuthNavigator.createPostAuthIntent(
												OAuthActivity.this,
												Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
										);
										startActivity(intent);
									}

									@Override
									public void onError(ErrorResponse error){
									Log.e(TAG, "OAuth:Callback - VerifyCredentials failed");
										handleError(error);
										progress.dismiss();
									}
								})
								.exec(instance.getDomain(), token);
					}

					@Override
					public void onError(ErrorResponse error){
						Log.e(TAG, "OAuth:Callback - Token exchange failed");
						handleError(error);
						progress.dismiss();
					}
				})
				.execNoAuth(instance.getDomain());
	}

	private void handleError(ErrorResponse error){
		error.showToast(OAuthActivity.this);
		finish();
		restartMainActivity();
	}

	private void restartMainActivity(){
		Intent intent = PostAuthNavigator.createPostAuthIntent(
				this,
				Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP
		);
		startActivity(intent);
	}
}
