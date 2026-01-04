package app.kabinka.social.api.requests.statuses;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Translation;

import java.util.Map;

public class TranslateStatus extends MastodonAPIRequest<Translation>{
	public TranslateStatus(String id, String lang){
		super(HttpMethod.POST, "/statuses/"+id+"/translate", Translation.class);
		setRequestBody(Map.of("lang", lang));
	}
}
