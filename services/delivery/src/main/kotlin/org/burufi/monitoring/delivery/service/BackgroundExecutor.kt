package org.burufi.monitoring.delivery.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Component
class BackgroundExecutor {

    companion object {
        private val log = LoggerFactory.getLogger(BackgroundExecutor::class.java)
    }

    private val executor: ExecutorService

    init {
        val factory = Thread.ofVirtual()
            .name("Delivery-", 1)
            .uncaughtExceptionHandler(LoggingExceptionHandler())
            .factory()
        executor = Executors.newThreadPerTaskExecutor(factory)
    }

    fun execute(task: Runnable) = executor.execute(task)

    private class LoggingExceptionHandler : UncaughtExceptionHandler {

        override fun uncaughtException(t: Thread, e: Throwable) {
            log.error("Uncaught exception in the background delivery executor", e)
        }
    }
}
