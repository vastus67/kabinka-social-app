package app.kabinka.social.fragments.account_list;

import android.os.Bundle;

import app.kabinka.social.R;
import app.kabinka.social.api.requests.HeaderPaginationRequest;
import app.kabinka.social.api.requests.statuses.GetStatusReblogs;
import app.kabinka.social.model.Account;

public class StatusReblogsListFragment extends StatusRelatedAccountListFragment{
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTitle(getResources().getQuantityString(R.plurals.x_reblogs, (int)(status.reblogsCount%1000), status.reblogsCount));
	}

	@Override
	public HeaderPaginationRequest<Account> onCreateRequest(String maxID, int count){
		return new GetStatusReblogs(status.id, maxID, count);
	}
}
