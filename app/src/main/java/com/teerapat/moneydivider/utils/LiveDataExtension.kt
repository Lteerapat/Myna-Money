package com.teerapat.moneydivider.utils

import com.hadilq.liveevent.LiveEvent

fun LiveEvent<Unit>.action() {
    value = Unit
}

fun <T> LiveEvent<T>.action(data: T) {
    value = data
}