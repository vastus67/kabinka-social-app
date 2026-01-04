package app.kabinka.social.model;

import app.kabinka.social.api.ObjectValidationException;
import app.kabinka.social.api.RequiredField;
import org.parceler.Parcel;

import java.util.List;

@Parcel
public class FilterResult extends BaseModel{
	@RequiredField
	public Filter filter;

	public List<String> keywordMatches;

	@Override
	public void postprocess() throws ObjectValidationException{
		super.postprocess();
		filter.postprocess();
	}
}
