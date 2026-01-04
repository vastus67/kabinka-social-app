package app.kabinka.social.api.requests.trends;

import com.google.gson.reflect.TypeToken;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Card;

import java.util.List;

public class GetTrendingLinks extends MastodonAPIRequest<List<Card>>{
	public GetTrendingLinks(int limit){
		super(HttpMethod.GET, "/trends/links", new TypeToken<>(){});
		addQueryParameter("limit", String.valueOf(limit));
	}
}
