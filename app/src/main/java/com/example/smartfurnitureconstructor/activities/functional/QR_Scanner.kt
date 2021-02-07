package com.example.smartfurnitureconstructor.activities.functional

import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.budiyev.android.codescanner.*
import com.example.smartfurnitureconstructor.R
import com.example.smartfurnitureconstructor.activities.base.ActivityWithTopMenu
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_q_r__scanner.*
import kotlinx.android.synthetic.main.fragment_unfolding_item.view.*
import org.json.JSONArray
import java.net.URL
import kotlin.properties.Delegates

private const val CAMERA_REQUEST_CODE = 101

class QR_Scanner : ActivityWithTopMenu() {
    private var constructionId by Delegates.notNull<Int>()
    private lateinit var codeScanner: CodeScanner
    private lateinit var constructionName: String
    private val instructions: ArrayList<String> = arrayListOf()
    val componentsToScan: ArrayList<String> = arrayListOf()
    private val instructionImages: ArrayList<String> = arrayListOf()
    private lateinit var instructionMessage: InstructionMessage
    var currentIterationNumber: Int = 0
    var enableScanning: Boolean = false
    var componentsCount by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_q_r__scanner)
        constructionId = intent.getIntExtra("constructionId", 0)
        setupPermissions()
        codeScanner()
        getConstructionInstruction()
    }

    private fun codeScanner() {
        codeScanner = CodeScanner(this, scanner_view)
        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS
            autoFocusMode = AutoFocusMode.CONTINUOUS
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true
            isFlashEnabled = false

            decodeCallback = DecodeCallback {
                runOnUiThread {
                    tv_textView.text = it.text
                    if (currentIterationNumber < componentsCount &&
                        it.text == componentsToScan[currentIterationNumber]
                        && enableScanning) {
                        enableScanning = false
                        ++currentIterationNumber
                        showCurrentAlertMessage()
                    }
                }
            }

            errorCallback = ErrorCallback {
                runOnUiThread {
                    Log.e("QR_Scanner", "Camera initialization error: ${it.message}")
                }
            }

            scanner_view.setOnClickListener {
                codeScanner.startPreview()
            }
        }
    }

    private fun showCurrentAlertMessage() {
        instructionMessage =
            InstructionMessage(
                instructions[currentIterationNumber],
                instructionImages[currentIterationNumber], this
            )
        instructionMessage.show(supportFragmentManager,
            "instructionMessage$currentIterationNumber")
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
            android.Manifest.permission.CAMERA)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() ||
                    grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "YYY", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getConstructionInstruction() {
        val url = "http://192.168.0.104:9595/api/user_construction_list/$constructionId/"

        val queue = Volley.newRequestQueue(this)
        val req = JsonObjectRequest(Request.Method.GET, url, null,
            Response.Listener { response ->
                constructionName = response.getString("name")
                val instructionArray: JSONArray = response.getJSONArray("instruction")
                instructions.add(instructionArray.getString(0))
                var instruction: JSONArray
                for (k in 1 until instructionArray.length()) {
                    instruction = instructionArray.getJSONArray(k)
                    componentsToScan.add(instruction.getString(0))
                    instructions.add(instruction.getString(1))
                }
                val images: JSONArray = response.getJSONArray("images")
                for (k in 0 until images.length()) {
                    instructionImages.add(images.getString(k))
                }
                componentsCount = componentsToScan.size
                showCurrentAlertMessage()
            }, Response.ErrorListener {
            })
        queue.add(req)
    }
}

class InstructionMessage(message: String, image: String, scanner: QR_Scanner) : DialogFragment() {
    private val messageText = message
    private val imageURL = image
    private val qrScanner = scanner

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)

            val alertBody: LinearLayout = LinearLayout(context)
            alertBody.orientation = LinearLayout.VERTICAL
            alertBody.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )

            val params: LinearLayout.LayoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
            params.setMargins(10, 50, 10, 50)

            val messageBox: TextView = TextView(context)
            messageBox.text = messageText
            messageBox.textSize = 16F
            messageBox.gravity = Gravity.CENTER_HORIZONTAL
            messageBox.layoutParams = params
            alertBody.addView(messageBox)

            val instructionImage: ImageView = ImageView(context)
            val url: URL = URL("http://192.168.0.104:9595/images/$imageURL")
            Picasso.get().load(url.toString()).into(instructionImage)
            alertBody.addView(instructionImage)

            builder.setTitle(resources.getString(R.string.make_up_the_furniture_title))
                .setView(alertBody)
                .setPositiveButton(R.string.catalog_item_ok
                ) { _, _ ->
                    qrScanner.enableScanning = true
                    if (qrScanner.currentIterationNumber == qrScanner.componentsCount) {
                        qrScanner.finish()
                    }
                    else {
                        qrScanner.textView52.text = qrScanner.componentsToScan[qrScanner.currentIterationNumber]
                    }
                }
            val alert = builder.create()
            alert.setOnShowListener {
                val btnPositive = alert.getButton(Dialog.BUTTON_POSITIVE)
                btnPositive.textSize = 17F
                val btnNegative = alert.getButton(Dialog.BUTTON_NEGATIVE)
                btnNegative.textSize = 17F
            }

            return alert
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun addCheckBoxesToView(view: View,
                                    itemSet: MutableSet<String>,
                                    filterParamsSet: MutableSet<String>) {
        for (i in itemSet) {
            val currentCheckBox: CheckBox = CheckBox(context)
            currentCheckBox.text = i
            currentCheckBox.setOnClickListener {
                if (currentCheckBox.isChecked) {
                    filterParamsSet.add(currentCheckBox.text.toString())
                } else {
                    filterParamsSet.remove(currentCheckBox.text.toString())
                }
            }
            view.unfoldingItemListLayout.addView(currentCheckBox)
        }
    }

    private fun setListeners(button: Button, expandedView: ConstraintLayout, card: CardView) {
        button.setOnClickListener {
            if (expandedView.visibility == View.GONE) {
                TransitionManager.beginDelayedTransition(
                    card, AutoTransition()
                )
                expandedView.visibility = View.VISIBLE
                button.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
            } else {
                TransitionManager.beginDelayedTransition(
                    card, AutoTransition()
                )
                expandedView.visibility = View.GONE
                button.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
            }
        }
    }
}