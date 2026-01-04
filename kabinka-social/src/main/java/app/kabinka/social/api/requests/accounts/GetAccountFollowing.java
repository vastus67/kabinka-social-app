package app.kabinka.social.api.requests.accounts;

import com.google.gson.reflect.TypeToken;

import app.kabinka.social.api.requests.HeaderPaginationRequest;
import app.kabinka.social.model.Account;

public class GetAccountFollowing extends HeaderPaginationRequest<Account>{
	public GetAccountFollowing(String id, String maxID, int limit){
		super(HttpMethod.GET, "/accounts/"+id+"/following", new TypeToken<>(){});
		if(maxID!=null)
			addQueryParameter("max_id", maxID);
		if(limit>0)
			addQueryParameter("limit", limit+"");
	}
}
