package com.example.tippy

import android.animation.ArgbEvaluator
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.tippy.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.math.ceil

private const val TAG = "MainActivity"
private const val INIT_TIP_PERCENT = 15

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var etBaseAmount: EditText
    private lateinit var tipSeekBar: SeekBar
    private lateinit var tvTipAmount: TextView
    private lateinit var tvTotalAmount: TextView
    private lateinit var tvPercentLabel: TextView
    private lateinit var tvTipDescripton: TextView
    private lateinit var etSplitPeople: EditText
    private lateinit var tvSplitValue: TextView
    private lateinit var copyButton: FloatingActionButton
    private lateinit var shareButton: FloatingActionButton

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        etBaseAmount = binding.etBaseAmount
        tipSeekBar = binding.tipSeekBar
        tvTipAmount = binding.tvTipAmount
        tvTotalAmount = binding.tvTotalAmount
        tvPercentLabel = binding.tvPercentLabel
        tvTipDescripton = binding.tvTipDescripton
        etSplitPeople = binding.etSplitPeople
        tvSplitValue = binding.tvSplitValue
        copyButton = binding.copyButton
        shareButton = binding.shareButton

        tipSeekBar.progress = INIT_TIP_PERCENT
        tvPercentLabel.text = "$INIT_TIP_PERCENT %"

        updateTipDescription(INIT_TIP_PERCENT)

        tipSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.i(TAG, "onProgressChanged $progress")
                tvPercentLabel.text = "$progress %"
                computeTipAndTotal()
                updateTipDescription(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}

        })

        etBaseAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                Log.i(TAG, "afterTextChanged $s")
                computeTipAndTotal()
            }

        })

        etSplitPeople.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                Log.i(TAG, "afterTextChanges $s")
                computeSplit()
            }

        })

        copyButton.setOnClickListener {
            val textToCopy = generateTextToCopy()
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", textToCopy)
            clipboardManager.setPrimaryClip(clipData)

            Toast.makeText(this, "Copied $textToCopy", Toast.LENGTH_LONG).show()
        }
        shareButton.setOnClickListener {
            val textToCopy = generateTextToCopy()
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, textToCopy)
            intent.type = "text/plain"
            startActivity(Intent.createChooser(intent, "Share To:"))
        }

    }

    private fun computeSplit() {
        if (etSplitPeople.text.isEmpty() || tvTotalAmount.text.isEmpty()) {
            tvSplitValue.text = ""
            return
        }
        val splitAmount =
            ceil(tvTotalAmount.text.toString().toFloat() / etSplitPeople.text.toString().toInt())
        tvSplitValue.text = splitAmount.toString()
    }

    private fun updateTipDescription(progress: Int) {
        val message = when (progress) {
            in 0..9 -> "Poor"
            in 10..20 -> "Acceptable"
            in 21..25 -> "Good"
            else -> "Great"
        }
        binding.tvTipDescripton.text = message

        //Interpolation to determine the color for tipDescrption
        val color = ArgbEvaluator().evaluate(
            progress.toFloat() / tipSeekBar.max,
            ContextCompat.getColor(this, R.color.color_worst_tip),
            ContextCompat.getColor(this, R.color.color_best_tip)
        ) as Int
        tvTipDescripton.setTextColor(color)
    }

    private fun computeTipAndTotal() {
        //GET the tip and base
        if (etBaseAmount.text.isEmpty()) {
            tvTipAmount.text = ""
            tvTotalAmount.text = ""
            return
        }
        val baseAmount = etBaseAmount.text.toString().toDouble()
        val tipPercent = tipSeekBar.progress
        //Compute tip and total
        val tipAmount = ceil(baseAmount * tipPercent / 100)
        val totalAmount = baseAmount + tipAmount
        //Update the ui
        val formatter = "%.2f"
        tvTipAmount.text = formatter.format(tipAmount)
        tvTotalAmount.text = formatter.format(totalAmount)
        if (etSplitPeople.text.isNotEmpty())
            computeSplit()
    }

    private fun generateTextToCopy(): CharSequence? {
        var textToCopy = "Total 0.00 = Base 0.00 + Tip 0.00"
        if (tvSplitValue.text.isNotEmpty())
            textToCopy =
                "Total ${tvTotalAmount.text} = Base ${etBaseAmount.text} + Tip ${tvTipAmount.text}  •  Per Person ${tvSplitValue.text}"
        else if (tvTotalAmount.text.isNotEmpty())
            textToCopy =
                "Total ${tvTotalAmount.text} = Base ${etBaseAmount.text} + Tip ${tvTipAmount.text}"
        return textToCopy
    }
}