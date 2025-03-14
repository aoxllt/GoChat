package com.example.gochat.ui.user

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.gochat.databinding.ActivityCaptchBinding
import com.example.gochat.viewmodel.CaptchViewModel
import com.example.myapp.ui.viewmodel.RegisterViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class CaptchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCaptchBinding
    private lateinit var countDownTimer: CountDownTimer
    private var timeRemaining = 60 // 倒计时总时长（秒）
    private val viewModel: CaptchViewModel by viewModel() // 假设 Koin 已注入 deviceId
    private val resviewModel : RegisterViewModel by viewModel()

    companion object {
        private const val COUNTDOWN_TOTAL_MS = 60_000L // 60秒
        private const val COUNTDOWN_INTERVAL_MS = 1_000L // 1秒间隔
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCaptchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        // 获取传入的 email
        val email = intent.getStringExtra("email") ?: ""
        binding.tvMessage.text = "已向 $email 发送验证码"

        // 设置点击事件
        setupClickListeners(email)

        // 初始化验证码输入框
        initCodeInputs(email)

        // 启动倒计时
        initCountDownTimer()
    }

    private fun setupClickListeners(email: String) {
        binding.backbtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvResend.setOnClickListener {
            resetCountDown()
            resviewModel.register(email){isSucess->
                if(isSucess){
                    Toast.makeText(this, "验证码已重新发送至 $email", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, "服务器错误", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 初始化验证码输入框的焦点切换逻辑
     */
    private fun initCodeInputs(email: String) {
        val codeInputs = arrayOf(
            binding.etCode1,
            binding.etCode2,
            binding.etCode3,
            binding.etCode4
        )

        codeInputs.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    when {
                        s?.length == 1 -> handleInputFocus(index, email, codeInputs)
                        s.isNullOrEmpty() && index > 0 -> handleDeleteFocus(index, codeInputs)
                    }
                }
            })

            // 处理删除键的逻辑
            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && event.action == android.view.KeyEvent.ACTION_DOWN) {
                    if (editText.text.isEmpty() && index > 0) {
                        val prevInput = codeInputs[index - 1]
                        prevInput.text.clear() // 删除上一个输入框的内容
                        prevInput.requestFocus()
                    }
                }
                false
            }
        }
    }

    /**
     * 处理输入完成后的焦点切换
     */
    private fun handleInputFocus(currentIndex: Int, email: String, inputs: Array<EditText>) {
        if (currentIndex < inputs.lastIndex) {
            inputs[currentIndex + 1].apply {
                requestFocus()
                setSelection(text.length)
            }
        } else {
            verifyCode(email)
        }
    }

    /**
     * 处理删除时的焦点回退
     */
    private fun handleDeleteFocus(currentIndex: Int, inputs: Array<EditText>) {
        inputs[currentIndex - 1].apply {
            requestFocus()
            setSelection(text.length)
        }
    }

    /**
     * 初始化倒计时器
     */
    private fun initCountDownTimer() {
        countDownTimer = object : CountDownTimer(COUNTDOWN_TOTAL_MS, COUNTDOWN_INTERVAL_MS) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemaining = (millisUntilFinished / 1000).toInt()
                binding.tvResend.apply {
                    text = "重新发送($timeRemaining)"
                    isEnabled = false
                    setTextColor(Color.parseColor("#888888"))
                }
            }

            override fun onFinish() {
                binding.tvResend.apply {
                    isEnabled = true
                    setTextColor(Color.parseColor("#0099FF"))
                    text = "重新发送验证码"
                }
            }
        }.start()
    }

    /**
     * 重置倒计时
     */
    private fun resetCountDown() {
        countDownTimer.cancel()
        timeRemaining = 60
        initCountDownTimer()
    }

    /**
     * 验证验证码
     */
    private fun verifyCode(email: String) {
        val code = buildString {
            append(binding.etCode1.text)
            append(binding.etCode2.text)
            append(binding.etCode3.text)
            append(binding.etCode4.text)
        }
        if (code.length != 4) {
            Toast.makeText(this, "请输入完整的 4 位验证码", Toast.LENGTH_SHORT).show()
            return
        }

        // 禁用输入框，防止重复提交
        setInputsEnabled(false)

        // 调用 ViewModel 验证
        viewModel.verify(email, code) { isSuccess ->
            if (isSuccess.equals("true")) {
                Toast.makeText(this, "验证码验证成功", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, UserinfoaddActivity::class.java)
                intent.putExtra("email", email)
                startActivity(intent)
            } else {
                Toast.makeText(this, isSuccess, Toast.LENGTH_SHORT).show()
                clearCodeInputs()
                setInputsEnabled(true)
            }
        }
    }

    /**
     * 设置输入框是否启用
     */
    private fun setInputsEnabled(enabled: Boolean) {
        binding.etCode1.isEnabled = enabled
        binding.etCode2.isEnabled = enabled
        binding.etCode3.isEnabled = enabled
        binding.etCode4.isEnabled = enabled
    }

    /**
     * 清空验证码输入框
     */
    private fun clearCodeInputs() {
        binding.etCode1.text.clear()
        binding.etCode2.text.clear()
        binding.etCode3.text.clear()
        binding.etCode4.text.clear()
        binding.etCode1.requestFocus()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
    }
}