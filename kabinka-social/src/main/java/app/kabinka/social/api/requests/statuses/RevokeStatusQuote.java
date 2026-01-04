package app.kabinka.social.api.requests.statuses;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Status;

public class RevokeStatusQuote extends MastodonAPIRequest<Status>{
	public RevokeStatusQuote(String id, String quoteID){
		super(HttpMethod.POST, "/statuses/"+id+"/quotes/"+quoteID+"/revoke", Status.class);
		setRequestBody(new Object());
	}
}
