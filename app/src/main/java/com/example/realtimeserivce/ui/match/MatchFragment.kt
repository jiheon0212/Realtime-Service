package com.example.realtimeserivce.ui.match

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.example.realtimeserivce.adapter.Timer
import com.example.realtimeserivce.databinding.FragmentMatchBinding
import com.example.realtimeserivce.ency.EncyResponse
import com.example.realtimeserivce.ency.EncyService
import com.example.realtimeserivce.ency.NaverInformation
import com.example.realtimeserivce.viewmodel.MatchViewModel
import retrofit2.Call
import retrofit2.Response

class MatchFragment : Fragment() {
    private lateinit var fragmentMatchBinding: FragmentMatchBinding
    private val viewModel: MatchViewModel by viewModels()
    private lateinit var timer: Timer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentMatchBinding = FragmentMatchBinding.inflate(layoutInflater, container, false)
        timer = Timer()
        val wordInput = fragmentMatchBinding.etMatch
        val limit = fragmentMatchBinding.tvTimeLeft

        // match start 버튼 클릭 시, 실행
        fragmentMatchBinding.btnMatchStart.setOnClickListener {
            fragmentMatchBinding.tvTimeLeft.visibility = View.VISIBLE
            fragmentMatchBinding.btnMatchStart.visibility = View.GONE
            startTimer(wordInput, limit)
        }
        
        fragmentMatchBinding.etMatch.addTextChangedListener {
            fragmentMatchBinding.matchInputLayout.error = null
        }
        
        fragmentMatchBinding.matchInputLayout.setEndIconOnClickListener {
            val sendValue = fragmentMatchBinding.etMatch.text.toString()
            if (sendValue.isEmpty()) {
                fragmentMatchBinding.matchInputLayout.error = "message is empty"
            } else {
                checkWord(sendValue)
                fragmentMatchBinding.etMatch.setText("")
                hideKeyboard(it)
            }
        }
        return fragmentMatchBinding.root
    }

    // todo - 사용자의 턴 구분 메서드
    private fun isMyTurn(): Boolean {
        // todo - 매칭Id의 앞에 있는 user uid의 턴부터 시작한다.
        return true
    }
    // 타이머 작동 메서드
    @SuppressLint("SetTextI18n")
    private fun startTimer(wordInput: EditText, limit: TextView) {
        wordInput.isEnabled = true
        timer.startTimer(10000L,
            onTick = { timeLeft ->
                // 남은 시간 업데이트 해주기
                val secondsLeft = timeLeft / 1000
                limit.text = "Time limit: $secondsLeft seconds"
            },
            onFinish = {
                // 타이머 종료 시 입력 초기화 및 게임 종료 메서드 호출
                wordInput.setText("")
                wordInput.isEnabled = false
                limit.text = "End"
                matchEndCall()

                // todo - test용 code
                fragmentMatchBinding.btnMatchStart.visibility = View.VISIBLE
                fragmentMatchBinding.tvTimeLeft.visibility = View.GONE
            }
        )
    }
    // todo - 타이머가 끝난 이후 edittext 비활성화 해주는 메서드
    private fun banTypeWord() {

    }
    // 단어가 존재하는지 확인하는 메서드
    private fun checkWord(word: String) {
        val ency = EncyService.encyInterface.getResult(
            clientId = NaverInformation.CLIENT_ID,
            clientSecret = NaverInformation.CLIENT_SECRET,
            query = word
        )
        ency.enqueue(object: retrofit2.Callback<EncyResponse> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(p0: Call<EncyResponse>, p1: Response<EncyResponse>) {
                if (p1.isSuccessful) {
                    if (p1.body()?.items != null) {
                        p1.body()?.items?.forEach { itemSame ->
                            // html태그를 전부 제거하고 title, description을 받아온다.
                            val result = itemSame.trimResults
                            // 사용자가 입력한 단어 이외의 title에 넘어온 다른 데이터를 제거하며 일치하는 문구가 없을 경우에는 빈칸으로 변경한다
                            val isMatch = if (result.first.contains(word)) word else ""

                            // 빈칸이 호출되면 패배하며, 화면에 toast를 띄우며 진행한다.
                            if (isMatch != "") {
                                // todo - result를 match room id database에 push한다.
                                val result = "$isMatch:\n${result.second}"
                                // todo - user턴 변경하는 메서드 실행

                            } else {
                                Toast.makeText(context, "you lose $word doesn't exists", Toast.LENGTH_SHORT).show()
                                // 승리, 패배 메서드 실행
                                matchEndCall()
                            }
                        }
                    }
                }
            }

            override fun onFailure(p0: Call<EncyResponse>, p1: Throwable) {
                Toast.makeText(context, "$p1", Toast.LENGTH_SHORT).show()
            }

        })
    }

    // todo - 승리, 패배화면 호출 메서드
    private fun matchEndCall() {
        // todo - database에 승리, 패배 user uid를 기록한다.
        // database에 기록된 뒤 matchwait fragment로 복귀한다.
        moveToWait()
    }
    // todo - 종료 후, 매치 대기열 화면으로 이동하는 메서드
    private fun moveToWait() {

    }
    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}