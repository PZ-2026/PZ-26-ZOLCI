package pl.edu.ur.km131467.trainit.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.edu.ur.km131467.trainit.data.remote.NetworkModule
import pl.edu.ur.km131467.trainit.data.remote.api.AdminApi
import pl.edu.ur.km131467.trainit.data.remote.dto.ChangeRoleRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.UserDto

class AdminRepository(
    private val adminApi: AdminApi = NetworkModule.adminApi,
) {
    suspend fun getUsers(authHeader: String): Result<List<UserDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = adminApi.getUsers(authHeader)
            if (!response.isSuccessful) {
                throw IllegalStateException("Serwer zwrócił błąd: HTTP ${response.code()}")
            }
            response.body() ?: emptyList()
        }
    }

    suspend fun changeRole(authHeader: String, userId: Int, role: String): Result<UserDto> = withContext(Dispatchers.IO) {
        runCatching {
            val response = adminApi.changeRole(authHeader, userId, ChangeRoleRequestDto(role = role))
            if (!response.isSuccessful || response.body() == null) {
                throw IllegalStateException("Nie udało się zmienić roli (HTTP ${response.code()})")
            }
            response.body()!!
        }
    }

    suspend fun blockUser(authHeader: String, userId: Int): Result<UserDto> = withContext(Dispatchers.IO) {
        runCatching {
            val response = adminApi.blockUser(authHeader, userId)
            if (!response.isSuccessful || response.body() == null) {
                throw IllegalStateException("Nie udało się zablokować użytkownika (HTTP ${response.code()})")
            }
            response.body()!!
        }
    }

    suspend fun unblockUser(authHeader: String, userId: Int): Result<UserDto> = withContext(Dispatchers.IO) {
        runCatching {
            val response = adminApi.unblockUser(authHeader, userId)
            if (!response.isSuccessful || response.body() == null) {
                throw IllegalStateException("Nie udało się odblokować użytkownika (HTTP ${response.code()})")
            }
            response.body()!!
        }
    }

    suspend fun deleteUser(authHeader: String, userId: Int): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = adminApi.deleteUser(authHeader, userId)
            if (!response.isSuccessful) {
                throw IllegalStateException("Nie udało się usunąć użytkownika (HTTP ${response.code()})")
            }
            Unit
        }
    }
}

