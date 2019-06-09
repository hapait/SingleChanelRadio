package com.mcc.radio.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.mcc.radio.R;
import com.mcc.radio.data.constant.AppConstants;
import com.mcc.radio.utils.AppUtility;
import com.mcc.radio.utils.MyAnimation;
import com.mcc.radio.utils.PermissionUtils;
import com.mcc.radio.utils.SliderAnimation;


public class BaseActivity extends AppCompatActivity {

    private Activity mActivity;
    private Snackbar mSnackBar;
    private int margin;
    protected boolean isProgramListControllerPressed = false;
    private Context mContext;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVariable();
        // uncomment this line to disable ad
        // AdUtils.getInstance(mContext).disableBannerAd();
        // AdUtils.getInstance(mContext).disableInterstitialAd();
    }

    private void initVariable() {
        mContext = getApplicationContext();
        mActivity = BaseActivity.this;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtils.isPermissionResultGranted(grantResults)) {
            if (requestCode == PermissionUtils.PERMISSION_REQUEST_CALLBACK_CONSTANT) {
                AppUtility.askAudioRecordPermission(mActivity);
            }
        } else {
            AppUtility.showToast(mActivity, "Permission not granted!");
        }
    }

    protected boolean isPlayerServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            String serviceName = getPackageName()+AppConstants.PLAYER_SERVICE_NAME;
            if (serviceName.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    protected void showOrHideProgramList(final boolean isPlayerRunning, final LinearLayout linearLayoutPlayerHolder, ImageView imgCollapseExpandArrow, RecyclerView recyclerView, View playerBodyTransparentView, final RelativeLayout relativeLayoutPlayerDiskHolder, final RelativeLayout relativeLayoutPlayerDisk, final ImageView imgPlayer) {

        margin = AppUtility.getMargin(getApplicationContext());
        Log.d("MarginTesting", ""+margin);
        if (!isProgramListControllerPressed) {
            if (isPlayerRunning) {
                MyAnimation.stopRotationAnimator();
            }
            Animation animation = new SliderAnimation(linearLayoutPlayerHolder, linearLayoutPlayerHolder.getHeight(), linearLayoutPlayerHolder.getHeight() / 2);
            animation.setInterpolator(new AccelerateInterpolator());
            animation.setDuration(800);
            linearLayoutPlayerHolder.setAnimation(animation);
            linearLayoutPlayerHolder.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    removeMarginWithAnimation(linearLayoutPlayerHolder, relativeLayoutPlayerDiskHolder, relativeLayoutPlayerDisk);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            isProgramListControllerPressed = true;
            MyAnimation.animateCollapse(imgCollapseExpandArrow);
            recyclerView.setVisibility(View.VISIBLE);
            playerBodyTransparentView.setVisibility(View.VISIBLE);

        } else {
            recyclerView.setVisibility(View.GONE);
            playerBodyTransparentView.setVisibility(View.GONE);

            Animation animation = new SliderAnimation(linearLayoutPlayerHolder, linearLayoutPlayerHolder.getHeight(), linearLayoutPlayerHolder.getHeight() * 2);
            animation.setInterpolator(new DecelerateInterpolator());
            animation.setDuration(500);
            linearLayoutPlayerHolder.setAnimation(animation);
            linearLayoutPlayerHolder.startAnimation(animation);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    setMarginWithAnimation(isPlayerRunning, linearLayoutPlayerHolder, margin, relativeLayoutPlayerDiskHolder, relativeLayoutPlayerDisk, imgPlayer);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            isProgramListControllerPressed = false;
            MyAnimation.animateExpand(imgCollapseExpandArrow);

        }
    }

    /**
     * This method will remove margin(110dp) from "rl_player_disk_holder" layout with animation;
     */
    public void removeMarginWithAnimation(final LinearLayout linearLayoutPlayerHolder, final RelativeLayout relativeLayoutPlayerDiskHolder, final RelativeLayout relativeLayoutPlayerDisk) {
        Animation animation1 = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {

                final RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) linearLayoutPlayerHolder.getLayoutParams();
                params1.bottomMargin = 0;
                linearLayoutPlayerHolder.setLayoutParams(params1);

                final RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) relativeLayoutPlayerDiskHolder.getLayoutParams();
                params2.topMargin = 0;
                relativeLayoutPlayerDiskHolder.setLayoutParams(params2);


                final RelativeLayout.LayoutParams params3 = (RelativeLayout.LayoutParams) relativeLayoutPlayerDisk.getLayoutParams();
                params3.topMargin = -150;
                relativeLayoutPlayerDisk.setLayoutParams(params3);
            }
        };
        animation1.setDuration(1000);
        animation1.setInterpolator(new OvershootInterpolator());
        relativeLayoutPlayerDiskHolder.startAnimation(animation1);
        relativeLayoutPlayerDisk.startAnimation(animation1);
    }

    /**
     * This method will set margin to "rl_player_disk_holder" layout with animation;
     */
    public void setMarginWithAnimation(final boolean isPlayerRunning,final LinearLayout linearLayoutPlayerHolder, final int margin, final RelativeLayout relativeLayoutPlayerDiskHolder, final RelativeLayout relativeLayoutPlayerDisk, final ImageView imgPlayer) {
        Animation animation1 = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {

                final RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) linearLayoutPlayerHolder.getLayoutParams();
                params1.bottomMargin = 80;
                linearLayoutPlayerHolder.setLayoutParams(params1);

                final RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) relativeLayoutPlayerDiskHolder.getLayoutParams();
                Log.d("HeightTesting", " " + margin);
                params2.topMargin = (int) (margin * interpolatedTime);
                relativeLayoutPlayerDiskHolder.setLayoutParams(params2);

                final RelativeLayout.LayoutParams params3 = (RelativeLayout.LayoutParams) relativeLayoutPlayerDisk.getLayoutParams();
                params3.topMargin = 0;
                relativeLayoutPlayerDisk.setLayoutParams(params3);


            }
        };
        animation1.setDuration(1000);
        animation1.setInterpolator(new OvershootInterpolator());
        relativeLayoutPlayerDiskHolder.startAnimation(animation1);
        relativeLayoutPlayerDisk.startAnimation(animation1);
        animation1.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (isPlayerRunning) {
                    MyAnimation.rotationAnimator(imgPlayer);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }


    public void showSnackBar() {
        RelativeLayout mainLayout = findViewById(R.id.main_layout);
        mSnackBar = Snackbar.make(mainLayout, getString(R.string.net_work_not_available), Snackbar.LENGTH_LONG)
                .setAction("Connect", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                    }
                });
        mSnackBar.setDuration(3000)
                .show();
    }

    public void hideSnackBar() {
        if (mSnackBar != null && mSnackBar.isShown()) {
            mSnackBar.dismiss();
        }
    }


    protected void registerReceivers(BroadcastReceiver mInternetConnectivityChangeReceiver, BroadcastReceiver myBroadCastReceiver) {
        LocalBroadcastManager.getInstance(this).registerReceiver(myBroadCastReceiver, new IntentFilter(AppConstants.MY_BROADCAST_RECEIVER));
        registerReceiver(mInternetConnectivityChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    protected void unRegisterReceivers(BroadcastReceiver mInternetConnectivityChangeReceiver, BroadcastReceiver myBroadCastReceiver) {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myBroadCastReceiver);
        unregisterReceiver(mInternetConnectivityChangeReceiver);
    }
}
