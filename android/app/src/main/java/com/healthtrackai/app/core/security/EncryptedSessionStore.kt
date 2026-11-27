package com.healthtrackai.app.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

data class StoredSession(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresAt: String,
    val refreshTokenExpiresAt: String,
)

class SessionStorageException(cause: Throwable? = null) : Exception(
    "Không thể truy cập phiên đăng nhập được mã hóa trên thiết bị.",
    cause,
)

@Singleton
class EncryptedSessionStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    suspend fun read(): StoredSession? {
        return try {
            val preferences = dataStore.data.first()
            val storedValues = listOf(
                preferences[ACCESS_TOKEN],
                preferences[REFRESH_TOKEN],
                preferences[ACCESS_EXPIRES_AT],
                preferences[REFRESH_EXPIRES_AT],
            )
            if (storedValues.all { it == null }) return null
            if (storedValues.any { it == null }) throw SessionStorageException()
            StoredSession(
                accessToken = decrypt(storedValues[0] ?: throw SessionStorageException()),
                refreshToken = decrypt(storedValues[1] ?: throw SessionStorageException()),
                accessTokenExpiresAt = storedValues[2] ?: throw SessionStorageException(),
                refreshTokenExpiresAt = storedValues[3] ?: throw SessionStorageException(),
            )
        } catch (failure: SessionStorageException) {
            throw failure
        } catch (failure: Exception) {
            throw SessionStorageException(failure)
        }
    }

    suspend fun write(session: StoredSession) {
        try {
            val encryptedAccess = encrypt(session.accessToken)
            val encryptedRefresh = encrypt(session.refreshToken)
            dataStore.edit { preferences ->
                preferences[ACCESS_TOKEN] = encryptedAccess
                preferences[REFRESH_TOKEN] = encryptedRefresh
                preferences[ACCESS_EXPIRES_AT] = session.accessTokenExpiresAt
                preferences[REFRESH_EXPIRES_AT] = session.refreshTokenExpiresAt
            }
        } catch (failure: SessionStorageException) {
            throw failure
        } catch (failure: Exception) {
            throw SessionStorageException(failure)
        }
    }

    suspend fun clear() {
        try {
            dataStore.edit { preferences ->
                preferences.remove(ACCESS_TOKEN)
                preferences.remove(REFRESH_TOKEN)
                preferences.remove(ACCESS_EXPIRES_AT)
                preferences.remove(REFRESH_EXPIRES_AT)
            }
        } catch (failure: Exception) {
            throw SessionStorageException(failure)
        }
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
        return Base64.encodeToString(iv + encrypted, Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String {
        val decoded = Base64.decode(value, Base64.NO_WRAP)
        if (decoded.size <= IV_LENGTH + GCM_TAG_LENGTH_BYTES) {
            throw SessionStorageException()
        }
        val iv = decoded.copyOfRange(0, IV_LENGTH)
        val encrypted = decoded.copyOfRange(IV_LENGTH, decoded.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
        return String(cipher.doFinal(encrypted), StandardCharsets.UTF_8)
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build(),
            )
            generateKey()
        }
    }

    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val KEY_ALIAS = "healthtrack.auth.session.v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_LENGTH = 12
        const val GCM_TAG_LENGTH_BITS = 128
        const val GCM_TAG_LENGTH_BYTES = GCM_TAG_LENGTH_BITS / 8
        val ACCESS_TOKEN = stringPreferencesKey("session_access_token")
        val REFRESH_TOKEN = stringPreferencesKey("session_refresh_token")
        val ACCESS_EXPIRES_AT = stringPreferencesKey("session_access_expires_at")
        val REFRESH_EXPIRES_AT = stringPreferencesKey("session_refresh_expires_at")
    }
}
