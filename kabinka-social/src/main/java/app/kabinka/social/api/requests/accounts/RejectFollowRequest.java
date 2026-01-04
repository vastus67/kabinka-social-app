package app.kabinka.social.api.requests.accounts;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Relationship;

public class RejectFollowRequest extends MastodonAPIRequest<Relationship> {
	public RejectFollowRequest(String accountID) {
		super(HttpMethod.POST, "/follow_requests/" + accountID + "/reject", Relationship.class);
		setRequestBody(new Object());
	}
}
