package com.suhel.imagine.util

import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

fun <T> weakRefOf(obj: T) = WeakReferenceDelegate(obj)

class WeakReferenceDelegate<T>(obj: T) {
    private val ref: WeakReference<T> = WeakReference(obj)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = ref.get()
}