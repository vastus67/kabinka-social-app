package app.kabinka.social.api.requests.accounts;

import com.google.gson.reflect.TypeToken;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.FamiliarFollowers;

import java.util.Collection;
import java.util.List;

public class GetAccountFamiliarFollowers extends MastodonAPIRequest<List<FamiliarFollowers>>{
	public GetAccountFamiliarFollowers(Collection<String> ids){
		super(HttpMethod.GET, "/accounts/familiar_followers", new TypeToken<>(){});
		for(String id:ids){
			addQueryParameter("id[]", id);
		}
	}
}
