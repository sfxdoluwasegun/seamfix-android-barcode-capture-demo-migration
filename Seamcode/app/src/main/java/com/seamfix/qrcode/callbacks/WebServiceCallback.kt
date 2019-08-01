package com.seamfix.qrcode.callbacks

import androidx.annotation.Keep

/**
 *  This class is a tag for all callbacks
 *  @author Biose, Nonso Emmanuel
 *  @since 09-06-2019
 */
@Keep
abstract class WebServiceCallback {

    abstract fun onWebServiceInactive()
}