package app.kabinka.social.fragments.discover;

import android.os.Bundle;

import app.kabinka.social.api.requests.accounts.GetFollowSuggestions;
import app.kabinka.social.fragments.ScrollableToTop;
import app.kabinka.social.fragments.account_list.BaseAccountListFragment;
import app.kabinka.social.model.FollowSuggestion;
import app.kabinka.social.model.viewmodel.AccountViewModel;
import app.kabinka.social.ui.utils.DiscoverInfoBannerHelper;

import java.util.List;
import java.util.stream.Collectors;

import androidx.recyclerview.widget.RecyclerView;
import me.grishka.appkit.api.SimpleCallback;
import me.grishka.appkit.utils.MergeRecyclerAdapter;

public class DiscoverAccountsFragment extends BaseAccountListFragment implements ScrollableToTop{
	private DiscoverInfoBannerHelper bannerHelper;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		bannerHelper=new DiscoverInfoBannerHelper(DiscoverInfoBannerHelper.BannerType.ACCOUNTS, accountID);
	}

	@Override
	protected void doLoadData(int offset, int count){
		currentRequest=new GetFollowSuggestions(count)
				.setCallback(new SimpleCallback<>(this){
					@Override
					public void onSuccess(List<FollowSuggestion> result){
						List<AccountViewModel> accounts=result.stream().map(fs->new AccountViewModel(fs.account, accountID, getActivity())).collect(Collectors.toList());
						onDataLoaded(accounts, false);
						bannerHelper.onBannerBecameVisible();
					}
				})
				.exec(accountID);
	}

	@Override
	protected RecyclerView.Adapter getAdapter(){
		MergeRecyclerAdapter adapter=new MergeRecyclerAdapter();
		bannerHelper.maybeAddBanner(list, adapter);
		adapter.addAdapter(super.getAdapter());
		return adapter;
	}

	@Override
	public void scrollToTop(){
		smoothScrollRecyclerViewToTop(list);
	}
}
