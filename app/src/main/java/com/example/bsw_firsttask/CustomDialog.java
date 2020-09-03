package com.example.bsw_firsttask;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.bsw_firsttask.Callbacks.NativeAdCallback;
import com.example.bsw_firsttask.Factory.Constants;
import com.example.bsw_firsttask.Fragments.GameScreen;
import com.example.bsw_firsttask.Fragments.HomeScreen;
import com.facebook.ads.Ad;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

public class CustomDialog extends DialogFragment implements View.OnClickListener, NativeAdCallback {

    private TextView textView;
    private int dialogTitle;
    private Button positive,negative;
    private DialogListener listener;
    private Context context;
    private ImageView close;
    private UnifiedNativeAdView unifiedNativeAdView;
    private LinearLayout linearLayout;
    private UnifiedNativeAd unifiedNativeAd;
    private AdMobHandler adMobHandler;
    private NativeAdCallback nativeAdCallback;

    public CustomDialog() {
    }

    public CustomDialog(Context context, int dialogTitle){

        this.context = context;
        this.dialogTitle = dialogTitle;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.custom_dialogs,container,false);


        initViews(view);
        textView.setText(context.getResources().getText(dialogTitle));
        positive.setText(getContext().getResources().getText(R.string.yes));
        negative.setText(getContext().getResources().getText(R.string.no));
        adMobHandler = AdMobHandler.getInstance(getActivity());
        adMobHandler.setNativeAdCallback(nativeAdCallback);

        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        test();

        return view;
    }

    private void test(){

        unifiedNativeAdView = (UnifiedNativeAdView) getActivity().getLayoutInflater().inflate(R.layout.native_ad_layout,null);

        unifiedNativeAd = adMobHandler.getUnifiedNativeAd();

        Log.i("Custom Dialog","  " + unifiedNativeAd);

        Log.i("Custom Dialog","  " + unifiedNativeAdView);


        if(unifiedNativeAd != null && unifiedNativeAdView != null)
            populateNativeAdView(unifiedNativeAd,unifiedNativeAdView);
    }

    private void populateNativeAdView(UnifiedNativeAd unifiedNativeAd, UnifiedNativeAdView adView){

        Log.i("Custom Dialog","Inside Populate Native Ad");

        MediaView mediaView = adView.findViewById(R.id.ad_media);
        mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setStarRatingView(adView.findViewById(R.id.ad_ratingbar));
        adView.setMediaView((MediaView) adView.findViewById(R.id.ad_media));
        adView.setCallToActionView(adView.findViewById(R.id.ad_button));
        adView.setIconView(adView.findViewById(R.id.ad_icon));


        if (adView.getHeadlineView() != null && unifiedNativeAd.getHeadline() != null) {
            ((TextView) adView.getHeadlineView()).setText(unifiedNativeAd.getHeadline());
        }

        if (unifiedNativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.GONE);
        }else{
            ((RatingBar)adView.getStarRatingView()).setRating(unifiedNativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if(unifiedNativeAd.getMediaContent() == null)
            adView.getMediaView().setVisibility(View.INVISIBLE);
        else{
            adView.getMediaView().setMediaContent(unifiedNativeAd.getMediaContent());
            adView.getMediaView().setVisibility(View.VISIBLE);
        }

        if(unifiedNativeAd.getIcon() == null)
            adView.getIconView().setVisibility(View.GONE);
        else{
            ((ImageView)adView.getIconView()).setImageDrawable(unifiedNativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if(unifiedNativeAd.getCallToAction() ==null)
            adView.getCallToActionView().setVisibility(View.GONE);
        else{
            ((TextView)adView.getCallToActionView()).setText(unifiedNativeAd.getCallToAction());
        }

        adView.setNativeAd(unifiedNativeAd);

        linearLayout.addView(adView);

        if(getTargetFragment() instanceof GameScreen)
            linearLayout.setVisibility(View.VISIBLE);
    }


    private void initViews(View view){

        textView = view.findViewById(R.id.dialog_text);
        positive = view.findViewById(R.id.dialog_button1);
        negative = view.findViewById(R.id.dialog_button2);
        close = view.findViewById(R.id.close_dialog);

        linearLayout = view.findViewById(R.id.dialog_linear_layout);

        positive.setOnClickListener(this);
        negative.setOnClickListener(this);
        close.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.dialog_button1:
                listener.positive();
                break;
            case R.id.dialog_button2:
                listener.negative();
                break;
            case R.id.close_dialog:
                listener.close();
                break;
        }

        getDialog().dismiss();

    }

    @Override
    public void onResume() {
        super.onResume();

        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_BACK) {

                    // check for TARGET FRAGMENT = GAME SCREEN
                    if(getTargetFragment() instanceof GameScreen){

                        Toast.makeText(getContext(),"Press the Grey Color to Resume",Toast.LENGTH_LONG).show();

                        ((GameScreen) getTargetFragment()).restartHandler();
                        dialog.dismiss();

                    }else if(getTargetFragment() instanceof HomeScreen){

                        dialog.dismiss();
                    }
                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {

            listener = (DialogListener) getTargetFragment();

        }catch (ClassCastException e){
            System.out.print(e.getMessage());
        }
    }

    @Override
    public void onDetach() {

        listener = null;
        super.onDetach();
    }

    @Override
    public void nativeAd(UnifiedNativeAd unifiedNativeAd) {
        this.unifiedNativeAd = unifiedNativeAd;

    }

    @Override
    public void onAdOpen() {

    }

    @Override
    public void onAdClosed() {

    }

    @Override
    public void onAdLoaded() {

    }

    @Override
    public void onAdFailedToLoad() {

    }

    public interface DialogListener{

        void positive();
        void negative();
        void close();
    }

}