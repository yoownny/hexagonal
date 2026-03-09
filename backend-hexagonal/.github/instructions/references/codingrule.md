# 🧾 Coding Rules

 
---

## 📌 네이밍 규칙

- 클래스: PascalCase (`ThreatAnalyzer`)
- 메서드: camelCase (`analyzeThreat`)
- 변수: camelCase (`threatLevel`)
- 상수: UPPER_SNAKE_CASE (`MAX_RETRY_COUNT`)

---

## 🧼 코드 스타일

- 들여쓰기: 2 spaces
- 줄 길이 제한: 150자
- 주석: JavaDoc

---

## JAVA 프로젝트 모듈 package 구조
- package 기본 구조는 Layered Architecture를 따릅니다.
- com.ahnlab.edr 패키지는 이전에 구현된 레거시 코드입니다.
- com.ahnlab.edr2 패키지는 바이브 코딩에 의해 신규 작성한 코드입니다.
- 각 모듈의 exception 패키지는 하나만 존재합니다.
- com.ahnlab.edr2.module이름 하위에는 도메인 기능 단위로 패키지를 생성합니다.

예시>
com.ahnlab.edr2
 └── module이름                         # 모듈 이름
       └── exception                    # 사용자 정의 예외 
       └── domain이름                   # 도메인
             └── dto                    # 요청/응답 DTO(Request/Response 전송 객체)
             └── ctl                    # REST API 엔드포인트
             └── vo                     # VO(비즈니스 로직에서 전달하는 파라메터 객체)
             └── svc                    # 비즈니스 로직
             └── entity                 # Entity(데이터베이스 맵핑 객체)
             └── dao                    # Reposigory



## 🛡️ 예외 처리
- 사용자 정의 예외는 `com.ahnlab.edr.core.exception.EdrCoreException` 을 상속하여 구현한 하위 클래스에 ErrorCode를 추가하여 정의합니다.


---

## 🧾 로깅

- SLF4J 사용
- 로그 레벨: `DEBUG`, `INFO`, `WARN`, `ERROR` 중심
- 민감 정보 로그 금지

---

## 🧪 테스트

- 단위 테스트 필수
- 서비스 레이어는 Mockito로 mocking
- 컨트롤러는 WebMvcTest 사용