---
name: project-scaffold
description: >
  헥사고날 아키텍처 프로젝트의 초기 구조를 잡는 가이드.
  "프로젝트 초기 설정", "도메인 추가", "새 도메인", "도메인 생성",
  "헥사고날 구조 생성" 요청 시 사용.
---

# SKILL: 헥사고날 아키텍처 프로젝트 초기 구조 설정

## 목적

새로운 도메인을 위한 헥사고날 아키텍처 멀티모듈 프로젝트의 초기 구조를 잡는다.  
base package: `com.ahnlab.edr`

---

## 준수 규칙 — Instructions 연동

이 스킬로 생성하는 **모든 코드**는 `.github/instructions/` 의 규칙을 준수한다.  
파일을 새로 만들 때 자동 활성화가 보장되지 않으므로, 각 섹션에서 해당 규칙을 명시한다.

| Instructions 파일 | applyTo | 적용 섹션 |
|-------------------|---------|-----------|
| `java.instructions.md` | `**/*.java` | 모든 Java 파일 — Lombok, Optional, Import, 테스트 규칙 |
| `core.instructions.md` | `**/core/**/*.java` | Section 3 — Core VO/Port/Service/Exception |
| `inbound.instructions.md` | `**/in/**/*.java` | Section 6 참고 — Controller/Mapper/DTO |
| `outbound.instructions.md` | `**/out/**/*.java` | Section 5 참고 — Adapter/Store/Mapper |

### 모든 Java 파일 공통 규칙 (`java.instructions.md`)

```java
// ✅ Lombok — @Slf4j + @RequiredArgsConstructor + private final 생성자 주입
@Service @Slf4j @RequiredArgsConstructor
public class XxxService { private final XxxPort port; }

// ❌ @Autowired 필드/수정자 주입 금지
@Autowired private XxxPort port;

// ✅ null 반환 금지 — Optional 또는 빈 컬렉션 반환
return store.findById(id).orElseThrow(() -> new XxxException(XxxErrorCode.NOT_FOUND, id));
// ❌ Optional.get() 직접 호출 금지, null 반환 금지

// ✅ 와일드카드 import 금지 — 개별 import 사용
import java.util.Optional;
// ❌ import java.util.*;

// ✅ Controller 파라미터에 @Valid 적용
public ResponseEntity<...> save(@RequestBody @Valid XxxRequest request) { ... }

// ✅ Entity/DTO → @Data 사용
@Data public class XxxEntity { ... }

// ✅ 상수: private static final + UPPER_SNAKE_CASE
private static final int MAX_RETRY = 3;
```

### 테스트 작성 규칙 (`java.instructions.md`)

```java
// 테스트 메서드명: 메서드명_상황_기대결과
@ExtendWith(MockitoExtension.class)
class EventCommandServiceTest {

    @Mock private EventCommandStorePort storePort;
    private EventCommandService service;

    @BeforeEach
    void setUp() { service = new EventCommandService(storePort); }  // 생성자 주입 선호

    @Test
    @DisplayName("이벤트 저장 성공")
    void saveEvent_whenValid_savesToStore() {
        EventVO vo = new EventVO("id-1", "message");
        service.saveEvent(vo);
        verify(storePort).save(vo);
    }

    @Test
    @DisplayName("저장 실패 시 EventException 발생")
    void saveEvent_whenStoreFails_throwsEventException() {
        doThrow(new RuntimeException("store error")).when(storePort).save(any());
        assertThatThrownBy(() -> service.saveEvent(new EventVO("id-1", "msg")))
            .isInstanceOf(EventException.class);
    }
}
```

---

## 1. 생성할 모듈 목록

| 모듈명 | 역할 |
|--------|------|
| `{domain}-core` | 비즈니스 로직 (VO, Port, Service, Exception) |
| `{domain}-in-http` | HTTP REST Inbound Adapter |
| `{domain}-in-grpc` | gRPC Inbound Adapter |
| `{domain}-out-{tech}` | Outbound Adapter (기술별) |
| `{domain}-config` | @Conditional 어노테이션 |
| `{domain}-bootstrap` | Spring Boot 실행 진입점 |

---

## 2. 루트 pom.xml 모듈 등록

```xml
<modules>
  <module>{domain}-core</module>
  <module>{domain}-in-http</module>
  <module>{domain}-in-grpc</module>
  <module>{domain}-out-{tech}</module>
  <module>{domain}-config</module>
  <module>{domain}-bootstrap</module>
</modules>
```

---

## 3. 공통 클래스 — `{domain}-core`

> 📋 **적용 규칙**: `core.instructions.md` + `java.instructions.md`  
> Core는 비즈니스 로직만 담당하며 **외부 기술(HTTP, DB, gRPC) import 절대 금지**.  
> Entity·VO→Entity Mapper·HTTP DTO는 Core가 관리하지 않는다.

### Core Import 금지 목록

```java
// ❌ 절대 금지 — HTTP / REST
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

// ❌ 절대 금지 — JPA / Data
import org.springframework.data.*;
import jakarta.persistence.*;

// ❌ 절대 금지 — gRPC
import io.grpc.*;

// ❌ 절대 금지 — 직렬화 라이브러리
import com.fasterxml.jackson.*;

// ❌ 절대 금지 — DB 드라이버
import java.sql.*;
import org.opensearch.*;
import ru.yandex.clickhouse.*;

// ❌ 절대 금지 — Adapter 패키지
import com.ahnlab.edr.*.in.*;
import com.ahnlab.edr.*.out.*;
```

### 3.1 패키지 구조

```
com.ahnlab.edr.{domain}.core/
├── application/
│   ├── command/
│   │   ├── port/
│   │   │   ├── in/         ← UseCase 인터페이스
│   │   │   └── out/        ← CommandStorePort 인터페이스
│   │   └── service/        ← CommandService 구현체
│   ├── query/
│   │   ├── port/
│   │   │   ├── in/         ← QueryUseCase 인터페이스
│   │   │   └── out/        ← QueryStorePort 인터페이스
│   │   └── service/        ← QueryService 구현체
│   └── exception/
│       ├── ErrorCode.java
│       ├── ApplicationException.java
│       └── {DomainEntity}/
│           ├── {Entity}ErrorCode.java
│           └── {Entity}Exception.java
└── domain/
    └── vo/                 ← Value Object (record)
```

### 3.2 `ErrorCode` 인터페이스

```java
package com.ahnlab.edr.{domain}.core.application.exception;

/**
 * 모든 도메인 에러 코드가 구현해야 하는 인터페이스.
 * 각 도메인별 enum으로 구현한다.
 */
public interface ErrorCode {

    /** 에러 식별 코드 (예: EVT-101) */
    String getCode();

    /** 에러 메시지 템플릿 */
    String getMessage();

    /** HTTP 상태 코드 */
    int getHttpStatus();
}
```

### 3.3 `ApplicationException` 기본 예외 클래스

```java
package com.ahnlab.edr.{domain}.core.application.exception;

import lombok.Getter;

/**
 * 모든 도메인 예외의 기반 클래스.
 * Outbound Adapter에서 throw 금지 — Service에서만 사용.
 */
@Getter
public class ApplicationException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String detail;

    public ApplicationException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = errorCode.getMessage();
    }

    public ApplicationException(ErrorCode errorCode, String detail) {
        super(String.format(errorCode.getMessage(), detail));
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public ApplicationException(ErrorCode errorCode, String detail, Throwable cause) {
        super(String.format(errorCode.getMessage(), detail), cause);
        this.errorCode = errorCode;
        this.detail = detail;
    }
}
```

### 3.4 도메인 예외 & ErrorCode 예시 (`Event` 도메인)

```java
// com.ahnlab.edr.{domain}.core.application.exception.event.EventErrorCode
package com.ahnlab.edr.{domain}.core.application.exception.event;

import com.ahnlab.edr.{domain}.core.application.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventErrorCode implements ErrorCode {

    // Query (EVT-1XX)
    EVENT_NOT_FOUND("EVT-101", "Event with id '%s' not found", 404),
    EVENT_QUERY_FAILED("EVT-102", "Failed to query event", 500),

    // Command (EVT-2XX)
    EVENT_SAVE_FAILED("EVT-201", "Failed to save event", 500),

    // Validation (EVT-3XX)
    EVENT_ID_REQUIRED("EVT-301", "Event ID is required", 400),
    EVENT_MESSAGE_REQUIRED("EVT-302", "Event message is required", 400);

    private final String code;
    private final String message;
    private final int httpStatus;
}
```

```java
// com.ahnlab.edr.{domain}.core.application.exception.event.EventException
package com.ahnlab.edr.{domain}.core.application.exception.event;

import com.ahnlab.edr.{domain}.core.application.exception.ApplicationException;

public class EventException extends ApplicationException {

    public EventException(EventErrorCode errorCode) {
        super(errorCode);
    }

    public EventException(EventErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public EventException(EventErrorCode errorCode, String detail, Throwable cause) {
        super(errorCode, detail, cause);
    }
}
```

---

## 4. `ApiResponse` 래퍼 — `{domain}-bootstrap` 또는 공통 모듈

> 📋 **적용 규칙**: `inbound.instructions.md` + `java.instructions.md`  
> Controller 생성 시 반드시 준수:
> - **Command Controller** (`web/command/`): `@PostMapping`, `@PutMapping`, `@DeleteMapping`만 사용 (`@GetMapping` 금지)
> - **Query Controller** (`web/query/`): `@GetMapping`만 사용 (변경 메서드 금지)
> - **UseCase Port만 의존** — `StorePort`, `Outbound Adapter` 직접 참조 금지
> - `@RequestBody` 파라미터에 반드시 `@Valid` 적용
> - Inbound Adapter 패키지 구조: `dto/command/`, `dto/query/`, `mapper/command/`, `mapper/query/`, `web/command/`, `web/query/`

### Command Controller 패턴 (`inbound.instructions.md`)

```java
// web/command/{Entity}CommandController.java — POST/PUT/DELETE만 허용
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventCommandController {

    private final EventCommandUseCase eventCommandUseCase;   // ✅ UseCase만 의존 (StorePort 금지)
    private final EventCommandMapper eventCommandMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> save(@RequestBody @Valid EventRequest request) {  // ✅ @Valid 필수
        eventCommandUseCase.saveEvent(eventCommandMapper.toVO(request));
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
```

### Query Controller 패턴 (`inbound.instructions.md`)

```java
// web/query/{Entity}QueryController.java — GET만 허용
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventQueryController {

    private final EventQueryUseCase eventQueryUseCase;   // ✅ UseCase만 의존 (StorePort 금지)
    private final EventQueryMapper eventQueryMapper;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> get(@PathVariable String id) {
        return eventQueryUseCase.getEvent(id)
                .map(eventQueryMapper::toResponse)
                .map(ApiResponse::ok)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());   // ✅ null 반환 금지, Optional 활용
    }
}
```

### `ApiResponse` 클래스

```java
package com.ahnlab.edr.{domain}.bootstrap.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * API 응답 표준 래퍼.
 * 성공: {@code ApiResponse.ok(data)}
 * 실패: {@code ApiResponse.error(code, message)}
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String errorCode;
    private final String message;

    /** 성공 응답 (데이터 있음) */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    /** 성공 응답 (데이터 없음) */
    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(true, null, null, null);
    }

    /** 실패 응답 */
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return new ApiResponse<>(false, null, errorCode, message);
    }
}
```

Controller 사용 예시:

```java
@PostMapping
public ResponseEntity<ApiResponse<Void>> save(@RequestBody @Valid EventRequest request) {
    eventCommandUseCase.saveEvent(eventCommandMapper.toVO(request));
    return ResponseEntity.ok(ApiResponse.ok());
}

@GetMapping("/{id}")
public ResponseEntity<ApiResponse<EventResponse>> get(@PathVariable String id) {
    return eventQueryUseCase.getEvent(id)
            .map(eventQueryMapper::toResponse)
            .map(ApiResponse::ok)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}
```

---

## 5. `@Conditional` 어노테이션 — `{domain}-config`

> 📋 **적용 규칙**: `outbound.instructions.md` + `java.instructions.md`  
> Outbound Adapter 생성 시 반드시 준수:
> - `@Component` 빈 **전체**에 `@Conditional` 어노테이션 필수 (Store 클래스 포함)
> - VO를 파라미터로 받아 내부에서 Entity로 변환 (Core에 변환 책임 없음)
> - Core의 도메인 예외 import 금지 — checked 예외는 `RuntimeException`으로 래핑
> - Adapter 패키지 구조: `store/`, `command/`, `query/`, `mapper/`

### 5.1 패키지 구조

```
com.ahnlab.edr.{domain}.config/
├── HttpInboundEnabled.java
├── GrpcInboundEnabled.java
├── {Tech1}OutboundEnabled.java
└── {Tech2}OutboundEnabled.java
```

### 5.2 어노테이션 패턴

```java
// com.ahnlab.edr.{domain}.config.HttpInboundEnabled
package com.ahnlab.edr.{domain}.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import java.lang.annotation.*;

/**
 * HTTP Inbound Adapter 활성화 조건.
 * application.yml: sample.inbound.http-enabled: true
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(
    name = "{domain}.inbound.http-enabled",
    havingValue = "true",
    matchIfMissing = false
)
public @interface HttpInboundEnabled {
}
```

```java
// com.ahnlab.edr.{domain}.config.GrpcInboundEnabled
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(
    name = "{domain}.inbound.grpc-enabled",
    havingValue = "true",
    matchIfMissing = false
)
public @interface GrpcInboundEnabled {
}
```

```java
// com.ahnlab.edr.{domain}.config.OpenSearchOutboundEnabled
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(
    name = "{domain}.outbound",
    havingValue = "opensearch"
)
public @interface OpenSearchOutboundEnabled {
}
```

```java
// com.ahnlab.edr.{domain}.config.ClickHouseOutboundEnabled
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnProperty(
    name = "{domain}.outbound",
    havingValue = "clickhouse"
)
public @interface ClickHouseOutboundEnabled {
}
```

### 5.3 application.yml 설정 예시

```yaml
{domain}:
  inbound:
    http-enabled: true     # HttpInboundEnabled 빈 활성화
    grpc-enabled: false    # GrpcInboundEnabled 빈 비활성화
  outbound: opensearch     # opensearch | clickhouse
```

---

## 6. Bootstrap 모듈 설정 — `{domain}-bootstrap`

### 6.1 `pom.xml` 의존성

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Core -->
    <dependency>
        <groupId>com.ahnlab.edr</groupId>
        <artifactId>{domain}-core</artifactId>
        <version>${project.version}</version>
    </dependency>

    <!-- Inbound Adapters -->
    <dependency>
        <groupId>com.ahnlab.edr</groupId>
        <artifactId>{domain}-in-http</artifactId>
        <version>${project.version}</version>
    </dependency>
    <dependency>
        <groupId>com.ahnlab.edr</groupId>
        <artifactId>{domain}-in-grpc</artifactId>
        <version>${project.version}</version>
    </dependency>

    <!-- Outbound Adapters -->
    <dependency>
        <groupId>com.ahnlab.edr</groupId>
        <artifactId>{domain}-out-opensearch</artifactId>
        <version>${project.version}</version>
    </dependency>
    <dependency>
        <groupId>com.ahnlab.edr</groupId>
        <artifactId>{domain}-out-clickhouse</artifactId>
        <version>${project.version}</version>
    </dependency>

    <!-- Config -->
    <dependency>
        <groupId>com.ahnlab.edr</groupId>
        <artifactId>{domain}-config</artifactId>
        <version>${project.version}</version>
    </dependency>
</dependencies>
```

### 6.2 Main Application Class

```java
package com.ahnlab.edr.{domain}.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.ahnlab.edr.{domain}")
public class {Domain}BootstrapApplication {

    public static void main(String[] args) {
        SpringApplication.run({Domain}BootstrapApplication.class, args);
    }
}
```

---

## 7. 글로벌 예외 핸들러 — `{domain}-bootstrap`

```java
package com.ahnlab.edr.{domain}.bootstrap.common;

import com.ahnlab.edr.{domain}.core.application.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<Void>> handleApplicationException(ApplicationException e) {
        log.warn("Application exception: code={}, detail={}", e.getErrorCode().getCode(), e.getDetail());
        return ResponseEntity
                .status(e.getErrorCode().getHttpStatus())
                .body(ApiResponse.error(e.getErrorCode().getCode(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected exception", e);
        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.error("INTERNAL_ERROR", "Internal server error"));
    }
}
```

---

## 8. 체크리스트

새 도메인 모듈 추가 후 확인 사항:

- [ ] 루트 `pom.xml`에 모듈 등록
- [ ] `{domain}-core`: `ErrorCode`, `ApplicationException`, 도메인 예외, VO, Port, Service 생성
- [ ] `{domain}-config`: 모든 `@Conditional` 어노테이션 생성
- [ ] `{domain}-in-http`: Controller, DTO, Mapper 패키지 구조 준수
- [ ] `{domain}-out-{tech}`: 모든 빈에 `@Conditional` 어노테이션 적용
- [ ] `{domain}-bootstrap`: `ApiResponse`, `GlobalExceptionHandler` 배치
- [ ] `application.yml`에 `{domain}.inbound.*`, `{domain}.outbound` 프로퍼티 추가

### Instructions 준수 확인

- [ ] **`java.instructions.md`**: `@Autowired` 없음, `@Slf4j`+`@RequiredArgsConstructor` 사용, `null` 반환 없음, 와일드카드 import 없음
- [ ] **`core.instructions.md`**: Core에 기술 스택 import(`javax.persistence`, `org.springframework.web` 등) 없음, Entity/Mapper 없음
- [ ] **`inbound.instructions.md`**: Command Controller에 `@GetMapping` 없음, Query Controller에 변경 메서드 없음, `@Valid` 적용, `StorePort` 직접 참조 없음
- [ ] **`outbound.instructions.md`**: 모든 `@Component`에 `@Conditional` 있음, Core 도메인 예외 import 없음, checked 예외 → `RuntimeException` 래핑
