package app.kabinka.social.api.requests.lists;

import app.kabinka.social.api.ResultlessMastodonAPIRequest;

public class DeleteList extends ResultlessMastodonAPIRequest{
	public DeleteList(String id){
		super(HttpMethod.DELETE, "/lists/"+id);
	}
}
