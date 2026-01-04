package app.kabinka.social.fragments.account_list;

import android.os.Bundle;

import app.kabinka.social.R;
import app.kabinka.social.api.requests.HeaderPaginationRequest;
import app.kabinka.social.api.requests.accounts.GetAccountFollowers;
import app.kabinka.social.model.Account;

public class FollowerListFragment extends AccountRelatedAccountListFragment{

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setSubtitle(getResources().getQuantityString(R.plurals.x_followers, (int)(account.followersCount%1000), account.followersCount));
	}

	@Override
	public HeaderPaginationRequest<Account> onCreateRequest(String maxID, int count){
		return new GetAccountFollowers(account.id, maxID, count);
	}
}
