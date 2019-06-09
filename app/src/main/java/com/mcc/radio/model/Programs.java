package com.mcc.radio.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Admin on 23-Apr-18.
 */

public class Programs {
    @SerializedName("Programs")
    @Expose
    private List<Program> programs = null;

    public List<Program> getPrograms() {
        return programs;
    }

    public void setPrograms(List<Program> programs) {
        this.programs = programs;
    }

}
