package app.kabinka.social.api.requests.polls;

import app.kabinka.social.api.MastodonAPIRequest;
import app.kabinka.social.model.Poll;

import java.util.List;

public class SubmitPollVote extends MastodonAPIRequest<Poll>{
	public SubmitPollVote(String pollID, List<Integer> choices){
		super(HttpMethod.POST, "/polls/"+pollID+"/votes", Poll.class);
		setRequestBody(new Body(choices));
	}

	private static class Body{
		public List<Integer> choices;

		public Body(List<Integer> choices){
			this.choices=choices;
		}
	}
}
