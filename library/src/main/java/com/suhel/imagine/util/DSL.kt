package com.suhel.imagine.util

import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

/**
 * Create delegated read-only nullable property backed by a [WeakReference] to
 * a passed object
 *
 * @param obj The object to wrap in a [WeakReference]
 */
internal fun <T> weakRefTo(obj: T) = WeakReferenceDelegate(obj)

/**
 * Delegated read-only nullable property wrapper that wraps an object inside a [WeakReference]
 *
 * @param obj The object to wrap in a [WeakReference]
 */
internal class WeakReferenceDelegate<T>(obj: T) {
    private val ref: WeakReference<T> = WeakReference(obj)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = ref.get()
}