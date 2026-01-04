package app.kabinka.social.api.requests.statuses;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Status;

public class DeleteStatus extends MastodonAPIRequest<Status>{
	public DeleteStatus(String id){
		super(HttpMethod.DELETE, "/statuses/"+id, Status.class);
	}
}
