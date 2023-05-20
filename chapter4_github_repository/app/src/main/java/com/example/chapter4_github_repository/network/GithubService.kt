package com.example.chapter4_github_repository.network

import com.example.chapter4_github_repository.model.Repo
import com.example.chapter4_github_repository.model.UserDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubService {

    @Headers("Authorization: Bearer yourtoken")
    @GET("users/{username}/repos")
    fun listRepos(@Path("username") username: String): Call<List<Repo>>

    @Headers("Authorization: Bearer yourtoken")
    @GET("search/users")
    fun searchUser(@Query("q") query: String): Call<UserDto>
}