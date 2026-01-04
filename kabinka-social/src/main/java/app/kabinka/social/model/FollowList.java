package app.kabinka.social.model;

import com.google.gson.annotations.SerializedName;

import app.kabinka.social.api.AllFieldsAreRequired;
import app.kabinka.social.api.ObjectValidationException;
import org.parceler.Parcel;

// Called like this to avoid conflict with java.util.List
@AllFieldsAreRequired
@Parcel
public class FollowList extends BaseModel{
	public String id;
	public String title;
	public RepliesPolicy repliesPolicy=RepliesPolicy.LIST;
	public boolean exclusive;

	@Override
	public String toString(){
		return "FollowList{"+
				"id='"+id+'\''+
				", title='"+title+'\''+
				", repliesPolicy="+repliesPolicy+
				", exclusive="+exclusive+
				'}';
	}

	@Override
	public void postprocess() throws ObjectValidationException{
		if(repliesPolicy==null)
			repliesPolicy=RepliesPolicy.LIST;
		super.postprocess();
	}

	public enum RepliesPolicy{
		@SerializedName("followed")
		FOLLOWED,
		@SerializedName("list")
		LIST,
		@SerializedName("none")
		NONE
	}
}
