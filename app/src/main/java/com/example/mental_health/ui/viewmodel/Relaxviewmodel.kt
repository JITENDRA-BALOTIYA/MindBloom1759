package com.example.mental_health.ui.viewmodel


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.mental_health.data.repository.MeditationVideo
import com.example.mental_health.data.repository.MusicTrackData
import com.example.mental_health.data.repository.RelaxRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────────────────────
//  RelaxViewModel.kt
//  Manages ExoPlayer instances for music + video playback
// ─────────────────────────────────────────────────────────────────────────────

data class RelaxUiState(
    // Music
    val musicTracks: List<MusicTrackData> = RelaxRepository.musicTracks,
    val currentTrackIndex: Int = -1,          // -1 = none playing
    val isMusicPlaying: Boolean = false,
    val musicProgress: Float = 0f,            // 0f..1f

    // Video
    val videos: List<MeditationVideo> = RelaxRepository.meditationVideos,
    val currentVideoIndex: Int = -1,
    val isVideoPlaying: Boolean = false
)

@HiltViewModel
class RelaxViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(RelaxUiState())
    val uiState: StateFlow<RelaxUiState> = _uiState.asStateFlow()

    // ── Music Player ──────────────────────────────────────────────────────────
    val musicPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().also { player ->
            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        playNextTrack()
                    }
                }
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _uiState.value = _uiState.value.copy(isMusicPlaying = isPlaying)
                }
            })
        }
    }

    // ── Video Player ──────────────────────────────────────────────────────────
    val videoPlayer: ExoPlayer by lazy {
        ExoPlayer.Builder(context).build().also { player ->
            player.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _uiState.value = _uiState.value.copy(isVideoPlaying = isPlaying)
                }
            })
        }
    }

    // Progress polling
    init {
        viewModelScope.launch {
            while (true) {
                delay(500)
                val player = musicPlayer
                if (player.isPlaying && player.duration > 0) {
                    val progress = player.currentPosition.toFloat() / player.duration.toFloat()
                    _uiState.value = _uiState.value.copy(musicProgress = progress)
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Music Controls
    // ─────────────────────────────────────────────────────────────────────────

    fun playTrack(index: Int) {
        val track = _uiState.value.musicTracks.getOrNull(index) ?: return

        // Stop video if playing
        if (videoPlayer.isPlaying) videoPlayer.pause()

        if (_uiState.value.currentTrackIndex == index && musicPlayer.isPlaying) {
            // Same track → pause/resume
            musicPlayer.pause()
        } else {
            // New track
            musicPlayer.setMediaItem(MediaItem.fromUri(track.streamUrl))
            musicPlayer.prepare()
            musicPlayer.play()
            _uiState.value = _uiState.value.copy(
                currentTrackIndex = index,
                musicProgress = 0f
            )
        }
    }

    fun pauseMusic() = musicPlayer.pause()
    fun resumeMusic() = musicPlayer.play()

    fun seekMusic(progress: Float) {
        val duration = musicPlayer.duration
        if (duration > 0) {
            musicPlayer.seekTo((progress * duration).toLong())
        }
    }

    private fun playNextTrack() {
        val nextIndex = (_uiState.value.currentTrackIndex + 1) % _uiState.value.musicTracks.size
        playTrack(nextIndex)
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Video Controls
    // ─────────────────────────────────────────────────────────────────────────

    fun playVideo(index: Int) {
        val video = _uiState.value.videos.getOrNull(index) ?: return

        // Pause music while video plays
        if (musicPlayer.isPlaying) musicPlayer.pause()

        videoPlayer.setMediaItem(MediaItem.fromUri(video.streamUrl))
        videoPlayer.prepare()
        videoPlayer.play()

        _uiState.value = _uiState.value.copy(currentVideoIndex = index)
    }

    fun pauseVideo() = videoPlayer.pause()
    fun resumeVideo() = videoPlayer.play()
    fun stopVideo() {
        videoPlayer.stop()
        _uiState.value = _uiState.value.copy(
            currentVideoIndex = -1,
            isVideoPlaying = false
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Cleanup
    // ─────────────────────────────────────────────────────────────────────────
    override fun onCleared() {
        super.onCleared()
        musicPlayer.release()
        videoPlayer.release()
    }
}