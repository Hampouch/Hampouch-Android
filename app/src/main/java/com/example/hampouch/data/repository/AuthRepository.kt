package com.example.hampouch.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.hampouch.data.model.AuthProvider
import com.example.hampouch.data.model.UserSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth_session")

/**
 * 로그인 세션을 DataStore에 저장/조회하는 앱 전역 저장소.
 * [getInstance]로 어느 파일에서든 같은 인스턴스를 얻어 세션을 읽거나 갱신할 수 있다.
 */
class AuthRepository private constructor(private val context: Context) {

    private object Keys {
        val PROVIDER = stringPreferencesKey("provider")
        val TOKEN = stringPreferencesKey("token")
        val NICKNAME = stringPreferencesKey("nickname")
        val EMAIL = stringPreferencesKey("email")
        val PROFILE_IMAGE_URL = stringPreferencesKey("profile_image_url")
    }

    val userSession: Flow<UserSession?> = context.authDataStore.data.map { preferences ->
        val provider = preferences[Keys.PROVIDER]?.let { AuthProvider.valueOf(it) }
        val token = preferences[Keys.TOKEN]
        if (provider == null || token == null) {
            null
        } else {
            UserSession(
                provider = provider,
                token = token,
                nickname = preferences[Keys.NICKNAME],
                email = preferences[Keys.EMAIL],
                profileImageUrl = preferences[Keys.PROFILE_IMAGE_URL]
            )
        }
    }

    suspend fun saveSession(session: UserSession) {
        context.authDataStore.edit { preferences ->
            preferences[Keys.PROVIDER] = session.provider.name
            preferences[Keys.TOKEN] = session.token
            session.nickname?.let { preferences[Keys.NICKNAME] = it } ?: preferences.remove(Keys.NICKNAME)
            session.email?.let { preferences[Keys.EMAIL] = it } ?: preferences.remove(Keys.EMAIL)
            session.profileImageUrl?.let {
                preferences[Keys.PROFILE_IMAGE_URL] = it
            } ?: preferences.remove(Keys.PROFILE_IMAGE_URL)
        }
    }

    suspend fun clearSession() {
        context.authDataStore.edit { it.clear() }
    }

    companion object {
        @Volatile
        private var instance: AuthRepository? = null

        fun getInstance(context: Context): AuthRepository =
            instance ?: synchronized(this) {
                instance ?: AuthRepository(context.applicationContext).also { instance = it }
            }
    }
}
