package com.forcetower.uefs.core.storage.repository.cloud

import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.forcetower.uefs.AppExecutors
import com.forcetower.uefs.core.annotations.USLoginMethod
import com.forcetower.uefs.core.model.unes.Access
import com.forcetower.uefs.core.model.unes.AccessToken
import com.forcetower.uefs.core.model.unes.Profile
import com.forcetower.uefs.core.storage.database.UDatabase
import com.forcetower.uefs.core.storage.network.UService
import com.forcetower.uefs.core.storage.network.adapter.ApiResponse
import com.forcetower.uefs.core.storage.network.adapter.asLiveData
import com.forcetower.uefs.core.storage.resource.NetworkBoundResource
import com.forcetower.uefs.core.storage.resource.Resource
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val executors: AppExecutors,
    private val database: UDatabase,
    private val service: UService
) {
    @MainThread
    fun login(
        username: String,
        password: String,
        @USLoginMethod method: String = LOGIN_METHOD_UNES,
        forcedFetch: Boolean = true
    ): LiveData<Resource<AccessToken?>> {
        return object : NetworkBoundResource<AccessToken?, AccessToken> (executors) {
            override fun loadFromDb() = database.accessTokenDao().getAccessToken()
            override fun shouldFetch(it: AccessToken?) = forcedFetch || it == null
            override fun createCall(): LiveData<ApiResponse<AccessToken>> {
                return when (method) {
                    LOGIN_METHOD_UNES -> service.login(username, password).asLiveData()
                    else -> service.loginWithSagres(username, password).asLiveData()
                }
            }
            override fun saveCallResult(value: AccessToken) { database.accessTokenDao().insert(value) }
        }.asLiveData()
    }

    @WorkerThread
    fun performAccountSyncState() {
        val access = database.accessDao().getAccessDirect()
        access ?: return
        if (!access.valid) return
        val token = syncLogin(access.username, access.password)
        token ?: return
        syncCredentials(access)

        val profile = database.profileDao().selectMeDirect()
        profile ?: return
        syncProfileState(profile)
    }

    @WorkerThread
    private fun syncProfileState(profile: Profile): Boolean {
        try {
            val response = service.setupProfile(profile).execute()
            return response.isSuccessful
        } catch (t: Throwable) {
            Timber.e(t)
        }
        return false
    }

    @WorkerThread
    private fun syncCredentials(access: Access): Boolean {
        try {
            // Triggers a on server sync state
            val response = service.setupAccount(access).execute()
            return response.isSuccessful
        } catch (t: Throwable) {
            Timber.e(t)
        }
        return false
    }

    @WorkerThread
    fun syncLogin(username: String, password: String): AccessToken? {
        try {
            val response = service.loginWithSagres(username, password).execute()
            if (response.isSuccessful) {
                val token = response.body()
                if (token != null) {
                    database.accessTokenDao().insert(token)
                    return token
                } else {
                    Timber.e("Token response is null")
                }
            } else {
                Timber.e("Failed with code: ${response.code()}")
            }
        } catch (t: Throwable) {
            Timber.e(t)
        }
        return null
    }

    fun getAccessToken() = database.accessTokenDao().getAccessToken()
    fun getAccessTokenDirect() = database.accessTokenDao().getAccessTokenDirect()

    companion object {
        const val LOGIN_METHOD_UNES = "password"
        const val LOGIN_METHOD_SAGRES = "sagres"
    }
}