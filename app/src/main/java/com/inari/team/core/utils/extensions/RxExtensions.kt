package com.inari.team.core.utils.extensions

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun Disposable.addToCompositeDisposable(composite: CompositeDisposable) {
    composite.add(this)
}