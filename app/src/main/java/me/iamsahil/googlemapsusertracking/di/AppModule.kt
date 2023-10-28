package me.iamsahil.googlemapsusertracking.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.iamsahil.googlemapsusertracking.data.firebase.FirebaseRealtimeDataSource
import me.iamsahil.googlemapsusertracking.data.location.DefaultLocationClient
import me.iamsahil.googlemapsusertracking.data.repository.UserTrackRepositoryImpl
import me.iamsahil.googlemapsusertracking.domain.location.LocationClient
import me.iamsahil.googlemapsusertracking.domain.repository.UserTrackRepository


import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provideLocationClient(
        @ApplicationContext context: Context,
        fusedLocationProviderClient: FusedLocationProviderClient
    ): LocationClient = DefaultLocationClient(context, fusedLocationProviderClient)

    @Provides
    @Singleton
    fun providesUserTrackRepo(
        firebaseRealtimeDataSource: FirebaseRealtimeDataSource
    ) : UserTrackRepository = UserTrackRepositoryImpl(firebaseRealtimeDataSource)

    @Provides
    @Singleton
    fun providesFirebaseDatabase() : FirebaseDatabase = FirebaseDatabase.getInstance()



}