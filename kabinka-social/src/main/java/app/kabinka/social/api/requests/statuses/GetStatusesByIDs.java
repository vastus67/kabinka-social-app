package app.kabinka.social.api.requests.statuses;

import com.google.gson.reflect.TypeToken;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Status;

import java.util.Collection;
import java.util.List;

public class GetStatusesByIDs extends MastodonAPIRequest<List<Status>>{
	public GetStatusesByIDs(Collection<String> ids){
		super(HttpMethod.GET, "/statuses", new TypeToken<>(){});
		for(String id:ids)
			addQueryParameter("id[]", id);
	}
}
