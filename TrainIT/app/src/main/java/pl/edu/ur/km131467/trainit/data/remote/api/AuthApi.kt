package pl.edu.ur.km131467.trainit.data.remote.api

import pl.edu.ur.km131467.trainit.data.remote.dto.LoginRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.LoginResponseDto
import pl.edu.ur.km131467.trainit.data.remote.dto.ForgotPasswordRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.RegisterRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.UpdateProfileRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

interface AuthApi {

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequestDto): Response<UserDto>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequestDto): Response<LoginResponseDto>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequestDto): Response<Unit>

    @GET("api/auth/me")
    suspend fun getMe(@Header("Authorization") authorization: String): Response<UserDto>

    @PUT("api/auth/me")
    suspend fun updateMe(
        @Header("Authorization") authorization: String,
        @Body body: UpdateProfileRequestDto,
    ): Response<UserDto>
}
