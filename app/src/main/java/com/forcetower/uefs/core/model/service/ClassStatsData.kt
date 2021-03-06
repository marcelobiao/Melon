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

package com.forcetower.uefs.core.model.service

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName

data class ClassStatsData(
    var code: String,
    @ColumnInfo(name = "discipline")
    @SerializedName("discipline_name")
    var disciplineName: String,
    @SerializedName("credits")
    @ColumnInfo(name = "credits")
    var disciplineCredits: Int,
    var semester: Int,
    @ColumnInfo(name = "semester_name")
    @SerializedName("semester_name")
    var semesterName: String,
    var teacher: String,
    var grade: Double?,
    @SerializedName("partial_score")
    var partialScore: Double?,
    var group: String,
    var identifier: Int,
    @ColumnInfo(name = "eval_grade")
    var evaluationGrade: String?,
    @ColumnInfo(name = "eval_name")
    var evaluationName: String,
    @ColumnInfo(name = "eval_date")
    var evaluationDate: String?
) {
    companion object {
        const val STATS_CONTRIBUTION = "stats_contribution"
    }
}