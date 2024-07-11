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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.realtimeserivce.adapter.MatchAdapter
import com.example.realtimeserivce.adapter.Timer
import com.example.realtimeserivce.data.MatchResult
import com.example.realtimeserivce.databinding.FragmentMatchBinding
import com.example.realtimeserivce.ency.EncyResponse
import com.example.realtimeserivce.ency.EncyService
import com.example.realtimeserivce.ency.NaverInformation
import com.example.realtimeserivce.ui.main.MessageFragmentArgs
import com.example.realtimeserivce.viewmodel.MatchViewModel
import retrofit2.Call
import retrofit2.Response

class MatchFragment : Fragment() {
    private lateinit var fragmentMatchBinding: FragmentMatchBinding
    private val viewModel: MatchViewModel by viewModels()
    private val args: MatchFragmentArgs by navArgs()
    private lateinit var timer: Timer
    private lateinit var matchAdapter: MatchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentMatchBinding = FragmentMatchBinding.inflate(layoutInflater, container, false)
        matchAdapter = MatchAdapter(mutableListOf())
        timer = Timer()
        val wordInput = fragmentMatchBinding.etMatch
        val limit = fragmentMatchBinding.tvTimeLeft

        fragmentMatchBinding.matchRecycler.adapter = matchAdapter
        fragmentMatchBinding.matchRecycler.layoutManager = LinearLayoutManager(context)

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
                fragmentMatchBinding.etMatch.setText("")
                hideKeyboard(it)
                // todo - result, failed 반환 받기 checkword 메서드는 비동기 함수 -> 코루틴으로 처리
                // todo - result 수정해야됨
                val result: String = viewModel.checkWord(sendValue).toString()
                if (result != "failed") {
                    // result를 database에 등록시켜준다.
                    // todo - 다음 플레이어 턴을 진행시킨다.
                    viewModel.sendFilterWord(result, args.id)
                } else {
                    /* todo - 게임 결과 기록 & 대기 창으로 이동
                        viewModel.writeResults(MatchResult("",""))
                        moveToWait() */
                }
            }
        }

        viewModel.word.observe(viewLifecycleOwner) {
            matchAdapter.fetchWords(it)
            fragmentMatchBinding.matchRecycler.scrollToPosition(it.size - 1)
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
                // 남은 시간을 업데이트 해주는 부분
                val secondsLeft = timeLeft / 1000
                limit.text = "Time limit: $secondsLeft seconds"
            },
            onFinish = {
                // 타이머 종료 시 패배로 기록되며, 입력 초기화 및 게임 종료 단계를 동작시켜준다.
                wordInput.setText("")
                wordInput.isEnabled = false
                limit.text = "End"

                /* todo - 게임 결과 기록 & 대기 창으로 이동
                    viewModel.writeResults(MatchResult("",""))
                    moveToWait() */

                // todo - test용 code 삭제 예정
                fragmentMatchBinding.btnMatchStart.visibility = View.VISIBLE
                fragmentMatchBinding.tvTimeLeft.visibility = View.GONE
            }
        )
    }
    // todo - 종료 후, 매치 대기열 화면으로 이동하는 메서드
    private fun moveToWait() {

    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}