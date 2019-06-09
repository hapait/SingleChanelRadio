package com.mcc.radio.model;

public class ProgramTime {

    private String alarmTime;
    private String time;
    private String day;

    public ProgramTime(String alarmTime, String time, String day) {
        this.alarmTime = alarmTime;
        this.time = time;
        this.day = day;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

}
