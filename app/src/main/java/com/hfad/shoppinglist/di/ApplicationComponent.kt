package com.hfad.shoppinglist.di

import dagger.BindsInstance
import dagger.Component
import android.app.Application
import com.hfad.shoppinglist.presentation.MainActivity
import com.hfad.shoppinglist.presentation.ShopItemFragment

@ApplicationScope
@Component(
    modules = [
        DataModule::class,
        ViewModelModule::class
    ]
)
interface ApplicationComponent {

    fun inject(activity: MainActivity)

    fun inject(fragment: ShopItemFragment)

    @Component.Factory
    interface Factory {

        fun create(
            @BindsInstance application: Application
        ): ApplicationComponent
    }
}