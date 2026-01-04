package app.kabinka.social.fragments;

import android.os.Bundle;

import app.kabinka.social.api.requests.accounts.GetAccountStatuses;
import app.kabinka.social.api.session.AccountSessionManager;
import app.kabinka.social.model.Account;
import app.kabinka.social.model.FilterContext;
import app.kabinka.social.model.Hashtag;
import app.kabinka.social.model.Status;
import org.parceler.Parcels;

import java.util.List;

import me.grishka.appkit.api.SimpleCallback;

// The difference between this and HashtagTimelineFragment is that this opens from the featured hashtags
// and only shows posts by that account.
public class HashtagFeaturedTimelineFragment extends StatusListFragment{
	private Account targetAccount;
	private Hashtag hashtag;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		targetAccount=Parcels.unwrap(getArguments().getParcelable("targetAccount"));
		hashtag=Parcels.unwrap(getArguments().getParcelable("hashtag"));
		setTitle("#"+hashtag.name);
		loadData();
	}

	@Override
	protected void doLoadData(int offset, int count){
		currentRequest=new GetAccountStatuses(targetAccount.id, offset>0 ? getMaxID() : null, null, count, GetAccountStatuses.Filter.DEFAULT, hashtag.name)
				.setCallback(new SimpleCallback<>(this){
					@Override
					public void onSuccess(List<Status> result){
						if(getActivity()==null)
							return;
						boolean empty=result.isEmpty();
						AccountSessionManager.get(accountID).filterStatuses(result, FilterContext.ACCOUNT);
						onDataLoaded(result, !empty);
					}
				})
				.exec(accountID);
	}
}
