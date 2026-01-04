package app.kabinka.social.fragments;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.widget.ProgressBar;

import app.kabinka.social.MastodonApp;
import app.kabinka.social.R;
import app.kabinka.social.api.MastodonErrorResponse;
import app.kabinka.social.api.requests.accounts.CheckInviteLink;
import app.kabinka.social.api.requests.catalog.GetCatalogDefaultInstances;
import app.kabinka.social.api.session.AccountSessionManager;
import app.kabinka.social.fragments.onboarding.InstanceCatalogSignupFragment;
import app.kabinka.social.fragments.onboarding.InstanceChooserLoginFragment;
import app.kabinka.social.fragments.onboarding.InstanceRulesFragment;
import app.kabinka.social.model.Instance;
import app.kabinka.social.model.catalog.CatalogDefaultInstance;
import app.kabinka.social.ui.InterpolatingMotionEffect;
import app.kabinka.social.ui.M3AlertDialogBuilder;
import app.kabinka.social.ui.text.HtmlParser;
import app.kabinka.social.ui.utils.UiUtils;
import app.kabinka.social.ui.views.ProgressBarButton;
import app.kabinka.social.ui.views.SizeListenerFrameLayout;
import org.parceler.Parcels;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import androidx.annotation.Nullable;
import me.grishka.appkit.Nav;
import me.grishka.appkit.api.Callback;
import me.grishka.appkit.api.ErrorResponse;
import me.grishka.appkit.fragments.AppKitFragment;
import me.grishka.appkit.utils.V;
import me.grishka.appkit.views.BottomSheet;

public class SplashFragment extends AppKitFragment{

	private static final String DEFAULT_SERVER="mastodon.social";

	private SizeListenerFrameLayout contentView;
	private View artContainer, blueFill, greenFill;
	private InterpolatingMotionEffect motionEffect;
	private View artClouds, artPlaneElephant, artRightHill, artLeftHill, artCenterHill;
	private ProgressBarButton defaultServerButton;
	private ProgressBar defaultServerProgress;
	private String chosenDefaultServer=DEFAULT_SERVER;
	private boolean loadingDefaultServer, loadedDefaultServer;
	private Uri currentInviteLink;
	private ProgressDialog instanceLoadingProgress;
	private String inviteCode;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		motionEffect=new InterpolatingMotionEffect(MastodonApp.context);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState){
		Log.d("SplashFragment", "onCreateView called");
		contentView=(SizeListenerFrameLayout) inflater.inflate(R.layout.fragment_splash, container, false);
		
		View btnGetStarted = contentView.findViewById(R.id.btn_get_started);
		View btnLogIn = contentView.findViewById(R.id.btn_log_in);
		defaultServerButton=contentView.findViewById(R.id.btn_join_default_server);
		View btnLearnMore = contentView.findViewById(R.id.btn_learn_more);
		
		Log.d("SplashFragment", "Button views: btnGetStarted=" + (btnGetStarted != null) + 
			", btnLogIn=" + (btnLogIn != null) + 
			", defaultServerButton=" + (defaultServerButton != null) + 
			", btnLearnMore=" + (btnLearnMore != null));
		
		btnGetStarted.setOnClickListener(this::onButtonClick);
		btnLogIn.setOnClickListener(this::onButtonClick);
		defaultServerButton.setText(getString(R.string.join_default_server, chosenDefaultServer));
		defaultServerButton.setOnClickListener(this::onJoinDefaultServerClick);
		defaultServerProgress=contentView.findViewById(R.id.action_progress);
		if(loadingDefaultServer){
			defaultServerButton.setTextVisible(false);
			defaultServerProgress.setVisibility(View.VISIBLE);
		}
		contentView.findViewById(R.id.btn_learn_more).setOnClickListener(this::onLearnMoreClick);
		
		Log.d("SplashFragment", "All click listeners set successfully");

		artClouds=contentView.findViewById(R.id.art_clouds);
		artPlaneElephant=contentView.findViewById(R.id.art_plane_elephant);
		artRightHill=contentView.findViewById(R.id.art_right_hill);
		artLeftHill=contentView.findViewById(R.id.art_left_hill);
		artCenterHill=contentView.findViewById(R.id.art_center_hill);

		artContainer=contentView.findViewById(R.id.art_container);
		blueFill=contentView.findViewById(R.id.blue_fill);
		greenFill=contentView.findViewById(R.id.green_fill);
		motionEffect.addViewEffect(new InterpolatingMotionEffect.ViewEffect(artClouds, V.dp(-5), V.dp(5), V.dp(-5), V.dp(5)));
		motionEffect.addViewEffect(new InterpolatingMotionEffect.ViewEffect(artRightHill, V.dp(-15), V.dp(25), V.dp(-10), V.dp(10)));
		motionEffect.addViewEffect(new InterpolatingMotionEffect.ViewEffect(artLeftHill, V.dp(-25), V.dp(15), V.dp(-15), V.dp(15)));
		motionEffect.addViewEffect(new InterpolatingMotionEffect.ViewEffect(artCenterHill, V.dp(-14), V.dp(14), V.dp(-5), V.dp(25)));
		motionEffect.addViewEffect(new InterpolatingMotionEffect.ViewEffect(artPlaneElephant, V.dp(-20), V.dp(12), V.dp(-20), V.dp(12)));
		artContainer.setOnTouchListener(motionEffect);

		contentView.setSizeListener(new SizeListenerFrameLayout.OnSizeChangedListener(){
			@Override
			public void onSizeChanged(int w, int h, int oldw, int oldh){
				contentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener(){
					@Override
					public boolean onPreDraw(){
						contentView.getViewTreeObserver().removeOnPreDrawListener(this);
						updateArtSize(w, h);
						return true;
					}
				});
			}
		});
		if(!loadedDefaultServer && !loadingDefaultServer)
			loadAndChooseDefaultServer();

		return contentView;
	}

	private void onButtonClick(View v){
		Log.d("SplashFragment", "onButtonClick called, view ID: " + v.getId());
		Bundle extras=new Bundle();
		boolean isSignup=v.getId()==R.id.btn_get_started;
		extras.putBoolean("signup", isSignup);
		extras.putString("defaultServer", chosenDefaultServer);
		Log.d("SplashFragment", "Navigating to " + (isSignup ? "InstanceCatalogSignupFragment" : "InstanceChooserLoginFragment"));
		Nav.go(getActivity(), isSignup ? InstanceCatalogSignupFragment.class : InstanceChooserLoginFragment.class, extras);
	}

	private void onJoinDefaultServerClick(View v){
		try {
			Log.d("SplashFragment", "onJoinDefaultServerClick called - chosenDefaultServer: " + chosenDefaultServer);
			Log.d("SplashFragment", "loadingDefaultServer: " + loadingDefaultServer);
			Log.d("SplashFragment", "currentInviteLink: " + currentInviteLink);
			
			if(loadingDefaultServer){
				Log.d("SplashFragment", "Still loading default server, returning early");
				return;
			}
			
			Log.d("SplashFragment", "Creating progress dialog");
			instanceLoadingProgress=new ProgressDialog(getActivity());
			instanceLoadingProgress.setCancelable(false);
			instanceLoadingProgress.setMessage(getString(R.string.loading_instance));
			instanceLoadingProgress.show();
			
			if(currentInviteLink!=null){
				Log.d("SplashFragment", "Processing invite link: " + currentInviteLink);
				new CheckInviteLink(currentInviteLink.getPath())
					.setCallback(new Callback<>(){
						@Override
						public void onSuccess(CheckInviteLink.Response result){
							inviteCode=result.inviteCode;
							proceedWithServerDomain(currentInviteLink.getHost());
						}

						@Override
						public void onError(ErrorResponse error){
							if(getActivity()==null)
								return;
							if(instanceLoadingProgress!=null)
								instanceLoadingProgress.dismiss();
							instanceLoadingProgress=null;
							if(error instanceof MastodonErrorResponse mer){
								switch(mer.httpStatus){
									case 401 -> new M3AlertDialogBuilder(getActivity())
											.setTitle(R.string.expired_invite_link)
											.setMessage(getString(R.string.expired_clipboard_invite_link_alert, currentInviteLink.getHost(), chosenDefaultServer))
											.setPositiveButton(R.string.ok, null)
											.show();
									case 404 -> new M3AlertDialogBuilder(getActivity())
											.setTitle(R.string.invalid_invite_link)
											.setMessage(getString(R.string.invalid_clipboard_invite_link_alert, currentInviteLink.getHost(), chosenDefaultServer))
											.setPositiveButton(R.string.ok, null)
											.show();
									default -> error.showToast(getActivity());
								}
							}
						}
					})
					.execNoAuth(currentInviteLink.getHost());
			return;
		}
		Log.d("SplashFragment", "Proceeding with server domain: " + chosenDefaultServer);
		proceedWithServerDomain(chosenDefaultServer);
		} catch (Exception e) {
			Log.e("SplashFragment", "Exception in onJoinDefaultServerClick", e);
			if(getActivity() != null && instanceLoadingProgress != null) {
				instanceLoadingProgress.dismiss();
				instanceLoadingProgress = null;
				new M3AlertDialogBuilder(getActivity())
					.setTitle(R.string.error)
					.setMessage("Unexpected error: " + e.getMessage())
					.setPositiveButton(R.string.ok, null)
					.show();
			}
		}
	}

	private void proceedWithServerDomain(String domain){
		Log.d("SplashFragment", "proceedWithServerDomain called with domain: " + domain);
		AccountSessionManager.loadInstanceInfo(domain, new Callback<>(){
					@Override
					public void onSuccess(Instance result){
						if(getActivity()==null)
							return;
						if(instanceLoadingProgress!=null)
							instanceLoadingProgress.dismiss();
						instanceLoadingProgress=null;
						if(!result.areRegistrationsOpen() && TextUtils.isEmpty(inviteCode)){
							new M3AlertDialogBuilder(getActivity())
									.setTitle(R.string.error)
									.setMessage(R.string.instance_signup_closed)
									.setPositiveButton(R.string.ok, null)
									.show();
							return;
						}
						try {
							Bundle args=new Bundle();
							args.putParcelable("instance", Parcels.wrap(result));
							if(inviteCode!=null)
								args.putString("inviteCode", inviteCode);
							Log.d("SplashFragment", "Navigating to InstanceRulesFragment with args: " + args);
							Nav.go(getActivity(), InstanceRulesFragment.class, args);
						} catch (Exception e) {
							Log.e("SplashFragment", "Error navigating to InstanceRulesFragment", e);
							if(getActivity() != null) {
								new M3AlertDialogBuilder(getActivity())
										.setTitle(R.string.error)
										.setMessage("Navigation error: " + e.getMessage())
										.setPositiveButton(R.string.ok, null)
										.show();
							}
						}
					}

					@Override
					public void onError(ErrorResponse error){
						if(getActivity()==null)
							return;
						if(instanceLoadingProgress!=null)
							instanceLoadingProgress.dismiss();
						instanceLoadingProgress=null;
						error.showToast(getActivity());
					}
				});
	}

	private void onLearnMoreClick(View v){
		Log.d("SplashFragment", "onLearnMoreClick called");
		View sheetView=getActivity().getLayoutInflater().inflate(R.layout.intro_bottom_sheet, null);
		BottomSheet sheet=new BottomSheet(getActivity());
		sheet.setContentView(sheetView);
		sheet.setNavigationBarBackground(new ColorDrawable(UiUtils.alphaBlendColors(UiUtils.getThemeColor(getActivity(), R.attr.colorM3Surface),
				UiUtils.getThemeColor(getActivity(), R.attr.colorM3Primary), 0.05f)), !UiUtils.isDarkTheme());
		sheet.show();
	}

	private void updateArtSize(int w, int h){
		float scale=w/(float)V.dp(360);
		artContainer.setScaleX(scale);
		artContainer.setScaleY(scale);
		blueFill.setScaleY(artContainer.getBottom()-V.dp(90));
		greenFill.setScaleY(h-artContainer.getBottom()+V.dp(90));
	}


	@Override
	public void onApplyWindowInsets(WindowInsets insets){
		super.onApplyWindowInsets(insets);
		int bottomInset=insets.getSystemWindowInsetBottom();
		if(bottomInset>0 && bottomInset<V.dp(36)){
			contentView.setPadding(contentView.getPaddingLeft(), contentView.getPaddingTop(), contentView.getPaddingRight(), V.dp(36));
		}
		((ViewGroup.MarginLayoutParams)blueFill.getLayoutParams()).topMargin=-contentView.getPaddingTop();
		((ViewGroup.MarginLayoutParams)greenFill.getLayoutParams()).bottomMargin=-contentView.getPaddingBottom();
	}

	@Override
	public boolean wantsLightStatusBar(){
		return true;
	}

	@Override
	public boolean wantsLightNavigationBar(){
		return false;
	}

	@Override
	protected void onShown(){
		super.onShown();
		motionEffect.activate();
	}

	@Override
	protected void onHidden(){
		super.onHidden();
		motionEffect.deactivate();
	}

	private void loadAndChooseDefaultServer(){
		ClipData clipData=getActivity().getSystemService(ClipboardManager.class).getPrimaryClip();
		if(clipData!=null && clipData.getItemCount()>0){
			String clipText=clipData.getItemAt(0).coerceToText(getActivity()).toString();
			if(HtmlParser.isValidInviteUrl(clipText)){
				currentInviteLink=Uri.parse(clipText);
				defaultServerButton.setText(getString(R.string.join_server_x_with_invite, HtmlParser.normalizeDomain(Objects.requireNonNull(currentInviteLink.getHost()))));
			}
		}else{
			loadingDefaultServer=true;
			defaultServerButton.setTextVisible(false);
			defaultServerProgress.setVisibility(View.VISIBLE);
		}
		new GetCatalogDefaultInstances()
				.setCallback(new Callback<>(){
					@Override
					public void onSuccess(List<CatalogDefaultInstance> result){
						if(result.isEmpty()){
							setChosenDefaultServer(DEFAULT_SERVER);
							return;
						}
						float sum=0f;
						for(CatalogDefaultInstance inst:result){
							sum+=inst.weight;
						}
						if(sum<=0)
							sum=1f;
						for(CatalogDefaultInstance inst:result){
							inst.weight/=sum;
						}
						float rand=ThreadLocalRandom.current().nextFloat();
						float prev=0f;
						for(CatalogDefaultInstance inst:result){
							if(rand>=prev && rand<prev+inst.weight){
								setChosenDefaultServer(inst.domain);
								return;
							}
							prev+=inst.weight;
						}
						// Just in case something didn't add up
						setChosenDefaultServer(result.get(result.size()-1).domain);
					}

					@Override
					public void onError(ErrorResponse error){
						setChosenDefaultServer(DEFAULT_SERVER);
					}
				})
				.execNoAuth("");
	}

	private void setChosenDefaultServer(String domain){
		chosenDefaultServer=domain;
		loadingDefaultServer=false;
		loadedDefaultServer=true;
		if(defaultServerButton!=null && getActivity()!=null && currentInviteLink==null){
			defaultServerButton.setTextVisible(true);
			defaultServerProgress.setVisibility(View.GONE);
			defaultServerButton.setText(getString(R.string.join_default_server, HtmlParser.normalizeDomain(chosenDefaultServer)));
		}
	}
}
