package app.kabinka.social.api.requests.notifications;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Notification;

public class GetNotificationByID extends MastodonAPIRequest<Notification>{
	public GetNotificationByID(String id){
		super(HttpMethod.GET, "/notifications/"+id, Notification.class);
	}
}
