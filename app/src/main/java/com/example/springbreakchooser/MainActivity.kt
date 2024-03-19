package com.example.springbreakchooser

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.EditText
import android.widget.RadioGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var groupLanguageSelection: RadioGroup
    private lateinit var editTextSpeechOutput: EditText


    //i am using english as default
    private var codeSelectedLanguage: String = "en-US"

    //sensor information for shake detection
    private lateinit var managerSensor: SensorManager
    private var sensorAccelerometer: Sensor? = null
    private val thresholdShake = 800
    private var timeLastUpdate: Long = 0
    private var axisLastX: Float = 0.0f
    private var axisLastY: Float = 0.0f
    private var axisLastZ: Float = 0.0f


    //map the buttons to each language specific language codes
    private val mapLanguageCodes = mapOf(
        R.id.spanishButton to "es-ES",
        R.id.frenchButton to "fr-FR",
        R.id.chineseButton to "zh-CN",
        R.id.russianButton to "ru-RU",
        R.id.germanButton to "de-DE",
        R.id.teluguButton to "te-IN"
    )

    private val mapLocationLanguages = mapOf(
        //for spanish, Cancun, Mexico
        "es-ES" to "geo:21.1619,-86.8515",

        //for french, Lyon, France
        "fr-FR" to "geo:45.7640,4.8357",

        //for chinese, Hong Kong, China
        "zh-CN" to "geo:22.3193,114.1694",

        //for russian, Moscow, Russia
        "ru-RU" to "geo:55.7558,37.6173",

        //for german, Luxembourg, Luxembourg
        "de-DE" to "geo:49.6116,6.1319",

        //for telugu, Vijayawada, India
        "te-IN" to "geo:16.5062,80.6480"
    )

    private val launcherSpeechResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val textSpoken = result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull() ?: ""
            editTextSpeechOutput.setText(textSpoken)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        groupLanguageSelection = findViewById(R.id.languageRadioGroup)
        editTextSpeechOutput = findViewById(R.id.textOutput)
        setupManagerSensor()

        groupLanguageSelection.setOnCheckedChangeListener { _, checkedId ->
            val languageCode = mapLanguageCodes[checkedId]
            codeSelectedLanguage = languageCode ?: "en-US"
            initiateSpeechInput(languageCode)
        }
    }

    private fun setupManagerSensor() {
        managerSensor = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorAccelerometer = managerSensor.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        managerSensor.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    private fun initiateSpeechInput(languageCode: String?) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
        }
        launcherSpeechResult.launch(intent)
    }

    override fun onResume() {
        super.onResume()
        managerSensor.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_UI)
    }

    override fun onPause() {
        super.onPause()
        managerSensor.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val currentTime = System.currentTimeMillis()
        if ((currentTime - timeLastUpdate) > 100) {
            val diffTime = (currentTime - timeLastUpdate).toFloat()
            timeLastUpdate = currentTime

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val speed = Math.abs(x + y + z - axisLastX - axisLastY - axisLastZ) / diffTime * 10000

            if (speed > thresholdShake) {
                codeSelectedLanguage?.let {
                    mapLocationLanguages[it]?.also { uri ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                        startActivity(intent)
                        playGreetingForLanguage(codeSelectedLanguage)
                    }
                }
            }

            axisLastX = x
            axisLastY = y
            axisLastZ = z
        }
    }

    private fun playGreetingForLanguage(languageCode: String?) {
        val resourceId = when (languageCode) {
            "es-ES" -> R.raw.spanish_hello
            "fr-FR" -> R.raw.french_hello
            "zh-CN" -> R.raw.chinese_hello
            "ru-RU" -> R.raw.russian_hello
            "de-DE" -> R.raw.german_hello
            "te-IN" -> R.raw.telugu_hello
            else -> null
        }

        resourceId?.let {
            val mediaPlayer = MediaPlayer.create(this, it)
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { mp ->
                mp.release()
            }
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}
