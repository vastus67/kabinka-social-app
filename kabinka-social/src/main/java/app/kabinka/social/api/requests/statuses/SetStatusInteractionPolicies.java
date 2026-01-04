package app.kabinka.social.api.requests.statuses;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Status;
import app.kabinka.social.model.StatusQuotePolicy;

import java.util.Map;

public class SetStatusInteractionPolicies extends MastodonAPIRequest<Status>{
	public SetStatusInteractionPolicies(String id, StatusQuotePolicy quotePolicy){
		super(HttpMethod.PUT, "/statuses/"+id+"/interaction_policy", Status.class);
		setRequestBody(Map.of("quote_approval_policy", quotePolicy));
	}
}
