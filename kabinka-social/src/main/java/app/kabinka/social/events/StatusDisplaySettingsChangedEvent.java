package app.kabinka.social.events;

public class StatusDisplaySettingsChangedEvent{
	public final String accountID;

	public StatusDisplaySettingsChangedEvent(String accountID){
		this.accountID=accountID;
	}
}
