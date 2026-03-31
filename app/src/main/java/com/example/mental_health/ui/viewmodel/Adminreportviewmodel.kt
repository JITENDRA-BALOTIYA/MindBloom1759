package com.example.mental_health.ui.viewmodel



import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mental_health.data.model.RiskLevel
import com.example.mental_health.data.model.WeeklyReport
import com.example.mental_health.data.model.riskLevel
import com.example.mental_health.data.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────────────────────
//  UI State
// ─────────────────────────────────────────────────────────────────────────────

data class AdminReportUiState(
    val isLoading: Boolean = true,
    val reports: List<WeeklyReport> = emptyList(),
    val filteredReports: List<WeeklyReport> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: ReportFilter = ReportFilter.ALL,
    val selectedReport: WeeklyReport? = null,
    val adminNoteInput: String = "",
    val isSavingNote: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

enum class ReportFilter(val label: String) {
    ALL("All Students"),
    HIGH_RISK("High Risk"),
    MEDIUM_RISK("Medium Risk"),
    LOW_RISK("Low Risk"),
    LOW_ATTENDANCE("Low Attendance")
}

// ─────────────────────────────────────────────────────────────────────────────
//  ViewModel
// ─────────────────────────────────────────────────────────────────────────────

@HiltViewModel
class AdminReportViewModel @Inject constructor(
    private val repository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminReportUiState())
    val uiState: StateFlow<AdminReportUiState> = _uiState.asStateFlow()

    init {
        loadReports()
    }

    // ── Load all reports (real-time) ──────────────────────────────────────────

    private fun loadReports() {
        viewModelScope.launch {
            repository.getAllReportsFlow()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { reports ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            reports = reports,
                            filteredReports = applyFilters(
                                reports, state.searchQuery, state.selectedFilter
                            )
                        )
                    }
                }
        }
    }

    // ── Search ────────────────────────────────────────────────────────────────

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredReports = applyFilters(state.reports, query, state.selectedFilter)
            )
        }
    }

    // ── Filter ────────────────────────────────────────────────────────────────

    fun onFilterChange(filter: ReportFilter) {
        _uiState.update { state ->
            state.copy(
                selectedFilter = filter,
                filteredReports = applyFilters(state.reports, state.searchQuery, filter)
            )
        }
    }

    private fun applyFilters(
        reports: List<WeeklyReport>,
        query: String,
        filter: ReportFilter
    ): List<WeeklyReport> {
        var result = reports

        // Text search
        if (query.isNotBlank()) {
            result = result.filter {
                it.studentName.contains(query, ignoreCase = true) ||
                        it.studentId.contains(query, ignoreCase = true)
            }
        }

        // Risk / attendance filter
        result = when (filter) {
            ReportFilter.ALL            -> result
            ReportFilter.HIGH_RISK      -> result.filter { it.riskLevel() == RiskLevel.HIGH }
            ReportFilter.MEDIUM_RISK    -> result.filter { it.riskLevel() == RiskLevel.MEDIUM }
            ReportFilter.LOW_RISK       -> result.filter { it.riskLevel() == RiskLevel.LOW }
            ReportFilter.LOW_ATTENDANCE -> result.filter { it.attendancePercent < 75f }
        }

        return result
    }

    // ── Select report (open detail) ───────────────────────────────────────────

    fun selectReport(report: WeeklyReport) {
        _uiState.update {
            it.copy(selectedReport = report, adminNoteInput = report.adminNote)
        }
    }

    fun clearSelectedReport() {
        _uiState.update { it.copy(selectedReport = null, adminNoteInput = "") }
    }

    // ── Admin note ────────────────────────────────────────────────────────────

    fun onAdminNoteChange(note: String) {
        _uiState.update { it.copy(adminNoteInput = note) }
    }

    fun saveAdminNote() {
        val report = _uiState.value.selectedReport ?: return
        val note   = _uiState.value.adminNoteInput

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingNote = true) }
            try {
                repository.updateAdminNote(report.reportId, report.studentId, note)
                _uiState.update {
                    it.copy(
                        isSavingNote   = false,
                        successMessage = "Note saved successfully",
                        selectedReport = report.copy(adminNote = note)
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSavingNote = false, error = e.message) }
            }
        }
    }

    // ── Clear messages ────────────────────────────────────────────────────────

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }

    // ── Summary stats for top banner ──────────────────────────────────────────

    fun getSummaryStats(): AdminSummaryStats {
        val reports = _uiState.value.reports
        return AdminSummaryStats(
            totalStudents   = reports.map { it.studentId }.distinct().size,
            highRiskCount   = reports.count { it.riskLevel() == RiskLevel.HIGH },
            avgWellness     = if (reports.isNotEmpty()) reports.map { it.wellnessScore }.average().toInt() else 0,
            lowAttendCount  = reports.count { it.attendancePercent < 75f }
        )
    }
}

data class AdminSummaryStats(
    val totalStudents: Int,
    val highRiskCount: Int,
    val avgWellness: Int,
    val lowAttendCount: Int
)