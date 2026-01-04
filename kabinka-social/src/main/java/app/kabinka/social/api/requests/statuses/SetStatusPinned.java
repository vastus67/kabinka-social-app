package app.kabinka.social.api.requests.statuses;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Status;

public class SetStatusPinned extends MastodonAPIRequest<Status>{
	public SetStatusPinned(String id, boolean pinned){
		super(HttpMethod.POST, "/statuses/"+id+"/"+(pinned ? "pin" : "unpin"), Status.class);
		setRequestBody(new Object());
	}
}
