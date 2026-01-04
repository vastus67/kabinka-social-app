package app.kabinka.social.ui.displayitems;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.TextView;

import app.kabinka.social.GlobalUserPreferences;
import app.kabinka.social.R;
import app.kabinka.social.fragments.ProfileFragment;
import app.kabinka.social.model.Account;
import app.kabinka.social.ui.text.HtmlParser;
import app.kabinka.social.ui.text.LinkSpan;
import app.kabinka.social.ui.text.NonColoredLinkSpan;
import app.kabinka.social.ui.utils.CustomEmojiHelper;
import app.kabinka.social.ui.utils.UiUtils;
import org.parceler.Parcels;

import java.util.regex.Pattern;

import androidx.annotation.DrawableRes;
import me.grishka.appkit.Nav;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.imageloader.requests.ImageLoaderRequest;
import me.grishka.appkit.utils.V;

public class ReblogOrReplyLineStatusDisplayItem extends StatusDisplayItem{
	private CharSequence text;
	@DrawableRes
	private int icon;
	private CustomEmojiHelper emojiHelper=new CustomEmojiHelper();

	public ReblogOrReplyLineStatusDisplayItem(String parentID, Callbacks callbacks, Context context, CharSequence text, Account account, @DrawableRes int icon, String accountID){
		super(parentID, callbacks, context);
		if(account!=null){
			SpannableStringBuilder parsedName=new SpannableStringBuilder(account.displayName);
			if(GlobalUserPreferences.customEmojiInNames)
				HtmlParser.parseCustomEmoji(parsedName, account.emojis);
			emojiHelper.setText(parsedName);

			SpannableStringBuilder ssb=new SpannableStringBuilder();
			String[] parts=String.format(text.toString(), "{{name}}").split(Pattern.quote("{{name}}"), 2);
			if(parts.length>1 && !TextUtils.isEmpty(parts[0]))
				ssb.append(parts[0]);

			ssb.append(parsedName, new NonColoredLinkSpan(null, s->{
				Bundle args=new Bundle();
				args.putString("account", accountID);
				args.putParcelable("profileAccount", Parcels.wrap(account));
				Nav.go((Activity) context, ProfileFragment.class, args);
			}, LinkSpan.Type.CUSTOM, null, null, null), 0);

			if(parts.length==1){
				ssb.append(' ');
				ssb.append(parts[0]);
			}else if(!TextUtils.isEmpty(parts[1])){
				ssb.append(parts[1]);
			}

			this.text=ssb;
		}else{
			this.text=text;
		}
		this.icon=icon;
	}

	@Override
	public Type getType(){
		return Type.REBLOG_OR_REPLY_LINE;
	}

	@Override
	public int getImageCount(){
		return emojiHelper.getImageCount();
	}

	@Override
	public ImageLoaderRequest getImageRequest(int index){
		return emojiHelper.getImageRequest(index);
	}

	public static class Holder extends StatusDisplayItem.Holder<ReblogOrReplyLineStatusDisplayItem> implements ImageLoaderViewHolder{
		private final TextView text;
		public Holder(Activity activity, ViewGroup parent){
			super(activity, R.layout.display_item_reblog_or_reply_line, parent);
			text=findViewById(R.id.text);
		}

		@Override
		public void onBind(ReblogOrReplyLineStatusDisplayItem item){
			text.setText(item.text);
			if(item.icon!=0){
				Drawable icon=itemView.getContext().getDrawable(item.icon);
				icon.setBounds(0, 0, V.dp(16), V.dp(16));
				text.setCompoundDrawablesRelative(icon, null, null, null);
				if(Build.VERSION.SDK_INT<Build.VERSION_CODES.N)
					UiUtils.fixCompoundDrawableTintOnAndroid6(text);
			}else{
				text.setCompoundDrawables(null, null, null, null);
			}
		}

		@Override
		public void setImage(int index, Drawable image){
			item.emojiHelper.setImageDrawable(index, image);
			text.invalidate();
		}

		@Override
		public void clearImage(int index){
			setImage(index, null);
		}
	}
}
