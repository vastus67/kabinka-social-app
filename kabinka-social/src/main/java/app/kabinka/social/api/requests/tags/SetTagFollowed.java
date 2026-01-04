package app.kabinka.social.api.requests.tags;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Hashtag;

public class SetTagFollowed extends MastodonAPIRequest<Hashtag>{
	public SetTagFollowed(String tag, boolean followed){
		super(HttpMethod.POST, "/tags/"+tag+(followed ? "/follow" : "/unfollow"), Hashtag.class);
		setRequestBody(new Object());
	}
}
