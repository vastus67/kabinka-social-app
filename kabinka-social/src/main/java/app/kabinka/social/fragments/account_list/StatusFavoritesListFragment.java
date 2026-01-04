package app.kabinka.social.fragments.account_list;

import android.os.Bundle;

import app.kabinka.social.R;
import app.kabinka.social.api.requests.HeaderPaginationRequest;
import app.kabinka.social.api.requests.statuses.GetStatusFavorites;
import app.kabinka.social.model.Account;

public class StatusFavoritesListFragment extends StatusRelatedAccountListFragment{
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTitle(getResources().getQuantityString(R.plurals.x_favorites, (int)(status.favouritesCount%1000), status.favouritesCount));
	}

	@Override
	public HeaderPaginationRequest<Account> onCreateRequest(String maxID, int count){
		return new GetStatusFavorites(status.id, maxID, count);
	}
}
