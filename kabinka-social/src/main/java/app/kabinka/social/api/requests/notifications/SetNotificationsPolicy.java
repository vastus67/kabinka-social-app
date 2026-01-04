package app.kabinka.social.api.requests.notifications;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.NotificationsPolicy;

public class SetNotificationsPolicy extends MastodonAPIRequest<NotificationsPolicy>{
	public SetNotificationsPolicy(NotificationsPolicy policy){
		super(HttpMethod.PUT, "/notifications/policy", NotificationsPolicy.class);
		setRequestBody(policy);
	}
}
