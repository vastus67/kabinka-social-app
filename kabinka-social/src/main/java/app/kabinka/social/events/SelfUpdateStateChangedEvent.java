package app.kabinka.social.events;

import app.kabinka.social.updater.GithubSelfUpdater;

public class SelfUpdateStateChangedEvent{
	public final GithubSelfUpdater.UpdateState state;

	public SelfUpdateStateChangedEvent(GithubSelfUpdater.UpdateState state){
		this.state=state;
	}
}
