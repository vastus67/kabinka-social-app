package app.kabinka.social.model.catalog;

import app.kabinka.social.api.AllFieldsAreRequired;
import app.kabinka.social.model.BaseModel;

@AllFieldsAreRequired
public class CatalogCategory extends BaseModel{
	public String category;
	public int serversCount;

	@Override
	public String toString(){
		return "CatalogCategory{"+
				"category='"+category+'\''+
				", serversCount="+serversCount+
				'}';
	}
}
