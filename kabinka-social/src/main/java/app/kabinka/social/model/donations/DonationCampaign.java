package app.kabinka.social.model.donations;

import app.kabinka.social.api.AllFieldsAreRequired;
import app.kabinka.social.api.ObjectValidationException;
import app.kabinka.social.api.RequiredField;
import app.kabinka.social.model.BaseModel;

import java.util.Map;

@AllFieldsAreRequired
public class DonationCampaign extends BaseModel{
	public String id;
	public String bannerMessage;
	public String bannerButtonText;
	public String donationMessage;
	public String donationButtonText;
	public Amounts amounts;
	public String defaultCurrency;
	public String donationUrl;
	public String donationSuccessPost;

	@Override
	public void postprocess() throws ObjectValidationException{
		super.postprocess();
		amounts.postprocess();
	}

	public static class Amounts extends BaseModel{
		public Map<String, long[]> oneTime;
		@RequiredField
		public Map<String, long[]> monthly;
		public Map<String, long[]> yearly;
	}
}
