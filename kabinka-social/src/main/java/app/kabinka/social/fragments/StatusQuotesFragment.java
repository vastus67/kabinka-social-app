package app.kabinka.social.fragments;

import android.os.Bundle;

import app.kabinka.social.R;
import app.kabinka.social.api.requests.statuses.GetStatusQuotes;
import app.kabinka.social.model.HeaderPaginationList;
import app.kabinka.social.model.Status;
import org.parceler.Parcels;

import me.grishka.appkit.api.SimpleCallback;

public class StatusQuotesFragment extends StatusListFragment{
	private Status status;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		status=Parcels.unwrap(getArguments().getParcelable("status"));
		setTitle(getResources().getQuantityString(R.plurals.x_quotes, (int)status.quotesCount, status.quotesCount));
		loadData();
	}

	@Override
	protected void doLoadData(int offset, int count){
		new GetStatusQuotes(status.id, offset>0 ? getMaxID() : null, count)
				.setCallback(new SimpleCallback<>(this){
					@Override
					public void onSuccess(HeaderPaginationList<Status> result){
						if(getActivity()==null)
							return;
						onDataLoaded(result, result.nextPageUri!=null);
					}
				})
				.exec(accountID);
	}
}
