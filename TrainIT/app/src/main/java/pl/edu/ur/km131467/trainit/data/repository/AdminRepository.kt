package pl.edu.ur.km131467.trainit.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.edu.ur.km131467.trainit.data.remote.NetworkModule
import pl.edu.ur.km131467.trainit.data.remote.api.AdminApi
import pl.edu.ur.km131467.trainit.data.remote.dto.ChangeRoleRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.UserDto

/**
 * Repozytorium operacji panelu administratora (użytkownicy, role).
 */
class AdminRepository(
    private val adminApi: AdminApi = NetworkModule.adminApi,
) {

    /**
     * Pobiera listę wszystkich użytkowników systemu.
     *
     * @param authHeader nagłówek `Authorization: Bearer …`
     * @return [Result] z listą [UserDto] lub wyjątkiem przy błędzie HTTP
     */
    suspend fun getUsers(authHeader: String): Result<List<UserDto>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = adminApi.getUsers(authHeader)
            if (!response.isSuccessful) {
                throw IllegalStateException("Serwer zwrócił błąd: HTTP ${response.code()}")
            }
            response.body() ?: emptyList()
        }
    }

    /**
     * Zmienia rolę wybranego użytkownika.
     *
     * @param authHeader nagłówek `Authorization: Bearer …`
     * @param userId identyfikator użytkownika
     * @param role nowa rola (USER, TRAINER, ADMIN)
     * @return [Result] z zaktualizowanym [UserDto]
     */
    suspend fun changeRole(authHeader: String, userId: Int, role: String): Result<UserDto> = withContext(Dispatchers.IO) {
        runCatching {
            val response = adminApi.changeRole(authHeader, userId, ChangeRoleRequestDto(role = role))
            if (!response.isSuccessful || response.body() == null) {
                throw IllegalStateException("Nie udało się zmienić roli (HTTP ${response.code()})")
            }
            response.body()!!
        }
    }

    /**
     * Blokuje konto użytkownika (dezaktywacja).
     *
     * @param authHeader nagłówek `Authorization: Bearer …`
     * @param userId identyfikator użytkownika
     * @return [Result] z zaktualizowanym [UserDto]
     */
    suspend fun blockUser(authHeader: String, userId: Int): Result<UserDto> = withContext(Dispatchers.IO) {
        runCatching {
            val response = adminApi.blockUser(authHeader, userId)
            if (!response.isSuccessful || response.body() == null) {
                throw IllegalStateException("Nie udało się zablokować użytkownika (HTTP ${response.code()})")
            }
            response.body()!!
        }
    }

    /**
     * Odblokowuje wcześniej zablokowane konto użytkownika.
     *
     * @param authHeader nagłówek `Authorization: Bearer …`
     * @param userId identyfikator użytkownika
     * @return [Result] z zaktualizowanym [UserDto]
     */
    suspend fun unblockUser(authHeader: String, userId: Int): Result<UserDto> = withContext(Dispatchers.IO) {
        runCatching {
            val response = adminApi.unblockUser(authHeader, userId)
            if (!response.isSuccessful || response.body() == null) {
                throw IllegalStateException("Nie udało się odblokować użytkownika (HTTP ${response.code()})")
            }
            response.body()!!
        }
    }

    /**
     * Trwale usuwa użytkownika z systemu.
     *
     * @param authHeader nagłówek `Authorization: Bearer …`
     * @param userId identyfikator użytkownika
     * @return [Result] z [Unit] przy sukcesie
     */
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
