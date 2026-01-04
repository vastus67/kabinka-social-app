package app.kabinka.social.model;

/**
 * A model object from which {@link app.kabinka.social.ui.displayitems.StatusDisplayItem}s can be generated.
 */
public interface DisplayItemsParent{
	String getID();

	default String getAccountID(){
		return null;
	}
}
