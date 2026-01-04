package app.kabinka.social.api.requests.timelines;

import com.google.gson.reflect.TypeToken;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Status;

import java.util.List;

public class GetHomeTimeline extends MastodonAPIRequest<List<Status>>{
	public GetHomeTimeline(String maxID, String minID, int limit, String sinceID){
		super(HttpMethod.GET, "/timelines/home", new TypeToken<>(){});
		if(maxID!=null)
			addQueryParameter("max_id", maxID);
		if(minID!=null)
			addQueryParameter("min_id", minID);
		if(sinceID!=null)
			addQueryParameter("since_id", sinceID);
		if(limit>0)
			addQueryParameter("limit", ""+limit);
	}
}
