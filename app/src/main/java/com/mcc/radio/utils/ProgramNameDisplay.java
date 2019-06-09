package com.mcc.radio.utils;

import android.os.AsyncTask;

import com.mcc.radio.data.constant.AppConstants;
import com.mcc.radio.model.Program;
import com.mcc.radio.model.ProgramTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class ProgramNameDisplay extends AsyncTask<ArrayList<Program>, Void, String[]> {

    private ProgramNameDisplayListener listener;

    public void setListener(ProgramNameDisplayListener listener) {
        this.listener = listener;
    }


    @Override
    protected String[] doInBackground(ArrayList<Program>[] arrayLists) {

        for (Program program : arrayLists[0]) {
            ProgramTime programStartTime = AppUtility.getTime(program.getProgramStartTime());
            ProgramTime programEndTime = AppUtility.getTime(program.getProgramEndTime());

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
            Date date = new Date();
            String currentTime = sdf.format(date);

            try {
                Date currentHourMinute = sdf.parse(currentTime);
                Date startTime = sdf.parse(programStartTime.getTime());
                Date endTime = sdf.parse(programEndTime.getTime());
                if ((currentHourMinute.after(startTime)) && (currentHourMinute.before(endTime)) || currentHourMinute.equals(startTime) || currentHourMinute.equals(endTime)) {
                    //Log.d("ProgramTesting", program.getProgram_name());
                    return new String[]{program.getProgramName(), program.getProgramHostName()};
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    @Override
    protected void onPostExecute(String[] strings) {
        super.onPostExecute(strings);
        if (strings != null && strings.length != AppConstants.VALUE_ZERO) {
            listener.onProgramNameFound(strings[0], strings[1]);
        }
    }

    public interface ProgramNameDisplayListener {
        void onProgramNameFound(String programName, String hostName);
    }

}
