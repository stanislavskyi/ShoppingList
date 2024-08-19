package com.hfad.shoppinglist.di

import android.app.Application
import com.hfad.shoppinglist.data.AppDatabase
import com.hfad.shoppinglist.data.ShopListDao
import com.hfad.shoppinglist.data.ShopListRepositoryImpl
import com.hfad.shoppinglist.domain.ShopListRepository
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
interface DataModule {

    @ApplicationScope
    @Binds
    fun bindShopListRepository(impl: ShopListRepositoryImpl): ShopListRepository

    companion object {

        @ApplicationScope
        @Provides
        fun provideShopListDao(
            application: Application
        ): ShopListDao {
            return AppDatabase.getInstance(application).shopListDao()
        }
    }
}