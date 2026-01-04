package app.kabinka.social.model;

import app.kabinka.social.api.AllFieldsAreRequired;
import app.kabinka.social.api.ObjectValidationException;

import java.util.List;

@AllFieldsAreRequired
public class FamiliarFollowers extends BaseModel{
	public String id;
	public List<Account> accounts;

	@Override
	public void postprocess() throws ObjectValidationException{
		super.postprocess();
		for(Account acc:accounts){
			acc.postprocess();
		}
	}
}
