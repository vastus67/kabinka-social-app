package app.kabinka.social.model.viewmodel;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;

import app.kabinka.social.GlobalUserPreferences;
import app.kabinka.social.model.Account;
import app.kabinka.social.model.AccountField;
import app.kabinka.social.ui.text.HtmlParser;
import app.kabinka.social.ui.text.LinkSpan;
import app.kabinka.social.ui.utils.CustomEmojiHelper;

import java.util.Collections;

import me.grishka.appkit.imageloader.requests.ImageLoaderRequest;
import me.grishka.appkit.imageloader.requests.UrlImageLoaderRequest;
import me.grishka.appkit.utils.V;

public class AccountViewModel{
	public final Account account;
	public final ImageLoaderRequest avaRequest;
	public final CustomEmojiHelper emojiHelper;
	public final CharSequence parsedName, parsedBio;
	public final String verifiedLink;

	public AccountViewModel(Account account, String accountID, Context context){
		this(account, accountID, true, context);
	}

	public AccountViewModel(Account account, String accountID, boolean needBio, Context context){
		this.account=account;
		avaRequest=new UrlImageLoaderRequest(GlobalUserPreferences.playGifs ? account.avatar : account.avatarStatic, V.dp(50), V.dp(50));
		emojiHelper=new CustomEmojiHelper();
		if(GlobalUserPreferences.customEmojiInNames)
			parsedName=HtmlParser.parseCustomEmoji(account.displayName, account.emojis);
		else
			parsedName=account.displayName;
		SpannableStringBuilder ssb=new SpannableStringBuilder(parsedName);
		if(needBio){
			parsedBio=HtmlParser.parse(account.note, account.emojis, Collections.emptyList(), Collections.emptyList(), accountID, account, context);
			ssb.append(parsedBio);
		}else{
			parsedBio=null;
		}
		emojiHelper.setText(ssb);
		String verifiedLink=null;
		for(AccountField fld:account.fields){
			if(fld.verifiedAt!=null){
				verifiedLink=HtmlParser.stripAndRemoveInvisibleSpans(fld.value);
				break;
			}
		}
		this.verifiedLink=verifiedLink;
	}

	public AccountViewModel stripLinksFromBio(){
		if(parsedBio instanceof Spannable spannable){
			for(LinkSpan span:spannable.getSpans(0, spannable.length(), LinkSpan.class)){
				spannable.removeSpan(span);
			}
		}
		return this;
	}
}
