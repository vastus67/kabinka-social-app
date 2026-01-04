package app.kabinka.social.api.requests.statuses;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Status;

public class SetStatusBookmarked extends MastodonAPIRequest<Status>{
	public SetStatusBookmarked(String id, boolean bookmarked){
		super(HttpMethod.POST, "/statuses/"+id+"/"+(bookmarked ? "bookmark" : "unbookmark"), Status.class);
		setRequestBody(new Object());
	}
}
