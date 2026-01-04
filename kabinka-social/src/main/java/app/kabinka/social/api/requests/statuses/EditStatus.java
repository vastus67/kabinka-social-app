package app.kabinka.social.api.requests.statuses;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Status;

public class EditStatus extends MastodonAPIRequest<Status>{
	public EditStatus(CreateStatus.Request req, String id){
		super(HttpMethod.PUT, "/statuses/"+id, Status.class);
		setRequestBody(req);
	}
}
