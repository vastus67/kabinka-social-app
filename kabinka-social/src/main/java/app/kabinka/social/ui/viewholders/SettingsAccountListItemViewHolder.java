package app.kabinka.social.ui.viewholders;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;

import app.kabinka.social.R;
import app.kabinka.social.model.viewmodel.SettingsAccountListItem;
import app.kabinka.social.ui.OutlineProviders;

import me.grishka.appkit.imageloader.ImageLoaderViewHolder;

public class SettingsAccountListItemViewHolder extends ListItemViewHolder<SettingsAccountListItem<?>> implements ImageLoaderViewHolder{

	public SettingsAccountListItemViewHolder(Context context, ViewGroup parent){
		super(context, R.layout.item_generic_list, parent);
		icon.setOutlineProvider(OutlineProviders.OVAL);
		icon.setClipToOutline(true);
		icon.setImageTintList(null);
	}

	@Override
	protected void bindIcon(SettingsAccountListItem<?> item){}

	@Override
	public void setImage(int index, Drawable image){
		icon.setImageDrawable(image);
	}

	@Override
	public void clearImage(int index){
		icon.setImageResource(R.drawable.image_placeholder);
	}
}
