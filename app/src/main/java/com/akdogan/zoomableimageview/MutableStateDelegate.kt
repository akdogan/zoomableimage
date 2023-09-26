package com.akdogan.zoomableimageview

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class MutableStateDelegate<T, V>(initialValue: V) : ReadWriteProperty<T, V> {
    private val state: MutableState<V> = mutableStateOf(initialValue)

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        return state.value
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        state.value = value
    }
}

fun <T> mutableState(initialValue: T): ReadWriteProperty<Any, T> {
    return MutableStateDelegate(initialValue)
}