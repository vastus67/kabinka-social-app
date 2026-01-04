package app.kabinka.social.api.requests.statuses;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Status;

public class SetStatusConversationMuted extends MastodonAPIRequest<Status>{
	public SetStatusConversationMuted(String id, boolean muted){
		super(HttpMethod.POST, "/statuses/"+id+(muted ? "/mute" : "/unmute"), Status.class);
		setRequestBody(new Object());
	}
}
