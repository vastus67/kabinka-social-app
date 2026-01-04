package app.kabinka.social.model;

import app.kabinka.social.api.AllFieldsAreRequired;
import org.parceler.Parcel;

@AllFieldsAreRequired
@Parcel
public class FilterKeyword extends BaseModel{
	public String id;
	public String keyword;
	public boolean wholeWord;

	@Override
	public String toString(){
		return "FilterKeyword{"+
				"id='"+id+'\''+
				", keyword='"+keyword+'\''+
				", wholeWord="+wholeWord+
				'}';
	}
}
