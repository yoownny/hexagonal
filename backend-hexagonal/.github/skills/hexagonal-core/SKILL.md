---
name: hexagonal-core
description: >
  Core 계층을 새로 만들 때 참고하는 가이드.
  패키지 구조, VO/Port/Service 생성 규칙, 예외 처리 체계(ErrorCode enum, Exception 클래스) 포함.
  "core 만들어줘", "도메인 core 추가", "UseCase 만들어줘", "StorePort 만들어줘",
  "Service 만들어줘", "ErrorCode 추가", "Exception 추가" 요청 시 사용.
applyTo: "**/core/**/*.java"
---

# SKILL: 헥사고날 아키텍처 Core 계층 생성

## 목적

`{domain}-core` 모듈 내부를 올바르게 구성한다.  
Core는 비즈니스 로직의 핵심이며, **외부 기술(HTTP, DB, gRPC)에 대한 어떠한 의존도 가져서는 안 된다.**  
base package: `com.ahnlab.edr`

---

## 1. 패키지 구조

```
{domain}-core/
└── src/main/java/com/ahnlab/edr/{domain}/core/
    ├── application/
    │   ├── command/
    │   │   ├── port/
    │   │   │   ├── in/          ← {Entity}CommandUseCase (Inbound Port)
    │   │   │   └── out/         ← {Entity}CommandStorePort (Outbound Port)
    │   │   └── service/         ← {Entity}CommandService (UseCase 구현체)
    │   ├── query/
    │   │   ├── port/
    │   │   │   ├── in/          ← {Entity}QueryUseCase (Inbound Port)
    │   │   │   └── out/         ← {Entity}QueryStorePort (Outbound Port)
    │   │   └── service/         ← {Entity}QueryService (UseCase 구현체)
    │   └── exception/
    │       ├── ErrorCode.java           ← 공통 인터페이스
    │       ├── ApplicationException.java ← 공통 기반 예외
    │       └── {entity}/               ← 도메인별 예외 (예: event/)
    │           ├── {Entity}ErrorCode.java
    │           └── {Entity}Exception.java
    └── domain/
        └── vo/                  ← Value Object (record)
```

> **Core가 관리하지 않는 것**
> - Entity 클래스 → Outbound Adapter 모듈 관리
> - VO→Entity 변환 Mapper → Adapter 내부 책임
> - HTTP/gRPC DTO → Inbound Adapter 모듈 관리

---

## 2. pom.xml — Core 모듈 최소 의존성

```xml
<project>
  <parent>
    <groupId>com.ahnlab.edr</groupId>
    <artifactId>edr-base</artifactId>
    <version>${revision}</version>
  </parent>

  <artifactId>{domain}-core</artifactId>
  <name>{domain}-core</name>

  <dependencies>
    <!-- Spring Stereotype (Service, Component 어노테이션) -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter</artifactId>
    </dependency>

    <!-- Test -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>
</project>
```

> **절대 추가 금지**: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`,
> gRPC, OpenSearch, ClickHouse 라이브러리 등 기술 스택 의존성

---

## 3. Import 허용/금지 목록

### ✅ 허용

```java
// Core 내부 참조
import com.ahnlab.edr.{domain}.core.*;

// Java 표준
import java.util.Optional;
import java.util.List;

// Spring 스테레오타입 / DI
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Component;

// Lombok
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// SLF4J 로깅 인터페이스
import org.slf4j.Logger;
```

### ❌ 절대 금지

```java
// HTTP / REST
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

// JPA / Data
import org.springframework.data.*;
import jakarta.persistence.*;

// gRPC
import io.grpc.*;

// 직렬화 라이브러리
import com.fasterxml.jackson.*;

// DB 드라이버
import java.sql.*;
import org.opensearch.*;
import ru.yandex.clickhouse.*;

// Adapter 패키지 직접 참조
import com.ahnlab.edr.*.in.*;
import com.ahnlab.edr.*.out.*;
```

---

## 4. VO 생성 규칙

- `record`로 선언하여 불변성 보장
- compact constructor 안에서 비즈니스 검증 수행
- 외부 기술 타입(`HttpStatus`, `JsonNode` 등) 포함 금지

```java
// domain/vo/EventVO.java
package com.ahnlab.edr.{domain}.core.domain.vo;

/**
 * 이벤트 도메인 Value Object.
 * 불변 객체이며, 생성 시점에 비즈니스 검증을 수행한다.
 *
 * @param id      이벤트 식별자
 * @param message 이벤트 메시지
 */
public record EventVO(String id, String message) {

    public EventVO {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Event ID cannot be null or blank");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Event message cannot be null or blank");
        }
    }
}
```

---

## 5. Port 생성 규칙

### 5.1 공통 규칙

- 메서드 시그니처에 `throws` 절 선언 **금지**
- 파라미터와 반환 타입은 **VO 또는 Java 표준 타입** 사용 (Entity, HTTP DTO 금지)
- Command Port: VO를 파라미터로 받음
- Query Port: VO 또는 `Optional<VO>`를 반환

### 5.2 Inbound Port (UseCase)

```java
// application/command/port/in/EventCommandUseCase.java
package com.ahnlab.edr.{domain}.core.application.command.port.in;

import com.ahnlab.edr.{domain}.core.domain.vo.EventVO;

/**
 * 이벤트 Command UseCase (Inbound Port).
 * Inbound Adapter(HTTP Controller, gRPC Facade)가 호출하는 인터페이스.
 */
public interface EventCommandUseCase {

    /**
     * 이벤트를 저장한다.
     *
     * @param eventVO 저장할 이벤트 VO
     */
    void saveEvent(EventVO eventVO);  // throws 절 없음
}
```

```java
// application/query/port/in/EventQueryUseCase.java
package com.ahnlab.edr.{domain}.core.application.query.port.in;

import com.ahnlab.edr.{domain}.core.domain.vo.EventVO;
import java.util.Optional;

/**
 * 이벤트 Query UseCase (Inbound Port).
 */
public interface EventQueryUseCase {

    /**
     * ID로 이벤트를 조회한다.
     *
     * @param id 이벤트 식별자
     * @return 이벤트 VO (없으면 empty)
     */
    Optional<EventVO> getEvent(String id);  // throws 절 없음
}
```

### 5.3 Outbound Port (StorePort)

```java
// application/command/port/out/EventCommandStorePort.java
package com.ahnlab.edr.{domain}.core.application.command.port.out;

import com.ahnlab.edr.{domain}.core.domain.vo.EventVO;

/**
 * 이벤트 Command StorePort (Outbound Port).
 * Outbound Adapter(OpenSearch, ClickHouse 등)가 구현하는 인터페이스.
 * VO를 받으며, Entity 변환은 Adapter 책임이다.
 */
public interface EventCommandStorePort {

    /**
     * 이벤트를 저장한다.
     *
     * @param eventVO 저장할 이벤트 VO
     */
    void save(EventVO eventVO);  // throws 절 없음
}
```

```java
// application/query/port/out/EventQueryStorePort.java
package com.ahnlab.edr.{domain}.core.application.query.port.out;

import com.ahnlab.edr.{domain}.core.domain.vo.EventVO;
import java.util.Optional;

/**
 * 이벤트 Query StorePort (Outbound Port).
 */
public interface EventQueryStorePort {

    /**
     * ID로 이벤트를 조회한다.
     *
     * @param id 이벤트 식별자
     * @return 이벤트 VO (없으면 empty)
     */
    Optional<EventVO> findById(String id);  // throws 절 없음
}
```

---

## 6. Service 생성 규칙

- UseCase 인터페이스만 구현 (`implements XxxUseCase`)
- StorePort는 반드시 인터페이스로 주입 (구현체 직접 참조 금지)
- `@Service`, `@Slf4j`, `@RequiredArgsConstructor` 조합 사용
- `RuntimeException` catch → 도메인 예외(`{Entity}Exception`)로 변환

```java
// application/command/service/EventCommandService.java
package com.ahnlab.edr.{domain}.core.application.command.service;

import com.ahnlab.edr.{domain}.core.application.command.port.in.EventCommandUseCase;
import com.ahnlab.edr.{domain}.core.application.command.port.out.EventCommandStorePort;
import com.ahnlab.edr.{domain}.core.application.exception.event.EventErrorCode;
import com.ahnlab.edr.{domain}.core.application.exception.event.EventException;
import com.ahnlab.edr.{domain}.core.domain.vo.EventVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventCommandService implements EventCommandUseCase {

    private final EventCommandStorePort storePort;  // ✅ 인터페이스 주입

    @Override
    public void saveEvent(EventVO eventVO) {
        try {
            storePort.save(eventVO);  // VO 전달, Entity 변환 코드 없음
        } catch (RuntimeException e) {
            log.error("Failed to save event: {}", eventVO.id(), e);
            throw new EventException(EventErrorCode.EVENT_SAVE_FAILED, eventVO.id(), e);
        }
    }
}
```

```java
// application/query/service/EventQueryService.java
package com.ahnlab.edr.{domain}.core.application.query.service;

import com.ahnlab.edr.{domain}.core.application.exception.event.EventErrorCode;
import com.ahnlab.edr.{domain}.core.application.exception.event.EventException;
import com.ahnlab.edr.{domain}.core.application.query.port.in.EventQueryUseCase;
import com.ahnlab.edr.{domain}.core.application.query.port.out.EventQueryStorePort;
import com.ahnlab.edr.{domain}.core.domain.vo.EventVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventQueryService implements EventQueryUseCase {

    private final EventQueryStorePort storePort;  // ✅ 인터페이스 주입

    @Override
    public Optional<EventVO> getEvent(String id) {
        try {
            return storePort.findById(id);
        } catch (RuntimeException e) {
            log.error("Failed to query event: {}", id, e);
            throw new EventException(EventErrorCode.EVENT_QUERY_FAILED, id, e);
        }
    }
}
```

---

## 7. 예외 처리 체계

### 7.1 `ErrorCode` 인터페이스

```java
// application/exception/ErrorCode.java
package com.ahnlab.edr.{domain}.core.application.exception;

/**
 * 모든 도메인 에러 코드가 구현해야 하는 인터페이스.
 * 각 도메인별 enum으로 구현한다.
 */
public interface ErrorCode {

    /** 에러 식별 코드 (예: EVT-101) */
    String getCode();

    /** 에러 메시지 템플릿 (%s 포맷 지원) */
    String getMessage();

    /** HTTP 상태 코드 */
    int getHttpStatus();
}
```

### 7.2 `ApplicationException` 기반 클래스

```java
// application/exception/ApplicationException.java
package com.ahnlab.edr.{domain}.core.application.exception;

import lombok.Getter;

/**
 * 모든 도메인 예외의 기반 클래스.
 * Outbound Adapter에서 직접 throw 금지 — Service에서만 사용.
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

### 7.3 도메인별 `ErrorCode` enum

에러 코드 번호 체계: `{도메인약어}-{분류XXX}`

| 범위 | 분류 | 예시 |
|------|------|------|
| 1XX | Query 에러 | `EVT-101` |
| 2XX | Command 에러 | `EVT-201` |
| 3XX | Validation 에러 | `EVT-301` |

```java
// application/exception/event/EventErrorCode.java
package com.ahnlab.edr.{domain}.core.application.exception.event;

import com.ahnlab.edr.{domain}.core.application.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventErrorCode implements ErrorCode {

    // Query (EVT-1XX)
    EVENT_NOT_FOUND("EVT-101", "Event with id '%s' not found", 404),
    EVENT_QUERY_FAILED("EVT-102", "Failed to query event with id '%s'", 500),

    // Command (EVT-2XX)
    EVENT_SAVE_FAILED("EVT-201", "Failed to save event with id '%s'", 500),

    // Validation (EVT-3XX)
    EVENT_ID_REQUIRED("EVT-301", "Event ID is required", 400),
    EVENT_MESSAGE_REQUIRED("EVT-302", "Event message is required", 400);

    private final String code;
    private final String message;
    private final int httpStatus;
}
```

### 7.4 도메인별 `Exception` 클래스

```java
// application/exception/event/EventException.java
package com.ahnlab.edr.{domain}.core.application.exception.event;

import com.ahnlab.edr.{domain}.core.application.exception.ApplicationException;

/**
 * 이벤트 도메인 예외.
 * Service에서만 throw하며, ErrorCode와 1:1로 매핑된다.
 */
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

### 7.5 예외 흐름 요약

```
Outbound Adapter
  └── IOException, RuntimeException 발생
        ↓ catch → new RuntimeException("...", cause) 래핑
Service
  └── RuntimeException catch
        ↓ throw new {Entity}Exception({Entity}ErrorCode.XXX, detail, cause)
GlobalExceptionHandler (bootstrap)
  └── ApplicationException catch
        ↓ ApiResponse.error(code, message) 반환
```

---

## 8. 의존성 흐름 규칙 요약

```
Inbound Adapter  →  EventCommandUseCase (in-port)
                         ↓ implements
                    EventCommandService
                         ↓ uses
                    EventCommandStorePort (out-port)
                         ↓ implements
                    Outbound Adapter
```

- **Core → Adapter 의존 금지**: Core는 Port 인터페이스만 선언, 구현체는 모름
- **Adapter → Core 의존**: Adapter는 Core의 Port·VO·Exception을 참조
- **Service는 StorePort 구현체를 직접 참조하지 않음**: Spring DI가 런타임에 주입

---

## 9. 체크리스트

새로운 Core 도메인/엔티티 추가 시 확인 사항:

- [ ] `domain/vo/{Entity}VO.java` — `record`, compact constructor 검증
- [ ] `application/command/port/in/{Entity}CommandUseCase.java` — `throws` 절 없음, VO 파라미터
- [ ] `application/command/port/out/{Entity}CommandStorePort.java` — `throws` 절 없음, VO 파라미터
- [ ] `application/query/port/in/{Entity}QueryUseCase.java` — `throws` 절 없음, VO 반환
- [ ] `application/query/port/out/{Entity}QueryStorePort.java` — `throws` 절 없음, VO 반환
- [ ] `application/command/service/{Entity}CommandService.java` — Port 인터페이스 주입, 도메인 예외 변환
- [ ] `application/query/service/{Entity}QueryService.java` — Port 인터페이스 주입, 도메인 예외 변환
- [ ] `application/exception/{entity}/{Entity}ErrorCode.java` — enum, 번호 체계 1XX/2XX/3XX
- [ ] `application/exception/{entity}/{Entity}Exception.java` — ApplicationException 상속
- [ ] Core 내부에 기술 스택 import 없음 (`javax.persistence`, `org.springframework.web` 등 금지)
