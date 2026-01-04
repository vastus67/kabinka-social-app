package app.kabinka.social.model.viewmodel;

import android.content.Context;

import app.kabinka.social.R;
import app.kabinka.social.model.Hashtag;
import app.kabinka.social.model.SearchResult;

public class SearchResultViewModel{
	public SearchResult result;
	public AccountViewModel account;
	public ListItem<Hashtag> hashtagItem;

	public SearchResultViewModel(SearchResult result, String accountID, boolean isRecents, Context context){
		this.result=result;
		switch(result.type){
			case ACCOUNT -> account=new AccountViewModel(result.account, accountID, context);
			case HASHTAG -> {
				hashtagItem=new ListItem<>((isRecents ? "#" : "")+result.hashtag.name, null, isRecents ? R.drawable.ic_history_24px : R.drawable.ic_tag_24px, null, result.hashtag);
				hashtagItem.isEnabled=true;
			}
		}
	}
}
