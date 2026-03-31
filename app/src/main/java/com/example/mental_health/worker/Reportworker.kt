package com.example.mental_health.worker



import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.mental_health.weeklyReport.WeeklyReportGenerator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.Calendar
import java.util.concurrent.TimeUnit

// ─────────────────────────────────────────────────────────────────────────────
//  ReportWorker  — runs every Sunday automatically
// ─────────────────────────────────────────────────────────────────────────────

@HiltWorker
class ReportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val generator: WeeklyReportGenerator
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            generator.generateForAllStudents()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // Retry up to 3 times on failure
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "weekly_report_generator"

        /**
         * Schedule report generation every Sunday at 11:55 PM.
         * Call this from Application class or after login.
         */
        fun schedule(context: Context) {
            val initialDelay = calculateDelayToNextSunday()

            val weeklyRequest = PeriodicWorkRequestBuilder<ReportWorker>(
                repeatInterval = 7,
                repeatIntervalTimeUnit = TimeUnit.DAYS
            )
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                weeklyRequest
            )
        }

        /** For testing: trigger immediately */
        fun scheduleNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<ReportWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }

        /** Calculates milliseconds until next Sunday 11:55 PM */
        private fun calculateDelayToNextSunday(): Long {
            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 55)
                set(Calendar.SECOND, 0)
                if (before(now)) add(Calendar.WEEK_OF_YEAR, 1)
            }
            return target.timeInMillis - now.timeInMillis
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  WorkManager Hilt Integration
//  Add this to your @HiltAndroidApp Application class:
//
//  override fun onCreate() {
//      super.onCreate()
//      ReportWorker.schedule(this)
//  }
//
//  Also add to build.gradle (app):
//  implementation "androidx.hilt:hilt-work:1.1.0"
//  kapt "androidx.hilt:hilt-compiler:1.1.0"
//  implementation "androidx.work:work-runtime-ktx:2.9.0"
// ─────────────────────────────────────────────────────────────────────────────