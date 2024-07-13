package com.example.realtimeserivce.ui.match

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.navigation.fragment.findNavController
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Response

class MatchFragment : Fragment() {
    private lateinit var fragmentMatchBinding: FragmentMatchBinding
    private val viewModel: MatchViewModel by viewModels()
    private val args: MatchFragmentArgs by navArgs()
    private val auth = Firebase.auth
    private lateinit var timer: Timer
    private lateinit var matchAdapter: MatchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentMatchBinding = FragmentMatchBinding.inflate(layoutInflater, container, false)
        viewModel.setMatch(args.id)
        timer = Timer()
        matchAdapter = MatchAdapter(mutableListOf())

        fragmentMatchBinding.tvMatchName.text = args.id
        fragmentMatchBinding.matchRecycler.adapter = matchAdapter
        fragmentMatchBinding.matchRecycler.layoutManager = LinearLayoutManager(context)

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
                // result, failed 반환 받기 checkword 메서드는 비동기 함수 -> 코루틴으로 처리
                CoroutineScope(Dispatchers.Main).launch {
                    timer.cancelTimer()
                    val result = viewModel.checkWord(sendValue)
                    if (result != "failed") {
                        // 다음 플레이어 턴을 진행시킨다. -> 턴 진행은 matchId의 데이터 값 sender의 변경에 따라 처리하는 것으로 이전
                        // result를 database에 등록시켜준다.
                        viewModel.sendFilterWord(result, args.id)
                    } else {
                        viewModel.sendFilterWord(result, args.id)

                        withContext(Dispatchers.Main) {
                            // 승리/패배화면 호출하기 -> 승리/패배 기록 메서드 호출하기
                            delay(2500)
                            viewModel.destroyMatch(args.id)
                        }
                    }
                }
            }
        }

        // 해당 matchid에 포함된 단어에 변경사항이 생기면 word (livedata)를 통해 전달해 뷰에 호출한다.
        viewModel.wordListChange(args.id)
        viewModel.word.observe(viewLifecycleOwner) {
            matchAdapter.fetchWords(it)
            fragmentMatchBinding.matchRecycler.scrollToPosition(it.size - 1)
            // 리스트가 없다는 것은 word 객체가 생성 전이기 때문에 선공권을 특정해 제공한다.
            if (it.isEmpty()) {
                val matchIdAntiFirstUser = args.id.replaceFirst(Regex("\\s.*"), "")
                isMyTurn(matchIdAntiFirstUser)
            } else {
                // livedata로 전달받은 리스트의 마지막 word 객체에 포함된 sender를 받아온다
                val lastSender = it.last().sender!!
                isMyTurn(lastSender)

                // 마지막 단어가 failed 일경우 uid 판단해서 승리/패배 결정
                val isFail = it.last().value!!
                if (isFail == "failed") {
                    showResultsFromFail(lastSender)
                } else return@observe
            }
        }

        return fragmentMatchBinding.root
    }

    // 현재 사용자의 턴인지 구분하고 edittext를 활성/비활성화 시켜주는 메서드
    private fun isMyTurn(lastSender: String) {
        val wordInput = fragmentMatchBinding.etMatch
        val limit = fragmentMatchBinding.tvTimeLeft

        // 매칭Id를 기준으로 뒤에있는 user uid의 턴부터 시작한다.
        if (lastSender != auth.uid) {
            fragmentMatchBinding.matchInputLayout.error = null
            startTimer(wordInput, limit)
        } else {
            fragmentMatchBinding.etMatch.isEnabled = false
            fragmentMatchBinding.matchInputLayout.error = "its not my turn"
        }
    }

    // 틀린 단어로 승리-패배 화면 호출하는 메서드
    private fun showResultsFromFail(loser: String) {
        if (loser != auth.uid) showWinDialog() else {
            showLoseDialog()
            // 패배한 쪽에서 viewmodel 승리-패배 기록하는 메서드 작동
            viewModel.writeResults(loser, args.id)
        }
        timer.cancelTimer()
    }
    // 시간 소모로 인한 승리-패배 화면 호출하는 메서드 - 불필요
    private fun showResultsFromTimer(loser: String) {}
    
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
                CoroutineScope(Dispatchers.Main).launch{
                    wordInput.setText("")
                    wordInput.isEnabled = false
                    limit.text = "End"

                    // 타이머 종료 시, 승리-패배화면 호출하기
                    // 타이머 종료 메서드를 별도로 만들기보다는 타이머 종료 시, failed를 자동으로 전송해 패배화면 호출
                    viewModel.sendFilterWord("failed", args.id)
                    delay(2500)
                    viewModel.destroyMatch(args.id)
                }
            }
        )
    }
    private fun showWinDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Victory")
        builder.setMessage("Congratulations You won the game :D")

        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss() // 다이얼로그를 닫습니다.
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }
    private fun showLoseDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Defeat")
        builder.setMessage("You lose the game try again :C")

        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss() // 다이얼로그를 닫습니다.
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    // 종료 후, 매치 대기열 화면으로 이동하는 메서드
    private fun moveToWait() {
        findNavController().popBackStack()
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onStop() {
        super.onStop()
        // match fragment가 화면에 보이지않으면 matchId를 삭제 후 match wait fragment로 이동
        timer.cancelTimer()
        viewModel.destroyMatch(args.id)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.observeMatchDestroy(args.id)
        // 해당 match id를 감지해 match가 사라지게되면 match wait fragment로 이동하도록 구현
        viewModel.isMatchExists.observe(viewLifecycleOwner) { onMatch ->
            if (onMatch) {
                return@observe
            } else {
                moveToWait()
            }
        }
    }
}