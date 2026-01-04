package app.kabinka.social.events;

public class DismissDonationCampaignBannerEvent{
	public final String campaignID;

	public DismissDonationCampaignBannerEvent(String campaignID){
		this.campaignID=campaignID;
	}
}
