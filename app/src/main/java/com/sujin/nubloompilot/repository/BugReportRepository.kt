package com.sujin.nubloompilot.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.sujin.nubloompilot.models.BugReport
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class BugReportRepository {
    private val db = FirebaseFirestore.getInstance()

    /**
     * Submits a bug report to the "bug_reports" collection in Firestore.
     */
    suspend fun submitBugReport(report: BugReport) {
        return suspendCancellableCoroutine { continuation ->
            db.collection("bug_reports")
                .add(report)
                .addOnSuccessListener {
                    continuation.resume(Unit)
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }
}
