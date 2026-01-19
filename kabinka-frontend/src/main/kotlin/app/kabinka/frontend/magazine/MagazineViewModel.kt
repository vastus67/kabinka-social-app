package app.kabinka.frontend.magazine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Magazine feature
 */
class MagazineViewModel : ViewModel() {
    private val repository = MagazineRepository.getInstance()
    
    private val _currentIssue = MutableStateFlow<MagazineIssue?>(null)
    val currentIssue: StateFlow<MagazineIssue?> = _currentIssue.asStateFlow()
    
    private val _pastIssues = MutableStateFlow<List<MagazineIssue>>(emptyList())
    val pastIssues: StateFlow<List<MagazineIssue>> = _pastIssues.asStateFlow()
    
    // Persistent state for saved and dismissed ads
    private val _savedAds = MutableStateFlow<Set<String>>(emptySet())
    val savedAds: StateFlow<Set<String>> = _savedAds.asStateFlow()
    
    private val _dismissedAds = MutableStateFlow<Set<String>>(emptySet())
    val dismissedAds: StateFlow<Set<String>> = _dismissedAds.asStateFlow()
    
    val interactions = repository.interactions
    val submissions = repository.submissions
    
    init {
        loadIssues()
        observeInteractions()
    }
    
    private fun loadIssues() {
        viewModelScope.launch {
            _currentIssue.value = repository.getCurrentIssue()
            _pastIssues.value = repository.getPastIssues()
        }
    }
    
    private fun observeInteractions() {
        viewModelScope.launch {
            repository.interactions.collect { interactionMap ->
                // Update saved ads from repository
                _savedAds.value = interactionMap
                    .filter { it.value.isSaved }
                    .keys
                
                // Update dismissed ads from repository
                _dismissedAds.value = interactionMap
                    .filter { it.value.isDismissed }
                    .keys
            }
        }
    }
    
    fun getIssueById(issueId: String): MagazineIssue? {
        return repository.getIssueById(issueId)
    }
    
    fun getAdById(adId: String): MagazineAd? {
        return repository.getAdById(adId)
    }
    
    fun saveAd(adId: String) {
        viewModelScope.launch {
            _savedAds.value = _savedAds.value + adId
            repository.markAsCircled(adId)
        }
    }
    
    fun dismissAd(adId: String) {
        viewModelScope.launch {
            _dismissedAds.value = _dismissedAds.value + adId
            repository.markAsCrumpled(adId)
        }
    }
    
    fun getSavedAds(): List<MagazineAd> {
        return repository.getAllIssues()
            .flatMap { it.ads }
            .filter { it.id in _savedAds.value }
    }
    
    fun markAsTornOff(adId: String) {
        repository.markAsTornOff(adId)
    }
    
    fun submitAd(submission: AdSubmission) {
        repository.submitAd(submission)
    }
    
    fun saveDraft(submission: AdSubmission) {
        repository.saveDraft(submission)
    }
}
