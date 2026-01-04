package app.kabinka.social.events;

import app.kabinka.social.model.Poll;

public class PollUpdatedEvent{
	public String accountID;
	public Poll poll;

	public PollUpdatedEvent(String accountID, Poll poll){
		this.accountID=accountID;
		this.poll=poll;
	}
}
