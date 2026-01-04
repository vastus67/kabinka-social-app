package app.kabinka.social.api.requests.statuses;

import app.kabinka.social.api.AllFieldsAreRequired;
import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.BaseModel;

public class GetStatusSourceText extends MastodonAPIRequest<GetStatusSourceText.Response>{
	public GetStatusSourceText(String id){
		super(HttpMethod.GET, "/statuses/"+id+"/source", Response.class);
	}

	@AllFieldsAreRequired
	public static class Response extends BaseModel{
		public String id;
		public String text;
		public String spoilerText;
	}
}
