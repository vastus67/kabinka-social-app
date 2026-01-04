package app.kabinka.social.api.requests.instance;

import app.kabinka.social.api.MastodonAPIRequest;

import java.time.Instant;

public class GetInstanceExtendedDescription extends MastodonAPIRequest<GetInstanceExtendedDescription.Response>{
	public GetInstanceExtendedDescription(){
		super(HttpMethod.GET, "/instance/extended_description", Response.class);
	}

	public static class Response{
		public Instant updatedAt;
		public String content;
	}
}
