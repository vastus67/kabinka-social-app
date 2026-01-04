package app.kabinka.social.api.requests.accounts;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Preferences;

public class GetPreferences extends MastodonAPIRequest<Preferences> {
    public GetPreferences(){
        super(HttpMethod.GET, "/preferences", Preferences.class);
    }
}
