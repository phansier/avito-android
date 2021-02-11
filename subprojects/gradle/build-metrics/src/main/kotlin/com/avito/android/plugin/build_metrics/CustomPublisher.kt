package com.avito.android.plugin.build_metrics

import com.cdsap.talaiot.entities.ExecutionReport
import com.cdsap.talaiot.publisher.Publisher

class CustomPublisher: Publisher {

    override fun publish(report: ExecutionReport) {
        println("=== CustomPublisher ===")
    }
}
