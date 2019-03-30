package com.inari.team.core.utils.extensions

import android.arch.lifecycle.*
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import dagger.MapKey
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.reflect.KClass

enum class DataState { LOADING, SUCCESS, ERROR }

data class Data<out T> constructor(val dataState: DataState, val data: T? = null, val message: String? = null)

@Suppress("UNCHECKED_CAST")
@Singleton
class ViewModelFactory @Inject constructor(private val viewModels: MutableMap<Class<out ViewModel>, Provider<ViewModel>>) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T = viewModels[modelClass]?.get() as T

}

inline fun <reified T : ViewModel> FragmentActivity.getViewModel(viewModelFactory: ViewModelProvider.Factory): T {
    return ViewModelProviders.of(this, viewModelFactory)[T::class.java]
}

inline fun <reified T : ViewModel> FragmentActivity.withViewModel(
    viewModelFactory: ViewModelProvider.Factory,
    body: T.() -> Unit = {}
): T {
    val vm = getViewModel<T>(viewModelFactory)
    vm.body()
    return vm
}

inline fun <reified T : ViewModel> Fragment.getViewModel(viewModelFactory: ViewModelProvider.Factory): T {
    return ViewModelProviders.of(this, viewModelFactory)[T::class.java]
}

inline fun <reified T : ViewModel> Fragment.withViewModel(
    viewModelFactory: ViewModelProvider.Factory,
    body: T.() -> Unit
): T {
    val vm = getViewModel<T>(viewModelFactory)
    vm.body()
    return vm
}

fun <T> Single<T>.subscribe(
    onSubscribe: () -> Unit = {},
    success: (data: T) -> Unit,
    error: (throwable: Throwable) -> Unit,
    compositeDisposable: CompositeDisposable
) {

    this.subscribeOn(Schedulers.newThread())
        .doOnSubscribe { onSubscribe.invoke() }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
            success.invoke(it)
        }, {
            error.invoke(it)
        }).addToCompositeDisposable(compositeDisposable)

}

fun <T : Any, L : LiveData<T>> LifecycleOwner.observe(liveData: L, body: (T?) -> Unit) {
    liveData.observe(this, Observer(body))
}

fun <K> MutableLiveData<Data<K>>.showError(message: String?) {
    this.postValue(Data(dataState = DataState.ERROR, message = message))
}

fun <K> MutableLiveData<Data<K>>.showLoading() {
    this.postValue(Data(dataState = DataState.LOADING))
}

fun <K> MutableLiveData<Data<K>>.updateData(data: K, message: String? = null) {
    this.postValue(Data(dataState = DataState.SUCCESS, data = data, message = message))
}

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)
