package com.example.mental_health.data.repository


// ─────────────────────────────────────────────────────────────────────────────
//  RelaxRepository.kt
//  Provides real music tracks and meditation videos (free/public domain URLs)
// ─────────────────────────────────────────────────────────────────────────────

data class MusicTrackData(
    val id: Int,
    val title: String,
    val subtitle: String,
    val duration: String,
    val emoji: String,
    val streamUrl: String          // Direct MP3 stream URL
)

data class MeditationVideo(
    val id: Int,
    val title: String,
    val description: String,
    val thumbnail: String,         // Emoji placeholder (replace with real thumbnails)
    val duration: String,
    val streamUrl: String          // Direct MP4 stream URL
)

object RelaxRepository {

    // ── Free / Public-domain music tracks (Archive.org + Pixabay CDN) ─────────
    val musicTracks = listOf(
        MusicTrackData(
            id = 1,
            title = "kar Har Madan Fhateh",
            subtitle = "Peaceful background tones",
            duration = "3:20",
            emoji = "🎵",
            streamUrl = "https://neijocyrjpzswbyhhagw.supabase.co/storage/v1/object/public/Music/Kar%20Har%20Maidaan%20Fateh%20Sanju%20320%20Kbps.mp3"
        ),
        MusicTrackData(
            id = 2,
            title = "Tringa",
            subtitle = "Nature soundscape",
            duration = "4:00",
            emoji = "🌊",
            streamUrl = "https://neijocyrjpzswbyhhagw.supabase.co/storage/v1/object/public/Music/Tiranga%20Yodha%20320%20Kbps.mp3"
        ),
        MusicTrackData(
            id = 3,
            title = "All the Star",
            subtitle = "Gentle melody",
            duration = "5:10",
            emoji = "🎹",
            streamUrl = "https://neijocyrjpzswbyhhagw.supabase.co/storage/v1/object/public/Music/hello.mp3"
        ),
        MusicTrackData(
            id = 4,
            title = "I love me like you Do",
            subtitle = "Rain & birds ambience",
            duration = "6:30",
            emoji = "🌧️",
            streamUrl = "https://neijocyrjpzswbyhhagw.supabase.co/storage/v1/object/public/Music/Love_Me_Like_You_Do2018.mp3"
        )
    )

    // ── Free meditation/relaxation videos (direct MP4 from public CDN) ────────
    val meditationVideos = listOf(
        MeditationVideo(
            id = 1,
            title = "Guided Breathing",
            description = "5-minute box breathing exercise",
            thumbnail = "🌬️",
            duration = "5:00",
            streamUrl = "https://www.w3schools.com/html/mov_bbb.mp4"   // Replace with real content
        ),
        MeditationVideo(
            id = 2,
            title = "Nature Walk",
            description = "Calming forest scenery",
            thumbnail = "🌲",
            duration = "8:00",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        ),
        MeditationVideo(
            id = 3,
            title = "Sunrise Meditation",
            description = "Morning mindfulness",
            thumbnail = "🌅",
            duration = "10:00",
            streamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        )
    )
}