package app.kabinka.social.api.requests.notifications;

import com.google.gson.reflect.TypeToken;

import app.kabinka.social.api.requests.HeaderPaginationRequest;
import app.kabinka.social.model.NotificationRequest;

public class GetNotificationRequests extends HeaderPaginationRequest<NotificationRequest>{
	public GetNotificationRequests(String maxID){
		super(HttpMethod.GET, "/notifications/requests", new TypeToken<>(){});
		if(maxID!=null)
			addQueryParameter("max_id", maxID);
	}
}
