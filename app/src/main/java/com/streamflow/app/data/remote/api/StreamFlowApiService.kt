package com.streamflow.app.data.remote.api

import com.streamflow.app.data.remote.dto.CatalogResponseDto
import com.streamflow.app.data.remote.dto.RailDto
import com.streamflow.app.data.remote.dto.ReportRequestDto
import com.streamflow.app.data.remote.dto.ReportResponseDto
import com.streamflow.app.data.remote.dto.TitleDto
import com.streamflow.app.data.remote.dto.WatchlistRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface StreamFlowApiService {

    @GET("v1/catalog")
    suspend fun getCatalog(
        @Query("_t") timestamp: Long = System.currentTimeMillis()
    ): CatalogResponseDto

    @GET("v1/catalog/featured")
    suspend fun getFeaturedTitles(
        @Query("_t") timestamp: Long = System.currentTimeMillis()
    ): List<TitleDto>

    @GET("v1/catalog/rails")
    suspend fun getHomeRails(
        @Query("_t") timestamp: Long = System.currentTimeMillis()
    ): List<RailDto>

    @GET("v1/titles/{id}")
    suspend fun getTitleById(
        @Path("id") titleId: String,
        @Query("_t") timestamp: Long = System.currentTimeMillis()
    ): TitleDto

    @GET("v1/titles/search")
    suspend fun searchTitles(
        @Query("q") query: String,
        @Query("_t") timestamp: Long = System.currentTimeMillis()
    ): List<TitleDto>

    @GET("v1/watchlist")
    suspend fun getUserWatchlist(): List<TitleDto>

    @POST("v1/watchlist/sync")
    suspend fun syncWatchlist(
        @Body request: WatchlistRequestDto
    ): List<TitleDto>

    @POST("v1/reports")
    suspend fun submitReportV1(
        @Body request: ReportRequestDto
    ): ReportResponseDto

    @POST("reports")
    suspend fun submitReportRoot(
        @Body request: ReportRequestDto
    ): ReportResponseDto

    @POST("v1/titles/{id}/report")
    suspend fun reportTitleV1(
        @Path("id") titleId: String,
        @Body request: ReportRequestDto
    ): ReportResponseDto

    @POST("titles/{id}/report")
    suspend fun reportTitleRoot(
        @Path("id") titleId: String,
        @Body request: ReportRequestDto
    ): ReportResponseDto
}
