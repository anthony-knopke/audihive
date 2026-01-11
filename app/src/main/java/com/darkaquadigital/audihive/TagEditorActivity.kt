package com.darkaquadigital.audihive

import android.app.Activity
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import com.darkaquadigital.audihive.data.Song
import com.darkaquadigital.audihive.ui.screens.TagEditorScreen
import com.darkaquadigital.audihive.utilities.updateAudioFileTags
import com.darkaquadigital.audihive.utilities.updateTags

class TagEditorActivity : ComponentActivity() {

    private var pendingIntentSender: IntentSender? = null
    private var pendingSong: Song? = null
    private var pendingFilePath: String? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val song = intent.getParcelableExtra("song", Song::class.java)

        setContent {
            MaterialTheme {
                if (song != null) {
                    TagEditorScreen(song,
                        onSave = {
                            updateAudioFileTags(
                                context = this,
                                filePath = song.data,
                                song = it
                            )
                        },
                        onCancel = { finish() })
                }
            }
        }
    }

    private val editPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Retry the tag update after user grants access
                retryPendingTagSave()
            }
        }

    private fun retryPendingTagSave() {
        val song = pendingSong
        val filePath = pendingFilePath
        Log.d("TagEditorActivity", "Retrying for song $filePath")
        if (song != null && filePath != null) {
            updateTags(
                context = this,
                filePath = filePath,
                song = song,
                onRecoverableSecurityException = { intentSender ->
                    // Ideally this doesn't get triggered again
                    editPermissionLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                }
            )
        }
    }

}