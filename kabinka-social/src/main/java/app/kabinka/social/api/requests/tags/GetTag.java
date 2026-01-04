package app.kabinka.social.api.requests.tags;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Hashtag;

public class GetTag extends MastodonAPIRequest<Hashtag>{
	public GetTag(String tag){
		super(HttpMethod.GET, "/tags/"+tag, Hashtag.class);
	}
}
