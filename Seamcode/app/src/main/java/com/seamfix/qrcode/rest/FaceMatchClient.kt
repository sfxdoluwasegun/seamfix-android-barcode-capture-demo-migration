package com.seamfix.qrcode.rest

import android.os.CountDownTimer
import android.util.Log
import androidx.annotation.Keep
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import com.seamfix.qrcode.callbacks.WebServiceCallback
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 *  This class obtains the Retrofit client for api calls
 *  @author Biose, Nonso Emmanuel
 *  @since 11-06-2019
 */
@Keep
class FaceMatchClient {

    companion object {

        private var RETROFIT_INSTANCE: Retrofit? = null
        private lateinit var webServiceCountdownTimer: CountDownTimer

        /**
         * This method is used to obtain an instance of Retrofit2
         *
         * @return Retrofit
         */
        fun getRetrofitInstance(): Retrofit? {

            if (RETROFIT_INSTANCE == null) {

                val logging = HttpLoggingInterceptor()
                logging.level = HttpLoggingInterceptor.Level.BASIC

                val client = OkHttpClient.Builder()
                    .readTimeout(1, TimeUnit.MINUTES)
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .addInterceptor(logging)
                    .build()

                RETROFIT_INSTANCE = Retrofit.Builder()
                    .baseUrl("http://logs.seamfix.com:9293/pred_client/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return RETROFIT_INSTANCE
        }

        /**
         * This method counts down from 1 minute, any call without a response, is terminated
         * @param callback of Web service to be notified
         */
        fun startWebServiceCountdownTimer(callback: WebServiceCallback) {

            Log.e(FaceMatchClient::class.simpleName, "CountDownTimer Started !!!")

            webServiceCountdownTimer = object : CountDownTimer(60000, 1000) {

                override fun onTick(millisUntilFinished: Long) {
                    /* Do nothing here*/
                }

                override fun onFinish() {
                    callback.onWebServiceInactive()
                }
            }.start()
        }

        /**
         * Stop the web service countdown timer
         */
        fun stopWebServiceCountdownTimer() {
            Log.e(FaceMatchClient::class.simpleName, "CountDownTimer Stopped !!!")
            webServiceCountdownTimer.cancel()
        }
    }
}