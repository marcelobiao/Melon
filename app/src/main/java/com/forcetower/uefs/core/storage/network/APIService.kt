/*
 * Copyright (c) 2019.
 * João Paulo Sena <joaopaulo761@gmail.com>
 *
 * This file is part of the UNES Open Source Project.
 *
 * UNES is licensed under the MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.forcetower.uefs.core.storage.network

import androidx.lifecycle.LiveData
import com.forcetower.uefs.core.model.api.UDisciplineWithData
import com.forcetower.uefs.core.model.api.helpers.UHourOverview
import com.forcetower.uefs.core.model.api.UResponse
import com.forcetower.uefs.core.model.service.discipline.DisciplineDetailsData
import com.forcetower.uefs.core.storage.network.adapter.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface APIService {
    @POST("save_data")
    fun sendHourglassInitial(@Body data: DisciplineDetailsData): Call<UResponse<UHourOverview>>

    @GET("overview")
    fun overview(): LiveData<ApiResponse<UResponse<UHourOverview>>>

    @POST("discipline")
    @FormUrlEncoded
    fun getDisciplineDetails(@Field("code") code: String): LiveData<ApiResponse<UResponse<UDisciplineWithData>>>
}