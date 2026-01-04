package app.kabinka.social.model.viewmodel;

import app.kabinka.social.model.Account;
import app.kabinka.social.model.DisplayItemsParent;
import app.kabinka.social.model.NotificationGroup;
import app.kabinka.social.model.Status;

import java.util.List;

public class NotificationViewModel implements DisplayItemsParent{
	public NotificationGroup notification;
	public List<Account> accounts;
	public Status status;

	@Override
	public String getID(){
		return notification.groupKey;
	}
}
