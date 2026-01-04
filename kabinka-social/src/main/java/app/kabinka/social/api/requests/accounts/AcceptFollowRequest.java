package app.kabinka.social.api.requests.accounts;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Relationship;

public class AcceptFollowRequest extends MastodonAPIRequest<Relationship> {
	public AcceptFollowRequest(String accountID) {
		super(HttpMethod.POST, "/follow_requests/" + accountID + "/authorize", Relationship.class);
		setRequestBody(new Object());
	}
}
