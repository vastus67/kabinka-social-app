package app.kabinka.social.model;

import app.kabinka.social.api.ObjectValidationException;
import app.kabinka.social.api.RequiredField;

public class FollowSuggestion extends BaseModel{
	@RequiredField
	public Account account;
//	public String source;

	@Override
	public void postprocess() throws ObjectValidationException{
		super.postprocess();
		account.postprocess();
	}
}
