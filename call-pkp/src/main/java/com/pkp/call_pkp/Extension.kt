package com.pkp.call_pkp

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.observeOnce(observer: Observer<T>) {
    observeForever(object : Observer<T> {

        override fun onChanged(value: T) {
            observer.onChanged(value)
            removeObserver(this)
        }
    })

    /*
    observeForever(object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
     */
}