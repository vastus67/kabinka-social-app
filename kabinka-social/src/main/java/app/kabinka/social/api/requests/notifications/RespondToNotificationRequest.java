package app.kabinka.social.api.requests.notifications;

import app.kabinka.social.api.ResultlessMastodonAPIRequest;

public class RespondToNotificationRequest extends ResultlessMastodonAPIRequest{
	public RespondToNotificationRequest(String id, boolean allow){
		super(HttpMethod.POST, "/notifications/requests/"+id+(allow ? "/accept" : "/dismiss"));
		setRequestBody(new Object());
	}
}
