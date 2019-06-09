package com.mcc.radio.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.mcc.radio.R;
import com.mcc.radio.data.sqlite.ProgramDbController;
import com.mcc.radio.model.Program;


public class ActivityUtils {

    private static ActivityUtils sActivityUtils = null;

    public static ActivityUtils getInstance() {
        if (sActivityUtils == null) {
            sActivityUtils = new ActivityUtils();
        }
        return sActivityUtils;
    }

    public void invokeActivity(Activity activity, Class<?> tClass, boolean shouldFinish) {
        Intent intent = new Intent(activity, tClass);
        activity.startActivity(intent);
        if (shouldFinish) {
            activity.finish();
        }
    }

    public static boolean isProgramAlarmAlreadySet(Context mContext, String programId) {
        try {
            ProgramDbController favouriteController = new ProgramDbController(mContext);
            favouriteController.open();
            boolean addedToFavouriteList = favouriteController.isAlreadyFavourite(programId);
            favouriteController.close();
            return addedToFavouriteList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void setProgramAsFavorite(Context mContext, Program program) {

        try {
            ProgramDbController favouriteController = new ProgramDbController(mContext);
            favouriteController.open();
            favouriteController.insertFavouriteItem(program.getProgramId(), program.getProgramName(), program.getProgramHostName(), program.getProgramStartTime(), program.getProgramEndTime(), program.getProgramDuration());
            favouriteController.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeProgramFromFavorite(Context mContext, String productId){
        try {
            ProgramDbController favouriteController = new ProgramDbController(mContext);
            favouriteController.open();
            favouriteController.deleteFavouriteItemById(Integer.parseInt(productId));
            favouriteController.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showAllPrograms(Context mContext) {
        try {
            ProgramDbController favouriteController = new ProgramDbController(mContext);
            favouriteController.open();
            favouriteController.getAllProgramData();
            favouriteController.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    public static void shareAppLink(Context mContext){
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, mContext.getString(R.string.app_url));
        sendIntent.setType("text/plain");
        mContext.startActivity(sendIntent);
    }

}
