package app.kabinka.social.api.requests.accounts;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Account;

public class GetAccountByID extends MastodonAPIRequest<Account>{
	public GetAccountByID(String id){
		super(HttpMethod.GET, "/accounts/"+id, Account.class);
	}
}
