package app.kabinka.social.api.requests.statuses;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Status;

public class SetStatusReblogged extends MastodonAPIRequest<Status>{
	public SetStatusReblogged(String id, boolean reblogged){
		super(HttpMethod.POST, "/statuses/"+id+"/"+(reblogged ? "reblog" : "unreblog"), Status.class);
		setRequestBody(new Object());
	}
}
