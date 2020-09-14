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

public class ExitDialog extends DialogFragment implements View.OnClickListener {

    private TextView textView;
    private int dialogTitle;
    private Button positive,negative;
    private ExitDialogListener exitDialogListener;
    private Context context;
    private ImageView close;
    private static final String TAG = ExitDialog.class.getSimpleName();


    public ExitDialog(Context context, int dialogTitle){

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

    private void initViews(View view){

        textView = view.findViewById(R.id.dialog_text);
        positive = view.findViewById(R.id.dialog_button1);
        negative = view.findViewById(R.id.dialog_button2);
        close = view.findViewById(R.id.close_dialog);

        positive.setOnClickListener(this);
        negative.setOnClickListener(this);
        close.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        if(getActivity() != null && exitDialogListener != null){

            switch (v.getId()){
                case R.id.dialog_button1:
                    exitDialogListener.exit();
                    break;
                case R.id.dialog_button2:
                    exitDialogListener.dontExit();
                    break;
                case R.id.close_dialog:
                    break;
            }
        }

        dismiss();
    }

    private void showLogs(String msg) {
        Log.d(TAG,msg);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {

            exitDialogListener = (ExitDialogListener) getTargetFragment();

        }catch (ClassCastException e){

            FirebaseCrashlytics.getInstance().log("Custom Dialog Listener attach error");
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    @Override
    public void onDetach() {

        exitDialogListener = null;
        super.onDetach();
    }


    public interface ExitDialogListener{
        void exit();
        void dontExit();
    }
}
