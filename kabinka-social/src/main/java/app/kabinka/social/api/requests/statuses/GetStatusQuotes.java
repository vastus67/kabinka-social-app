package app.kabinka.social.api.requests.statuses;

import com.google.gson.reflect.TypeToken;

import app.kabinka.social.api.requests.HeaderPaginationRequest;
import app.kabinka.social.model.Status;

public class GetStatusQuotes extends HeaderPaginationRequest<Status>{
	public GetStatusQuotes(String id, String maxID, int limit){
		super(HttpMethod.GET, "/statuses/"+id+"/quotes", new TypeToken<>(){});
		if(maxID!=null)
			addQueryParameter("max_id", maxID);
		if(limit>0)
			addQueryParameter("limit", limit+"");
	}
}
