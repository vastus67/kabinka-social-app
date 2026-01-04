package app.kabinka.social.api.requests.instance;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.InstanceV2;

public class GetInstanceV2 extends MastodonAPIRequest<InstanceV2>{
	public GetInstanceV2(){
		super(HttpMethod.GET, "/instance", InstanceV2.class);
	}

	@Override
	protected String getPathPrefix(){
		return "/api/v2";
	}
}
