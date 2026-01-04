package app.kabinka.social.model;

import app.kabinka.social.api.AllFieldsAreRequired;
import org.parceler.Parcel;

@AllFieldsAreRequired
@Parcel
public class FilterStatus extends BaseModel{
	public String id;
	public String statusId;
}
