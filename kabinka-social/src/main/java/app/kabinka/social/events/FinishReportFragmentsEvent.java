package app.kabinka.social.events;

public class FinishReportFragmentsEvent{
	public final String reportAccountID;

	public FinishReportFragmentsEvent(String reportAccountID){
		this.reportAccountID=reportAccountID;
	}
}
