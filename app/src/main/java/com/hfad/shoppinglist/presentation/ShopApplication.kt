package com.hfad.shoppinglist.presentation

import android.app.Application
import com.hfad.shoppinglist.di.DaggerApplicationComponent

class ShopApplication : Application(){

    val component by lazy {
        DaggerApplicationComponent.factory().create(this)
    }
}