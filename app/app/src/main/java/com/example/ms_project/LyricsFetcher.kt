package com.example.ms_project

import okhttp3.*
import org.json.JSONObject
import java.io.IOException

fun fetchLyrics(apiKey: String, artist: String, song: String, callback: (String?) -> Unit) {
    val client = OkHttpClient()

    val request = Request.Builder()
        .url("https://api.musixmatch.com/ws/1.1/matcher.lyrics.get?format=json&apikey=$apiKey&q_track=$song&q_artist=$artist")
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.body?.string()
            val lyricsBody = JSONObject(responseBody).getJSONObject("message")
                .getJSONObject("body")
                .getJSONObject("lyrics")
                .getString("lyrics_body")
            callback(lyricsBody)
        }

        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            callback(null)
        }
    })
}

