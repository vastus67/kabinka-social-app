package app.kabinka.social.api.requests.oauth;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.api.session.AccountSessionManager;
import app.kabinka.social.model.Application;

public class CreateOAuthApp extends MastodonAPIRequest<Application>{
	public CreateOAuthApp(){
		super(HttpMethod.POST, "/apps", Application.class);
		setRequestBody(new Request());
	}

	private static class Request{
		public String clientName="Mastodon for Android";
		public String redirectUris=AccountSessionManager.REDIRECT_URI;
		public String scopes=AccountSessionManager.SCOPE;
		public String website="https://app.joinmastodon.org/android";
	}
}
