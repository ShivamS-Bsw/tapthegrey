package com.example.bsw_firsttask.Dialogs;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.bsw_firsttask.AdMob.AdMobHandler;
import com.example.bsw_firsttask.Fragments.GameScreen;
import com.example.bsw_firsttask.R;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

public class CustomDialog extends DialogFragment implements View.OnClickListener {

    private TextView textView;
    private int dialogTitle;
    private Button positive,negative;
    private DialogListener listener;
    private Context context;
    private ImageView close;
    private UnifiedNativeAdView unifiedNativeAdView;
    private LinearLayout linearLayout;
    private UnifiedNativeAd unifiedNativeAd;
    private static final String TAG = CustomDialog.class.getSimpleName();
    private DialogLifecycleListener lifecycleListener;


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

        if(context != null && getActivity() != null ){

            textView.setText(context.getResources().getText(dialogTitle));
            positive.setText(getContext().getResources().getText(R.string.yes));
            negative.setText(getContext().getResources().getText(R.string.no));
        }

        if(savedInstanceState != null){

            String title = savedInstanceState.getString("dialog_title");
            textView.setText(title);
        }



        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        if(AdMobHandler.getInstance(getActivity()).showNativeAd())
            showNativeAd();

        return view;
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("dialog_title",textView.getText().toString());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(getDialog() != null){

            getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

                    if(keyCode == KeyEvent.KEYCODE_BACK){

                        dialog.dismiss();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        if(lifecycleListener != null)
            lifecycleListener.onDismiss();
    }

    private void showNativeAd(){

        if(getActivity() != null) {

            unifiedNativeAdView = (UnifiedNativeAdView) getActivity().getLayoutInflater().inflate(R.layout.native_ad_layout,null);
            unifiedNativeAd = AdMobHandler.getInstance(getActivity()).getUnifiedNativeAd();

            Log.i("Custom Dialog","  " + unifiedNativeAd);

            Log.i("Custom Dialog","  " + unifiedNativeAdView);


            if(unifiedNativeAd != null && unifiedNativeAdView != null)
                populateNativeAdView(unifiedNativeAd,unifiedNativeAdView);
        }
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

        if(getActivity() != null && listener != null){

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
        }

        dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();

        if(lifecycleListener != null)
            lifecycleListener.OnDialogResume();

        showLogs("On Resume");
    }

    @Override
    public void onPause() {
        super.onPause();

        if(lifecycleListener != null)
            lifecycleListener.OnDialogPause();

        showLogs("On Pause");
    }

    @Override
    public void onStop() {
        super.onStop();

        showLogs("On Stop");
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
    }

    private void showLogs(String msg) {
        Log.d(TAG,msg);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {

           listener = (DialogListener) getTargetFragment();
            lifecycleListener = (DialogLifecycleListener) getTargetFragment();

        }catch (ClassCastException e){

            FirebaseCrashlytics.getInstance().log("Custom Dialog Listener attach error");
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    @Override
    public void onDetach() {

        listener = null;
        lifecycleListener = null;
        super.onDetach();
    }

    public interface DialogListener{

        void positive();
        void negative();
        void close();

    }

    public interface DialogLifecycleListener{

        void OnDialogResume();
        void OnDialogPause();
        void onDismiss();
    }
}
