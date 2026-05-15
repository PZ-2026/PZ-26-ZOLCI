package pl.edu.ur.km131467.trainit.data.remote.api

import pl.edu.ur.km131467.trainit.data.remote.dto.ChangeRoleRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * API REST panelu administratora.
 */
interface AdminApi {

    @GET("api/admin/users")
    /**
     * Wywołuje endpoint: `getUsers`.
     */
    suspend fun getUsers(@Header("Authorization") authorization: String): Response<List<UserDto>>

    @PUT("api/admin/users/{id}/role")
    /**
     * Wywołuje endpoint: `changeRole`.
     */
    suspend fun changeRole(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
        @Body body: ChangeRoleRequestDto,
    ): Response<UserDto>

    @PUT("api/admin/users/{id}/block")
    /**
     * Wywołuje endpoint: `blockUser`.
     */
    suspend fun blockUser(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
    ): Response<UserDto>

    @PUT("api/admin/users/{id}/unblock")
    /**
     * Wywołuje endpoint: `unblockUser`.
     */
    suspend fun unblockUser(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
    ): Response<UserDto>

    @DELETE("api/admin/users/{id}")
    /**
     * Wywołuje endpoint: `deleteUser`.
     */
    suspend fun deleteUser(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int,
    ): Response<Unit>
}

