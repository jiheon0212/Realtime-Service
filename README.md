## 끝말잇기 게임

### 필요 기초 기능

- 현재 접속 중인 사용자 파악
- 1대1 대화
- 사용자 정보 불러오기
- 입력한 단어의 존재 여부 파악

### 상세 기능

- 현재 접속 중인 사용자 파악
	1. 접속 중인 사용자 중 매치 대기열을 활성화 했는지 파악
	2. 접속 중인 사용자를 Main화면 상단 패널에 보여주는 기능
	3. 대기열 활성화를 해당 화면에 현재 포커스가 있는지 구분하여 on/off 상태를 변경해준다.
- 1대1 대화
	1. Main화면 상단 패널을 통해 별도의 대화방 개설 기능
	2. 사용자의 Uid, 대화, 보낸 시간이 기록되게 저장
	3. 보낸사람과 받는사람을 구분해 메세지 View 방향을 별도로 지정하는 기능
	4. 기존 대화가 사라지지않고 대화창의 시점은 항상 최신 대화로 가도록 보여주기
- 사용자 정보 불러오기
	1. 사용자의 프로필 사진, 닉네임을 보여주는 기능
	2. 사용자의 전적 기록을 보여주는 기능
	3. 차단한 사용자 여부를 파악하는 기능
	4. 친구 등록 기능
- 입력한 단어의 존재 여부 파악
	1. 백과사전 Api에서 해당 단어에 대한 데이터를 받아와 처리하는 기능
	2. 단어가 존재하지 않을 시, 패배화면을 대화방에 보여주는 기능
	3. 단어가 존재하면, 상대방에게 입력 타이머를 호출하는 기능
- 나를 대상으로한 채팅방 파악
	1. Database에서 현재 로그인 되어있는 사용자가 포함된 채팅방 가져오는 기능
	2. Recyclerview로 보여주고 해당 채팅방 터치 시, 기존 대화를 상대방과 이어갈 수 있도록 구분된 chatroom 기능
	3. 사용자 Status를 표시하는 아이콘을 눌러 시작한 대화방과 채팅방 Id를 동일하게 유지해 어떤 방법으로 접근해도 같은 대화가 지속되는 기능
- 1대1 매칭시스템
	1. User Status Data를 전부 받아오기 -> Online인 유저를 리스트에 담아주기
	2. Online User List에서 랜덤 두명 선택해주는 기능
	3. Navcontroller를 통해 Match Container로 변경해주기
	4. Match Fragment 진입 -> Encyclopedia Api/타이머 적용시킨 한번씩 송수신하는 화면
	5. 결과에 따라 승리/패배화면 호출하는 기능
	6. 결과화면 이후 N초뒤 자동으로 Main Container로 이동하는 기능
	

*Jetpack safeargs 사용을 위해 필요한 project.gradle*
```
buildscript {
    dependencies {
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.7.7")
    }
}
```

### 버그

- Navcontroller를 통해 이동 시 뒤로가기 버튼을 클릭하면 백스택이 유지가 안되는 문제
```
-> onBackPressedDispatcher를 통해 필요한 navcontroller에 백스택으로 돌아가는 callback을 등록해준다. - 완료
```
- Onresume에 등록해놓은 userStatus 변경 코드가 다른 container가 동작 중에도 같이 동작해 online으로 표기되는 문제
```
-> userStatus 변경을 처리하는 fragment의 container가 view.visible 상태일 때만 동작하도록 boolean 반환 메서드를 추가로 등록해 해당 container에서만 동작하도록 변경 - 완료
```
- 채팅방에 1대1 대화 시, 서로 같은 유저에게 보내는 채팅이 각각의 이름으로 채팅방을 두개 형성하는 문제
```
-> 채팅방 Id를 하나로 생성하는 방법 찾기
-> 채팅방 Id를 반환해주는 메서드에 양쪽 uid 문자열의 크기를 비교하여 큰 쪽이 항상 앞으로 오도록 조건문을 추가해 항상 같은 채팅방 Id를 사용할 수 있도록 변경 - 완료 
```
- 1대1 대화창에서 메세지를 전송하고 갱신될 때는 포커스가 최하단에 잘 맞춰지지만 키보드가 내려가면서 최하단에 있던 포커스도 같이 사라지는 문제
```
-> recycler view에 delay를 주어 실행하면 해결되지만 ui에 별로 좋지 못한거 같아 다른 방법 찾기
-> recycler view는 자체적으로 스크롤이 가능했다... scroll view로 감싸주었던 것이 동작 에러의 원인으로 제거하니 포커스가 잘 유지된다.
```
- 내가 포함되지않은 채팅방 목록이 호출되는 문제
```
-> if (it.key?.contains(auth.uid!!)!!)를 view model에 추가해 현재 사용자의 Uid가 포함된 경우에만 가져오도록 변경 - 완료
```

- 2명 이상일 경우 매칭시스템이 동작하는 방식으로 진행하면 3번째 혹은 4번째 플레이어가 리스트에 담기기 전에 실행되어 데이터가 누락되는 문제
```
-> callback 혹은 coroutine을 통해 작업완료 후 매칭시스템이 동작할 수 있는 순서를 보장받는 방식이 필요하다.
CoroutineScope(Dispatchers.IO).launch {
                status.forEach {
                    if (it.status == "online") {
                        onlinePlayers.add(it.user!!)
                    }
                }
                // 변경된 각각의 데이터에 대해 online인 유저가 2명 이상일 때 searchMatch 메서드가 동작하도록 구현
                if (onlinePlayers.size >= 2) {
                    searchMatch(onlinePlayers)
                }
                // onlinePlayers를 랜덤에 전달한 후에 초기화 시켜준다
                onlinePlayers.clear()
            }
-> coroutine을 통해 순차성을 확보해 정상 작동됨 - 완료
```

### 배운 점

- fragment의 필요성에 따라 Bottom Navigation의 가시성을 조정하는 방법
- adapter에 생성자에 콜백 메서드를 두어 viewholder 터치 이벤트를 adapter를 사용하는 fragment에서 처리하는 방법
- retrofit2에서 coroutine을 통해 결과 값을 반환받는 방법
- coroutine을 통해 io 스레드에서 네트워크 처리를 진행하여 결과를 main 스레드로 가져와 순차성을 확보하는 방법
