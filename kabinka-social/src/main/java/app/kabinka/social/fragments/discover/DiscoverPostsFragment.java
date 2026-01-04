package app.kabinka.social.fragments.discover;

import android.os.Bundle;

import app.kabinka.social.api.requests.trends.GetTrendingStatuses;
import app.kabinka.social.api.session.AccountSessionManager;
import app.kabinka.social.fragments.StatusListFragment;
import app.kabinka.social.model.FilterContext;
import app.kabinka.social.model.Status;
import app.kabinka.social.ui.utils.DiscoverInfoBannerHelper;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.utils.MergeRecyclerAdapter;

public class DiscoverPostsFragment extends StatusListFragment{
	private DiscoverInfoBannerHelper bannerHelper;
	private int realOffset=0;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		bannerHelper=new DiscoverInfoBannerHelper(DiscoverInfoBannerHelper.BannerType.TRENDING_POSTS, accountID);
	}

	@Override
	protected void doLoadData(int offset, int count){
		currentRequest=new GetTrendingStatuses(offset==0 ? 0 : realOffset, count)
				.setCallback(new SimpleCallback<>(this){
					@Override
					public void onSuccess(List<Status> result){
						realOffset+=result.size();
						AccountSessionManager.get(accountID).filterStatuses(result, FilterContext.PUBLIC);
						onDataLoaded(result, !result.isEmpty());
						bannerHelper.onBannerBecameVisible();
					}
				}).exec(accountID);
	}

	@Override
	protected RecyclerView.Adapter getAdapter(){
		MergeRecyclerAdapter adapter=new MergeRecyclerAdapter();
		bannerHelper.maybeAddBanner(list, adapter);
		adapter.addAdapter(super.getAdapter());
		return adapter;
	}
}
