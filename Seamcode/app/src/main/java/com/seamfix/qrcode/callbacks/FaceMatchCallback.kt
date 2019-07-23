package com.seamfix.qrcode.callbacks

import androidx.annotation.Keep

/**
 *  This class is used notify about Face match events
 *  @author Biose, Nonso Emmanuel
 *  @since 09-06-2019
 */
@Keep
abstract class FaceMatchCallback: WebServiceCallback() {

    abstract fun onFaceMatchResponse(isMatch: Boolean)

    abstract fun onFaceMatchError(message: String)
}