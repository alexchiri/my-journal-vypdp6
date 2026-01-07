package com.kroslabs.myjournal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kroslabs.myjournal.data.EntryType
import com.kroslabs.myjournal.data.JournalEntry
import com.kroslabs.myjournal.data.JournalRepository
import com.kroslabs.myjournal.data.UserPreferencesData
import com.kroslabs.myjournal.data.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class EditorState(
    val entryId: Long? = null,
    val content: String = "",
    val entryType: EntryType = EntryType.DAILY,
    val isNew: Boolean = true,
    val isSaving: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class JournalViewModel(
    private val repository: JournalRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val entries: StateFlow<List<JournalEntry>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) {
            repository.getAllEntries()
        } else {
            repository.searchEntries(query)
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val userPreferences: StateFlow<UserPreferencesData> = preferencesRepository.userPreferences
        .stateIn(viewModelScope, SharingStarted.Lazily, UserPreferencesData())

    private val _editorState = MutableStateFlow(EditorState())
    val editorState: StateFlow<EditorState> = _editorState.asStateFlow()

    private val _showDeleteConfirmation = MutableStateFlow<JournalEntry?>(null)
    val showDeleteConfirmation: StateFlow<JournalEntry?> = _showDeleteConfirmation.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun startNewEntry(type: EntryType) {
        viewModelScope.launch {
            val draft = repository.getDraft(type)
            if (draft != null) {
                _editorState.value = EditorState(
                    entryId = draft.id,
                    content = draft.content,
                    entryType = type,
                    isNew = false
                )
            } else {
                _editorState.value = EditorState(entryType = type)
            }
        }
    }

    fun editEntry(entry: JournalEntry) {
        _editorState.value = EditorState(
            entryId = entry.id,
            content = entry.content,
            entryType = entry.entryType,
            isNew = false
        )
    }

    fun updateEditorContent(content: String) {
        _editorState.value = _editorState.value.copy(content = content)
        autoSaveAsDraft()
    }

    private fun autoSaveAsDraft() {
        viewModelScope.launch {
            val state = _editorState.value
            if (state.content.isNotBlank()) {
                val entry = JournalEntry(
                    id = state.entryId ?: 0,
                    content = state.content,
                    entryType = state.entryType,
                    isDraft = true,
                    updatedAt = System.currentTimeMillis()
                )
                val id = repository.saveEntry(entry)
                if (state.entryId == null) {
                    _editorState.value = state.copy(entryId = id)
                }
            }
        }
    }

    fun saveEntry(): Boolean {
        val state = _editorState.value
        if (state.content.isBlank()) {
            return false
        }

        viewModelScope.launch {
            _editorState.value = state.copy(isSaving = true)
            val entry = JournalEntry(
                id = state.entryId ?: 0,
                content = state.content,
                entryType = state.entryType,
                isDraft = false,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveEntry(entry)

            if (state.entryType == EntryType.WEEKLY_REVIEW) {
                preferencesRepository.setWeeklyReviewPending(false)
            }

            _editorState.value = EditorState()
        }
        return true
    }

    fun clearEditor() {
        viewModelScope.launch {
            val state = _editorState.value
            if (state.entryId != null && state.content.isBlank()) {
                repository.getEntryById(state.entryId)?.let { entry ->
                    if (entry.isDraft) {
                        repository.deleteEntry(entry)
                    }
                }
            }
            _editorState.value = EditorState()
        }
    }

    fun requestDelete(entry: JournalEntry) {
        _showDeleteConfirmation.value = entry
    }

    fun confirmDelete() {
        viewModelScope.launch {
            _showDeleteConfirmation.value?.let { entry ->
                repository.deleteEntry(entry)
            }
            _showDeleteConfirmation.value = null
        }
    }

    fun cancelDelete() {
        _showDeleteConfirmation.value = null
    }

    fun getEntriesForPastWeek() = viewModelScope.launch {
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startTime = calendar.timeInMillis
        repository.getEntriesInRange(startTime, endTime)
    }

    fun setOnboardingCompleted() {
        viewModelScope.launch {
            preferencesRepository.setOnboardingCompleted()
        }
    }

    fun setReviewSchedule(dayOfWeek: Int, hour: Int, minute: Int) {
        viewModelScope.launch {
            preferencesRepository.setReviewSchedule(dayOfWeek, hour, minute)
        }
    }

    fun setTextSizeMultiplier(multiplier: Float) {
        viewModelScope.launch {
            preferencesRepository.setTextSizeMultiplier(multiplier)
        }
    }

    fun setWeeklyReviewPending(pending: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setWeeklyReviewPending(pending)
        }
    }

    class Factory(
        private val repository: JournalRepository,
        private val preferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return JournalViewModel(repository, preferencesRepository) as T
        }
    }
}
