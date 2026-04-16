package pl.edu.ur.km131467.trainit.data.remote.api

import pl.edu.ur.km131467.trainit.data.remote.dto.LoginRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.LoginResponseDto
import pl.edu.ur.km131467.trainit.data.remote.dto.RegisterRequestDto
import pl.edu.ur.km131467.trainit.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequestDto): Response<UserDto>

    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequestDto): Response<LoginResponseDto>
}
