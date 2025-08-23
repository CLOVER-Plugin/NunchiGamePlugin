# NunchiGamePlugin (눈치게임 플러그인)

마인크래프트 서버에서 사용할 수 있는 4라운드 구조의 미니게임 플러그인입니다. 각 라운드마다 다른 게임 메커니즘을 제공하여 플레이어들이 다양한 방식으로 경쟁할 수 있습니다.

## 📋 요구사항

### 서버 환경
- **Paper API**: 1.21.4-R0.1-SNAPSHOT 이상
- **Java 버전**: 21 이상
- **마인크래프트 버전**: 1.21.x

### Dependencies
- **Maven**: 3.6.0 이상
- **Paper API**: `io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT`
- **Build Tools**: 
  - `maven-compiler-plugin:3.13.0`
  - `maven-shade-plugin:3.5.3`

### 권한
- `nunchigame.areasetup`: 영역 설정 명령어 사용 권한
- 기본 명령어들은 OP 또는 권한이 있는 플레이어만 사용 가능

## 🎮 게임 구조

### 1라운드: 카운트다운 아이템
- **목적**: 스톱워치 아이템을 사용하여 정확한 시간 측정
- **아이템**: 스톱워치 (산호팬 블록)
- **명령어**: `/아이템 카운트`
- **규칙**: 플레이어가 원하는 시간을 정하고, 정확한 타이밍에 아이템을 사용해야 함

### 2라운드: 양궁 게임
- **목적**: 과녁을 맞춰서 높은 점수 획득
- **참여자**: 최대 4명
- **과녁**: 3개 (각각 3x1x3 크기)
- **방해 요소**: 각 과녁마다 방해 블럭이 랜덤하게 열림
- **게임 시간**: 3분 (180초)
- **점수 시스템**: 과녁 명중 시 점수 획득

**주요 명령어:**
- `/2라운드` - 게임 시작/중단
- `/영역설정` - 과녁과 방해 블럭 영역 설정
- `/참여` - 2라운드 참여자 등록

**영역 설정 방법:**
```
/영역설정 과녁1    # 과녁1 영역의 첫 번째 지점 선택
/영역설정 과녁1    # 과녁1 영역의 두 번째 지점 선택 (자동으로 영역 생성)
/영역설정 방해1    # 방해1 영역의 첫 번째 지점 선택
/영역설정 방해1    # 방해1 영역의 두 번째 지점 선택
/영역설정 저장 설정명    # 현재 설정을 저장
/영역설정 불러오기 설정명    # 저장된 설정 불러오기
```

### 3라운드: 투표 시스템
- **목적**: 30초 동안 다른 플레이어에게 투표
- **명령어**: `/투표시작`
- **아이템**: 투표용지
- **규칙**: 
  - 투표 시간 동안만 투표 가능
  - 한 번만 투표 가능
  - 투표 결과는 모든 플레이어에게 공개

### 4라운드: 테마 타이머
- **목적**: 주어진 주제에 대해 정확한 시간 측정
- **명령어**: `/테마시작` (플레이어 이름으로 시작)
- **주제 목록**: 애니, 마인크래프트, 동물, 인물, 음료, 아이돌 그룹, TV 프로그램, 브랜드, 직업, 노래, 과자, 라면, PC 게임, 사자성어
- **게임 흐름**:
  1. 3초 카운트다운
  2. 주제 공개
  3. 5초 타이머 시작
  4. 플레이어들이 정확한 타이밍에 테마샤드 아이템 사용

## ⚙️ 설정 파일

### config.yml
```yaml
round2:
  targets:
    target1:
      center: {world: "world", x: 100, y: 64, z: 100}
      blocks: [...]  # 3x1x3 영역의 모든 블럭 위치
  barriers:
    barrier1:
      pos1: {world: "world", x: 99, y: 65, z: 99}
      pos2: {world: "world", x: 101, y: 67, z: 101}
  settings:
    round_duration: 180        # 게임 시간 (초)
    barrier_open_duration: 2   # 방해 블럭 열림 시간 (초)
    min_open_targets: 1        # 최소 열리는 과녁 수
    max_open_targets: 3        # 최대 열리는 과녁 수

round4:
  topics: [애니, 마인크래프트, 동물, ...]  # 테마 주제 목록
```

### teleports.yml
```yaml
spawn: {world: "world", x: 0.5, y: 64.0, z: 0.5, yaw: 0.0, pitch: 0.0}
r1: {world: "world", x: 10.0, y: 64.0, z: 10.0, yaw: 90.0, pitch: 0.0}
r2: {world: "world", x: 20.0, y: 64.0, z: 10.0, yaw: 90.0, pitch: 0.0}
# ... 추가 라운드별 텔레포트 지점
```

## 🏗️ 코드 구조

```
src/main/java/yd/kingdom/nunchiGamePlugin/
├── NunchiGamePlugin.java          # 메인 플러그인 클래스
├── command/                        # 명령어 처리
│   ├── AreaSetupCommand.java      # 영역 설정 명령어
│   ├── ItemCommand.java           # 아이템 지급 명령어
│   ├── ParticipateCommand.java    # 참여 명령어
│   ├── Round2Command.java         # 2라운드 게임 명령어
│   └── VoteStartCommand.java      # 투표 시작 명령어
├── item/                          # 게임 아이템
│   ├── CountdownItem.java         # 스톱워치 아이템
│   ├── CountdownUseListener.java  # 스톱워치 사용 리스너
│   ├── ThemeShardItem.java        # 테마샤드 아이템
│   ├── VotePaperItem.java         # 투표용지 아이템
│   └── ClockOpenListener.java     # 시계 열기 리스너
├── round1/                        # 1라운드: 카운트다운
│   └── CountdownService.java      # 카운트다운 서비스
├── round2/                        # 2라운드: 양궁 게임
│   ├── ArcherRoundManager.java    # 양궁 게임 메인 매니저
│   ├── AreaSelectionManager.java  # 영역 선택 관리
│   ├── PlayerJoinListener.java    # 플레이어 참여 리스너
│   └── TargetListener.java        # 과녁 명중 리스너
├── round3/                        # 3라운드: 투표 시스템
│   ├── VoteManager.java           # 투표 관리
│   └── VoteGuiListener.java       # 투표 GUI 리스너
├── round4/                        # 4라운드: 테마 타이머
│   ├── ThemeTimerManager.java     # 테마 타이머 관리
│   └── ThemeTimerListener.java    # 테마 타이머 리스너
├── gui/                          # GUI 시스템
│   ├── TeleportGUI.java           # 텔레포트 GUI
│   └── TeleportGUIListener.java   # 텔레포트 GUI 리스너
├── config/                        # 설정 관리
│   └── TeleportConfig.java        # 텔레포트 설정
└── util/                          # 유틸리티
    └── ItemBuilders.java          # 아이템 빌더
```

## 🚀 설치 및 사용법

### 1. 플러그인 설치
1. `NunchiGamePlugin.jar` 파일을 서버의 `plugins` 폴더에 복사
2. 서버 재시작
3. 플러그인이 정상적으로 로드되었는지 확인

### 2. 개발 환경 설정 (개발자용)
1. **Java 21 설치**: JDK 21 이상 버전 설치
2. **Maven 설치**: Maven 3.6.0 이상 버전 설치
3. **프로젝트 빌드**: `mvn clean package` 명령어로 JAR 파일 생성
4. **의존성 확인**: `pom.xml`의 Paper API 버전이 서버와 호환되는지 확인

### 3. 초기 설정
1. **영역 설정**: `/영역설정` 명령어로 2라운드 과녁과 방해 블럭 영역 설정
2. **설정 저장**: `/영역설정 저장 기본설정`으로 설정 저장
3. **권한 설정**: 필요한 플레이어에게 `nunchigame.areasetup` 권한 부여

### 4. 게임 진행
1. **1라운드**: `/아이템 카운트`로 스톱워치 지급
2. **2라운드**: `/참여`로 참여자 등록 후 `/2라운드`로 게임 시작
3. **3라운드**: `/투표시작`으로 투표 시작
4. **4라운드**: 플레이어 이름으로 테마 타이머 시작

## 🔧 주요 기능

### 게임 관리
- **자동 점수 계산**: 과녁 명중 시 자동으로 점수 계산 및 스코어보드 업데이트
- **방해 블럭 시스템**: 랜덤하게 열리는 방해 블럭으로 게임 난이도 조절
- **참여자 관리**: 최대 4명까지 참여 가능, 게임 중 참여/퇴장 관리

### 설정 관리
- **영역 설정**: 월드에딧과 유사한 방식으로 과녁과 방해 블럭 영역 설정
- **설정 저장/불러오기**: 여러 게임 설정을 저장하고 재사용 가능
- **동적 설정**: 게임 중에도 설정 변경 가능

### 사용자 인터페이스
- **한국어 지원**: 모든 명령어와 메시지가 한국어로 제공
- **GUI 시스템**: 직관적인 텔레포트 GUI 제공
- **스코어보드**: 실시간 점수 표시 및 순위 확인

## 🐛 문제 해결

### 일반적인 문제
1. **플러그인이 로드되지 않음**: Java 21 이상 버전 확인
2. **영역 설정이 안됨**: 권한 확인 및 명령어 사용법 재확인
3. **게임이 시작되지 않음**: 영역 설정이 완료되었는지 확인

### Dependency 관련 문제
1. **Paper API 호환성 오류**: 서버의 Paper 버전과 `pom.xml`의 버전이 일치하는지 확인
2. **Maven 빌드 실패**: Java 21과 Maven 3.6.0 이상 버전 설치 확인
3. **의존성 다운로드 실패**: 인터넷 연결 및 Maven repository 접근 확인

### 로그 확인
- 서버 콘솔에서 플러그인 로그 확인
- `[NunchiGame]` 태그로 시작하는 로그 메시지 확인

## 📝 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다.

```
MIT License

Copyright (c) 2024 yd.kingdom

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 🤝 기여하기

1. 이 저장소를 포크
2. 새로운 기능 브랜치 생성 (`git checkout -b feature/AmazingFeature`)
3. 변경사항 커밋 (`git commit -m 'Add some AmazingFeature'`)
4. 브랜치에 푸시 (`git push origin feature/AmazingFeature`)
5. Pull Request 생성

## 📞 문의

프로젝트에 대한 질문이나 제안사항이 있으시면 이슈를 생성해 주세요.

---

**개발자**: ydking0911

**버전**: 1.0-SNAPSHOT  
**최종 업데이트**: 2025.08.23
