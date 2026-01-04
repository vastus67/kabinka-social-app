package app.kabinka.social.ui.displayitems;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import app.kabinka.social.E;
import app.kabinka.social.GlobalUserPreferences;
import app.kabinka.social.R;
import app.kabinka.social.api.requests.statuses.GetStatusSourceText;
import app.kabinka.social.api.requests.statuses.RevokeStatusQuote;
import app.kabinka.social.api.requests.statuses.SetStatusConversationMuted;
import app.kabinka.social.api.requests.statuses.SetStatusInteractionPolicies;
import app.kabinka.social.api.requests.statuses.SetStatusPinned;
import app.kabinka.social.api.session.AccountSessionManager;
import app.kabinka.social.events.StatusUpdatedEvent;
import app.kabinka.social.fragments.AddAccountToListsFragment;
import app.kabinka.social.fragments.ComposeFragment;
import app.kabinka.social.fragments.NotificationsListFragment;
import app.kabinka.social.fragments.ProfileFragment;
import app.kabinka.social.fragments.report.ReportReasonChoiceFragment;
import app.kabinka.social.model.Account;
import app.kabinka.social.model.Attachment;
import app.kabinka.social.model.Quote;
import app.kabinka.social.model.Relationship;
import app.kabinka.social.model.Status;
import app.kabinka.social.model.StatusPrivacy;
import app.kabinka.social.ui.M3AlertDialogBuilder;
import app.kabinka.social.ui.OutlineProviders;
import app.kabinka.social.ui.Snackbar;
import app.kabinka.social.ui.sheets.ComposerVisibilitySheet;
import app.kabinka.social.ui.text.HtmlParser;
import app.kabinka.social.ui.utils.CustomEmojiHelper;
import app.kabinka.social.ui.utils.UiUtils;
import org.parceler.Parcels;

import java.time.Instant;
import java.util.Locale;

import androidx.annotation.LayoutRes;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.imageloader.ImageLoaderViewHolder;
import me.grishka.appkit.imageloader.requests.ImageLoaderRequest;
import me.grishka.appkit.imageloader.requests.UrlImageLoaderRequest;
import me.grishka.appkit.utils.V;

public class HeaderStatusDisplayItem extends StatusDisplayItem{
	private Account user;
	private Instant createdAt;
	private ImageLoaderRequest avaRequest;
	private String accountID;
	private CustomEmojiHelper emojiHelper=new CustomEmojiHelper();
	private SpannableStringBuilder parsedName;
	public final Status status;
	private boolean hasVisibilityToggle;
	boolean needBottomPadding;
	private String extraText;

	public HeaderStatusDisplayItem(String parentID, Account user, Instant createdAt, Callbacks callbacks, Context context, String accountID, Status status, String extraText){
		super(parentID, callbacks, context);
		this.user=user;
		this.createdAt=createdAt;
		avaRequest=new UrlImageLoaderRequest(GlobalUserPreferences.playGifs ? user.avatar : user.avatarStatic, V.dp(50), V.dp(50));
		this.accountID=accountID;
		parsedName=new SpannableStringBuilder(user.displayName);
		this.status=status;
		if(GlobalUserPreferences.customEmojiInNames)
			HtmlParser.parseCustomEmoji(parsedName, user.emojis);
		emojiHelper.setText(parsedName);
		if(status!=null){
			hasVisibilityToggle=status.sensitive || !TextUtils.isEmpty(status.spoilerText);
			if(!hasVisibilityToggle && !status.mediaAttachments.isEmpty()){
				for(Attachment att:status.mediaAttachments){
					if(att.type!=Attachment.Type.AUDIO){
						hasVisibilityToggle=true;
						break;
					}
				}
			}
		}
		this.extraText=extraText;
	}

	@Override
	public Type getType(){
		return Type.HEADER;
	}

	@Override
	public int getImageCount(){
		return 1+emojiHelper.getImageCount();
	}

	@Override
	public ImageLoaderRequest getImageRequest(int index){
		if(index>0){
			return emojiHelper.getImageRequest(index-1);
		}
		return avaRequest;
	}

	public static class Holder extends StatusDisplayItem.Holder<HeaderStatusDisplayItem> implements ImageLoaderViewHolder{
		private final TextView name, timeAndUsername, extraText;
		private final ImageView avatar, more;
		private final PopupMenu optionsMenu;
		private final View clickableThing;

		public Holder(Activity activity, ViewGroup parent){
			this(activity, R.layout.display_item_header, parent);
		}

		protected Holder(Activity activity, @LayoutRes int layout, ViewGroup parent){
			super(activity, layout, parent);
			name=findViewById(R.id.name);
			timeAndUsername=findViewById(R.id.time_and_username);
			avatar=findViewById(R.id.avatar);
			more=findViewById(R.id.more);
			extraText=findViewById(R.id.extra_text);
			clickableThing=findViewById(R.id.clickable_thing);
			if(clickableThing!=null)
				clickableThing.setOnClickListener(this::onAvaClick);
			avatar.setOutlineProvider(OutlineProviders.roundedRect(10));
			avatar.setClipToOutline(true);
			more.setOnClickListener(this::onMoreClick);

			optionsMenu=new PopupMenu(activity, more);
			optionsMenu.inflate(R.menu.post);
			if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.P && !UiUtils.isEMUI() && !UiUtils.isMagic())
				optionsMenu.getMenu().setGroupDividerEnabled(true);
			optionsMenu.setOnMenuItemClickListener(menuItem->{
				Account account=item.user;
				Relationship relationship=item.callbacks.getRelationship(account.id);
				int id=menuItem.getItemId();
				if(id==R.id.edit){
					final Bundle args=new Bundle();
					args.putString("account", item.accountID);
					args.putParcelable("editStatus", Parcels.wrap(item.status));
					if(TextUtils.isEmpty(item.status.content) && TextUtils.isEmpty(item.status.spoilerText)){
						Nav.go((Activity) item.context, ComposeFragment.class, args);
					}else{
						new GetStatusSourceText(item.status.id)
								.setCallback(new Callback<>(){
									@Override
									public void onSuccess(GetStatusSourceText.Response result){
										args.putString("sourceText", result.text);
										args.putString("sourceSpoiler", result.spoilerText);
										Nav.go((Activity) item.context, ComposeFragment.class, args);
									}

									@Override
									public void onError(ErrorResponse error){
										error.showToast(item.context);
									}
								})
								.wrapProgress((Activity) item.context, R.string.loading, true)
								.exec(item.accountID);
					}
				}else if(id==R.id.delete){
					UiUtils.confirmDeletePost((Activity) item.context, item.accountID, item.status, s->{});
				}else if(id==R.id.mute){
					UiUtils.confirmToggleMuteUser((Activity) item.context, item.accountID, account, relationship!=null && relationship.muting, r->{});
				}else if(id==R.id.block){
					UiUtils.confirmToggleBlockUser((Activity) item.context, item.accountID, account, relationship!=null && relationship.blocking, r->{});
				}else if(id==R.id.report){
					Bundle args=new Bundle();
					args.putString("account", item.accountID);
					args.putParcelable("status", Parcels.wrap(item.status));
					args.putParcelable("reportAccount", Parcels.wrap(item.status.account));
					args.putParcelable("relationship", Parcels.wrap(relationship));
					Nav.go((Activity) item.context, ReportReasonChoiceFragment.class, args);
				}else if(id==R.id.open_in_browser){
					UiUtils.launchWebBrowser(activity, item.status.url);
				}else if(id==R.id.follow){
					if(relationship==null)
						return true;
					ProgressDialog progress=new ProgressDialog(activity);
					progress.setCancelable(false);
					progress.setMessage(activity.getString(R.string.loading));
					UiUtils.performAccountAction(activity, account, item.accountID, relationship, null, visible->{
						if(visible)
							progress.show();
						else
							progress.dismiss();
					}, rel->{
						item.callbacks.putRelationship(account.id, rel);
						Toast.makeText(activity, activity.getString(rel.following ? R.string.followed_user : rel.requested ? R.string.following_user_requested : R.string.unfollowed_user, account.getDisplayUsername()), Toast.LENGTH_SHORT).show();
					});
				}else if(id==R.id.bookmark){
					AccountSessionManager.getInstance().getAccount(item.accountID).getStatusInteractionController().setBookmarked(item.status, !item.status.bookmarked);
				}else if(id==R.id.share){
					UiUtils.openSystemShareSheet(activity, item.status);
				}else if(id==R.id.translate){
					item.callbacks.togglePostTranslation(item.status, item.parentID);
				}else if(id==R.id.add_to_list){
					Bundle args=new Bundle();
					args.putString("account", item.accountID);
					args.putParcelable("targetAccount", Parcels.wrap(account));
					Nav.go(activity, AddAccountToListsFragment.class, args);
				}else if(id==R.id.copy_link){
					activity.getSystemService(ClipboardManager.class).setPrimaryClip(ClipData.newPlainText(null, item.status.url));
					UiUtils.maybeShowTextCopiedToast(activity);
				}else if(id==R.id.pin){
					new SetStatusPinned(item.status.id, !item.status.pinned)
							.setCallback(new Callback<>(){
								@Override
								public void onSuccess(Status result){
									item.status.pinned=!item.status.pinned;
									new Snackbar.Builder(activity)
											.setText(item.status.pinned ? R.string.post_pinned : R.string.post_unpinned)
											.show();
								}

								@Override
								public void onError(ErrorResponse error){
									error.showToast(activity);
								}
							})
							.wrapProgress(activity, R.string.loading, true)
							.exec(item.accountID);
				}else if(id==R.id.mute_conversation){
					new SetStatusConversationMuted(item.status.id, !item.status.muted)
							.setCallback(new Callback<>(){
								@Override
								public void onSuccess(Status result){
									// TODO snackbar?
									item.status.muted=result.muted;
								}

								@Override
								public void onError(ErrorResponse error){
									error.showToast(activity);
								}
							})
							.wrapProgress(activity, R.string.loading, true)
							.exec(item.accountID);
				}else if(id==R.id.change_quote_policy){
					ComposerVisibilitySheet sheet=new ComposerVisibilitySheet(activity, item.status.visibility, item.status.quoteApproval.toQuotePolicy(),
							false, StatusPrivacy.PUBLIC, item.accountID, (s, visibility, policy)->{
						new SetStatusInteractionPolicies(item.status.id, policy)
								.setCallback(new Callback<>(){
									@Override
									public void onSuccess(Status result){
										item.status.quoteApproval=result.quoteApproval;
										s.dismiss();
									}

									@Override
									public void onError(ErrorResponse error){
										error.showToast(activity);
									}
								})
								.wrapProgress(activity, R.string.loading, true)
								.exec(item.accountID);
						return false;
					});
					sheet.show();
				}else if(id==R.id.remove_quote){
					new M3AlertDialogBuilder(activity)
							.setTitle(R.string.remove_quote_confirm_title)
							.setMessage(R.string.remove_quote_confirm)
							.setPositiveButton(R.string.remove_quote_button, (dlg, which)->{
								new RevokeStatusQuote(item.status.quote.quotedStatus.id, item.status.id)
										.setCallback(new Callback<>(){
											@Override
											public void onSuccess(Status result){
												item.status.quote=result.quote;
												E.post(new StatusUpdatedEvent(item.status));
											}

											@Override
											public void onError(ErrorResponse error){
												error.showToast(activity);
											}
										})
										.wrapProgress(activity, R.string.loading, true)
										.exec(item.accountID);
							})
							.setNegativeButton(R.string.cancel, null)
							.show();
				}
				return true;
			});
		}

		@SuppressLint("SetTextI18n")
		@Override
		public void onBind(HeaderStatusDisplayItem item){
			name.setText(item.parsedName);
			String time;
			if(item.status==null || item.status.editedAt==null)
				time=UiUtils.formatRelativeTimestamp(itemView.getContext(), item.createdAt);
			else
				time=item.context.getString(R.string.edited_timestamp, UiUtils.formatRelativeTimestamp(itemView.getContext(), item.status.editedAt));

			timeAndUsername.setText(time+" Â· @"+item.user.acct);
			itemView.setPadding(itemView.getPaddingLeft(), itemView.getPaddingTop(), itemView.getPaddingRight(), item.needBottomPadding ? V.dp(6) : V.dp(4));
			if(TextUtils.isEmpty(item.extraText)){
				extraText.setVisibility(View.GONE);
			}else{
				extraText.setVisibility(View.VISIBLE);
				extraText.setText(item.extraText);
			}
			if(clickableThing!=null){
				clickableThing.setContentDescription(item.context.getString(R.string.avatar_description, item.user.acct));
			}
		}

		@Override
		public void setImage(int index, Drawable drawable){
			if(index>0){
				item.emojiHelper.setImageDrawable(index-1, drawable);
				name.invalidate();
			}else{
				avatar.setImageDrawable(drawable);
			}
			if(drawable instanceof Animatable)
				((Animatable) drawable).start();
		}

		@Override
		public void clearImage(int index){
			if(index==0){
				avatar.setImageResource(R.drawable.image_placeholder);
				return;
			}
			setImage(index, null);
		}

		private void onAvaClick(View v){
			Bundle args=new Bundle();
			args.putString("account", item.accountID);
			args.putParcelable("profileAccount", Parcels.wrap(item.user));
			Nav.go((Activity) item.context, ProfileFragment.class, args);
		}

		private void onMoreClick(View v){
			updateOptionsMenu();
			optionsMenu.show();
		}

		private void updateOptionsMenu(){
			if(item.context==null)
				return;
			Account account=item.user;
			Relationship relationship=item.callbacks.getRelationship(account.id);
			Menu menu=optionsMenu.getMenu();
			boolean isOwnPost=AccountSessionManager.getInstance().isSelf(item.accountID, account);
			boolean canTranslate=item.status!=null && item.status.getContentStatus().isEligibleForTranslation();
			MenuItem translate=menu.findItem(R.id.translate);
			translate.setVisible(canTranslate);
			if(canTranslate){
				if(item.status.translationState==Status.TranslationState.SHOWN)
					translate.setTitle(R.string.translation_show_original);
				else
					translate.setTitle(item.context.getString(R.string.translate_post, Locale.forLanguageTag(item.status.getContentStatus().language).getDisplayLanguage()));
			}
			menu.findItem(R.id.edit).setVisible(item.status!=null && isOwnPost);
			menu.findItem(R.id.delete).setVisible(item.status!=null && isOwnPost);
			menu.findItem(R.id.change_quote_policy).setVisible(item.status!=null && isOwnPost && item.status.quoteApproval!=null && (item.status.visibility==StatusPrivacy.PUBLIC || item.status.visibility==StatusPrivacy.UNLISTED));
			menu.findItem(R.id.open_in_browser).setVisible(item.status!=null);
			MenuItem mute=menu.findItem(R.id.mute);
			MenuItem block=menu.findItem(R.id.block);
			MenuItem report=menu.findItem(R.id.report);
			MenuItem follow=menu.findItem(R.id.follow);
			MenuItem bookmark=menu.findItem(R.id.bookmark);
			MenuItem pin=menu.findItem(R.id.pin);
			MenuItem muteConversation=menu.findItem(R.id.mute_conversation);
			MenuItem removeQuote=menu.findItem(R.id.remove_quote);
			if(item.status!=null){
				bookmark.setVisible(true);
				bookmark.setTitle(item.status.bookmarked ? R.string.remove_bookmark : R.string.add_bookmark);
				pin.setVisible(item.status.pinned!=null);
				if(item.status.pinned!=null){
					pin.setTitle(item.status.pinned ? R.string.unpin_post : R.string.pin_post);
				}
			}else{
				bookmark.setVisible(false);
				pin.setVisible(false);
				removeQuote.setVisible(false);
			}
			if(isOwnPost){
				mute.setVisible(false);
				block.setVisible(false);
				report.setVisible(false);
				follow.setVisible(false);
				removeQuote.setVisible(false);
			}else{
				mute.setVisible(true);
				block.setVisible(true);
				report.setVisible(true);

				String truncatedName=account.displayName.length()>20 ? (account.displayName.substring(0, 15)+"â€¦") : account.displayName;

				follow.setVisible(relationship==null || relationship.following || (!relationship.blocking && !relationship.blockedBy && !relationship.domainBlocking && !relationship.muting));
				mute.setTitle(item.context.getString(relationship!=null && relationship.muting ? R.string.unmute_user : R.string.mute_user, truncatedName));
				block.setTitle(item.context.getString(relationship!=null && relationship.blocking ? R.string.unblock_user : R.string.block_user, truncatedName));
				report.setTitle(item.context.getString(R.string.report_user, truncatedName));
				follow.setTitle(item.context.getString(relationship!=null && relationship.following ? R.string.unfollow_user : R.string.follow_user, truncatedName));

				if(item.status.quote!=null && item.status.quote.quotedStatus!=null && AccountSessionManager.getInstance().isSelf(item.accountID, item.status.quote.quotedStatus.account)){
					removeQuote.setVisible(true);
					removeQuote.setTitle(item.context.getString(R.string.remove_quote, truncatedName));
				}else{
					removeQuote.setVisible(false);
				}
			}
			if(item.status.muted!=null){
				muteConversation.setVisible(isOwnPost || item.callbacks instanceof NotificationsListFragment);
				muteConversation.setTitle(item.status.muted ? R.string.unmute_conversation : R.string.mute_conversation);
			}else{
				muteConversation.setVisible(false);
			}
			menu.findItem(R.id.add_to_list).setVisible(relationship!=null && relationship.following);
		}
	}
}
