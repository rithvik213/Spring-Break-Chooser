package com.example.springbreakchooser

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var languageSelectionGroup: RadioGroup
    private lateinit var speechOutputText: EditText

    //add a mapping for each radio button to its respective language code
    private val languageCodes = mapOf(
        R.id.spanishButton to "es-ES",
        R.id.frenchButton to "fr-FR",
        R.id.chineseButton to "zh-CN",
        R.id.russianButton to "ru-RU",
        R.id.germanButton to "de-DE",
        R.id.teluguButton to "te-IN"
    )

    //speech result launcher
    private val speechResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val spokenText = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull() ?: ""
            speechOutputText.setText(spokenText)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        languageSelectionGroup = findViewById(R.id.languageRadioGroup)
        speechOutputText = findViewById(R.id.textOutput)

        //listen for when user selects language
        languageSelectionGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedLanguageCode = languageCodes[checkedId] ?: RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            launchSpeechInput(selectedLanguageCode)
        }
    }

    private fun launchSpeechInput(languageCode: String) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, languageCode)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, languageCode)
        }
        speechResultLauncher.launch(intent)
    }
}
