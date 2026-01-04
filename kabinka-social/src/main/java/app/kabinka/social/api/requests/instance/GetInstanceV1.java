package app.kabinka.social.api.requests.instance;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.InstanceV1;

public class GetInstanceV1 extends MastodonAPIRequest<InstanceV1>{
	public GetInstanceV1(){
		super(HttpMethod.GET, "/instance", InstanceV1.class);
	}
}
