package app.kabinka.social.fragments.account_list;

import android.os.Bundle;

import app.kabinka.social.R;
import app.kabinka.social.api.requests.HeaderPaginationRequest;
import app.kabinka.social.api.requests.accounts.GetAccountFollowing;
import app.kabinka.social.model.Account;

public class FollowingListFragment extends AccountRelatedAccountListFragment{

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setSubtitle(getResources().getQuantityString(R.plurals.x_following, (int)(account.followingCount%1000), account.followingCount));
	}

	@Override
	public HeaderPaginationRequest<Account> onCreateRequest(String maxID, int count){
		return new GetAccountFollowing(account.id, maxID, count);
	}
}
