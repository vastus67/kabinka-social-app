package app.kabinka.social.ui.viewholders;

import android.content.Context;
import android.view.ViewGroup;

import app.kabinka.social.R;
import app.kabinka.social.model.viewmodel.ListItem;

public class SimpleListItemViewHolder extends ListItemViewHolder<ListItem<?>>{
	public SimpleListItemViewHolder(Context context, ViewGroup parent){
		super(context, R.layout.item_generic_list, parent);
	}
}
