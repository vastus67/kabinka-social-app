package app.kabinka.social.api.requests.accounts;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Relationship;

public class SetAccountBlocked extends MastodonAPIRequest<Relationship>{
	public SetAccountBlocked(String id, boolean blocked){
		super(HttpMethod.POST, "/accounts/"+id+"/"+(blocked ? "block" : "unblock"), Relationship.class);
		setRequestBody(new Object());
	}
}
