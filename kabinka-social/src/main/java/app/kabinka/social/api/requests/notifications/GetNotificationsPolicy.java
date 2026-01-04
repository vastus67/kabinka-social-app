package app.kabinka.social.api.requests.notifications;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.NotificationsPolicy;

public class GetNotificationsPolicy extends MastodonAPIRequest<NotificationsPolicy>{
	public GetNotificationsPolicy(){
		super(HttpMethod.GET, "/notifications/policy", NotificationsPolicy.class);
	}
}
