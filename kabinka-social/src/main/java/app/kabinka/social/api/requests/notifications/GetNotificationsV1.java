package app.kabinka.social.api.requests.notifications;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;

import app.kabinka.social.api.ApiUtils;
import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Notification;
import app.kabinka.social.model.NotificationType;

import java.util.EnumSet;
import java.util.List;

public class GetNotificationsV1 extends MastodonAPIRequest<List<Notification>>{
	public GetNotificationsV1(String maxID, int limit, EnumSet<NotificationType> includeTypes){
		this(maxID, limit, includeTypes, null);
	}

	public GetNotificationsV1(String maxID, int limit, EnumSet<NotificationType> includeTypes, String onlyAccountID){
		super(HttpMethod.GET, "/notifications", new TypeToken<>(){});
		if(maxID!=null)
			addQueryParameter("max_id", maxID);
		if(limit>0)
			addQueryParameter("limit", ""+limit);
		if(includeTypes!=null){
			for(String type:ApiUtils.enumSetToStrings(includeTypes, NotificationType.class)){
				addQueryParameter("types[]", type);
			}
			for(String type:ApiUtils.enumSetToStrings(EnumSet.complementOf(includeTypes), NotificationType.class)){
				addQueryParameter("exclude_types[]", type);
			}
		}
		if(!TextUtils.isEmpty(onlyAccountID))
			addQueryParameter("account_id", onlyAccountID);
		removeUnsupportedItems=true;
	}
}
