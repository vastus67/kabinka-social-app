package app.kabinka.social.api.requests.lists;

import com.google.gson.reflect.TypeToken;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.FollowList;

import java.util.List;

public class GetLists extends MastodonAPIRequest<List<FollowList>>{
	public GetLists(){
		super(HttpMethod.GET, "/lists", new TypeToken<>(){});
	}
}
