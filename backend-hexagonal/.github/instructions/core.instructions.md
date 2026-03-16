---
applyTo: "**/core/**/*.java"
---

# Core Module Rules

`sample-core` 모듈에만 적용되는 규칙이다.  
Core는 비즈니스 로직의 핵심이며, **외부 기술에 대한 어떤 의존도 가져서는 안 된다.**

---

## Core가 관리하는 것

| 종류 | 위치 | 설명 |
|------|------|------|
| **VO** | `domain/vo/` | 도메인 불변 객체 (`record`) |
| **Inbound Port** | `application/command(query)/port/in/` | UseCase 인터페이스 |
| **Outbound Port** | `application/command(query)/port/out/` | StorePort 인터페이스 |
| **Service** | `application/command(query)/service/` | UseCase 구현체 |
| **Exception** | `application/exception/` | ApplicationException, ErrorCode, 도메인 예외 |

## Core가 관리하지 않는 것

- ❌ **Entity** — Outbound Adapter 모듈이 관리
- ❌ **VO→Entity 변환 Mapper** — Adapter 내부 책임
- ❌ **HTTP/gRPC DTO** — Inbound Adapter 모듈이 관리

---

## 외부 기술 Import 절대 금지

아래 패키지는 Core 파일에서 **절대로 import 할 수 없다.**

```java
// ❌ 절대 금지 — HTTP / REST
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.*;
import org.springframework.http.*;

// ❌ 절대 금지 — JPA / Data
import org.springframework.data.*;
import jakarta.persistence.*;
import javax.persistence.*;

// ❌ 절대 금지 — gRPC
import io.grpc.*;

// ❌ 절대 금지 — 직렬화 라이브러리
import com.fasterxml.jackson.*;
import com.google.gson.*;

// ❌ 절대 금지 — DB 드라이버 / SQL
import java.sql.*;
import org.postgresql.*;
import ru.yandex.clickhouse.*;
import org.opensearch.*;
import org.elasticsearch.*;

// ❌ 절대 금지 — Adapter 패키지 직접 참조
import com.ahnlab.edr.*.in.*;
import com.ahnlab.edr.*.out.*;
```

---

## 허용 Import

```java
// ✅ Core 내부
import com.ahnlab.edr.*.core.*;

// ✅ Java 표준 라이브러리
import java.util.*;
import java.util.Optional;
import java.util.List;

// ✅ Spring 스테레오타입 / DI
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Component;

// ✅ Lombok
import lombok.*;

// ✅ SLF4J (로깅 인터페이스)
import org.slf4j.Logger;
// 또는 @Slf4j
```

---

## VO 작성 규칙

- `record`로 선언하여 불변성 보장
- compact constructor 안에서 비즈니스 검증 수행
- 외부 기술 타입(예: `HttpStatus`, `JsonNode`) 절대 포함 금지

```java
// ✅
public record EventVO(String id, String message) {
    public EventVO {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Event ID cannot be null or blank");
        }
    }
}
```

---

## Port 인터페이스 작성 규칙

- 메서드 시그니처에 `throws` 절 선언 금지
- 파라미터와 반환 타입은 **VO 또는 Java 표준 타입** 사용 (Entity, HTTP DTO 금지)
- Command StorePort는 VO를 받고, Query StorePort는 VO를 반환

```java
// ✅ Inbound Port (UseCase)
public interface EventCommandUseCase {
    void saveEvent(EventVO eventVO);  // throws 절 없음
}

// ✅ Outbound Port (StorePort)
public interface EventCommandStorePort {
    void save(EventVO eventVO);  // VO 파라미터, throws 절 없음
}

public interface EventQueryStorePort {
    Optional<EventVO> findById(String id);  // VO 반환
}
```

---

## Service 작성 규칙

- UseCase 인터페이스만 구현 (`implements XxxUseCase`)
- StorePort는 반드시 인터페이스로 주입 (구현체 직접 참조 금지)
- `RuntimeException` catch → 도메인 예외(`XxxException`)로 변환

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class EventCommandService implements EventCommandUseCase {

    private final EventCommandStorePort storePort;  // ✅ 인터페이스

    @Override
    public void saveEvent(EventVO eventVO) {
        try {
            storePort.save(eventVO);
        } catch (RuntimeException e) {
            log.error("Failed to save event: {}", eventVO.id(), e);
            throw new EventException(EventErrorCode.EVENT_SAVE_FAILED, eventVO.id(), e);
        }
    }
}
```

---

## Exception 작성 규칙

- 모든 도메인 예외는 `ApplicationException`을 상속
- `ErrorCode` 인터페이스를 구현한 enum으로 에러 코드 체계화
- 에러 코드 번호 체계: `{도메인약어}-{분류XXX}` (예: `EVT-101`, `USR-201`)

```java
// ErrorCode 분류 체계
// 1XX: Query 에러, 2XX: Command 에러, 3XX: Validation 에러

public enum EventErrorCode implements ErrorCode {
    EVENT_NOT_FOUND("EVT-101", "Event with id '%s' not found", 404),
    EVENT_SAVE_FAILED("EVT-201", "Failed to save event", 500),
    EVENT_ID_REQUIRED("EVT-301", "Event ID is required", 400);
}
```
