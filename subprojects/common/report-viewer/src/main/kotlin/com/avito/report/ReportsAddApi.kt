package com.avito.report

import com.avito.android.Result
import com.avito.report.model.AndroidTest
import com.avito.report.model.BuildId
import com.avito.report.model.ReportCoordinates

interface ReportsAddApi {

    fun addTests(
        reportCoordinates: ReportCoordinates,
        buildId: BuildId,
        tests: Collection<AndroidTest>
    ): Result<List<String>>

    fun addTest(
        reportCoordinates: ReportCoordinates,
        test: AndroidTest
    ): Result<String>
}
