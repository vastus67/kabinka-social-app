package app.kabinka.social.api.requests.accounts;

import com.google.gson.reflect.TypeToken;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.FollowList;

import java.util.List;

public class GetAccountLists extends MastodonAPIRequest<List<FollowList>>{
	public GetAccountLists(String id){
		super(HttpMethod.GET, "/accounts/"+id+"/lists", new TypeToken<>(){});
	}
}
