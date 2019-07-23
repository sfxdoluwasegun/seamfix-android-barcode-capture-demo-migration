package com.seamfix.qrcode.rest

import androidx.annotation.Keep
import retrofit2.Call
import com.google.gson.JsonObject
import retrofit2.http.*


/**
 *  This class is used to define the methods for the Face match service
 *  @author Biose, Nonso Emmanuel
 *  @since 12-06-2019
 */
@Keep
interface FaceMatchService {

    @Headers("Content-Type: application/json")
    @POST
    fun matchFace(@Header("X-API-KEY") key: String, @Url path: String, @Body face: JsonObject): Call<JsonObject>

}