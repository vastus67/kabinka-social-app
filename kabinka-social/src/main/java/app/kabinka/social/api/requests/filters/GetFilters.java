package app.kabinka.social.api.requests.filters;

import com.google.gson.reflect.TypeToken;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Filter;

import java.util.List;

public class GetFilters extends MastodonAPIRequest<List<Filter>>{
	public GetFilters(){
		super(HttpMethod.GET, "/filters", new TypeToken<>(){});
	}

	@Override
	protected String getPathPrefix(){
		return "/api/v2";
	}
}
