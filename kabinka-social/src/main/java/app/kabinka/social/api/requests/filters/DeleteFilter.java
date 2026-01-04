package app.kabinka.social.api.requests.filters;

import app.kabinka.social.api.ResultlessMastodonAPIRequest;

public class DeleteFilter extends ResultlessMastodonAPIRequest{
	public DeleteFilter(String id){
		super(HttpMethod.DELETE, "/filters/"+id);
	}

	@Override
	protected String getPathPrefix(){
		return "/api/v2";
	}
}
