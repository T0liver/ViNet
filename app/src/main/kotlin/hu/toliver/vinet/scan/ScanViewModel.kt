package hu.toliver.vinet.scan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ScanViewModel @Inject constructor(
    application: Application,
    scanStateRepository: ScanStateRepository,
) : AndroidViewModel(application) {
    val uiState: StateFlow<ScanUiState> = scanStateRepository.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScanUiState(),
    )

    fun startScan() {
        ScanForegroundService.start(getApplication())
    }

    fun stopScan() {
        ScanForegroundService.stop(getApplication())
    }
}
