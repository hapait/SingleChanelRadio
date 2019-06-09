package com.mcc.radio.network;

import com.mcc.radio.model.Programs;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Admin on 18-Feb-18.
 */

public interface ApiInterface {
    @GET(HttpParams.SHEET_API_END_POINT)
    Call<Programs> getProgramList(@Query("id") String sheetId, @Query("sheet") String sheetName);
}
