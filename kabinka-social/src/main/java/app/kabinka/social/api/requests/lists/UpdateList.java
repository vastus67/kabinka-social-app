package app.kabinka.social.api.requests.lists;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.FollowList;

public class UpdateList extends MastodonAPIRequest<FollowList>{
	public UpdateList(String listID, String title, FollowList.RepliesPolicy repliesPolicy, boolean exclusive){
		super(HttpMethod.PUT, "/lists/"+listID, FollowList.class);
		setRequestBody(new Request(title, repliesPolicy, exclusive));
	}

	private static class Request{
		public String title;
		public FollowList.RepliesPolicy repliesPolicy;
		public boolean exclusive;

		public Request(String title, FollowList.RepliesPolicy repliesPolicy, boolean exclusive){
			this.title=title;
			this.repliesPolicy=repliesPolicy;
			this.exclusive=exclusive;
		}
	}
}
