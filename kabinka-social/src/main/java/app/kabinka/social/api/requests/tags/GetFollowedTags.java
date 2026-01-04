package app.kabinka.social.api.requests.tags;

import com.google.gson.reflect.TypeToken;

import app.kabinka.social.api.requests.HeaderPaginationRequest;
import app.kabinka.social.model.Hashtag;

public class GetFollowedTags extends HeaderPaginationRequest<Hashtag>{
	public GetFollowedTags(String maxID, int limit){
		super(HttpMethod.GET, "/followed_tags", new TypeToken<>(){});
		if(maxID!=null)
			addQueryParameter("max_id", maxID);
		if(limit>0)
			addQueryParameter("limit", limit+"");
	}
}
