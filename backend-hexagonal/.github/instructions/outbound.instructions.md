---
applyTo: "**/out/**/*.java"
---

# Outbound Adapter Rules

`sample-out-opensearch`, `sample-out-clickhouse` 모듈에 적용되는 규칙이다.  
Outbound Adapter는 Core의 StorePort를 구현하며, VO를 받아 기술 저장소에 저장/조회하는 역할을 담당한다.

---

## 패키지 구조

```
sample-out-{tech}/
├── store/     ← 저장소 클라이언트 래퍼 (공유 상태 또는 실제 클라이언트)
├── command/   ← XxxCommandAdapter implements XxxCommandStorePort
├── query/     ← XxxQueryAdapter implements XxxQueryStorePort
└── mapper/    ← VO → Entity 변환 (Core에 두지 않음)
```

---

## @Conditional 어노테이션 (필수)

모든 `@Component` 빈에 반드시 `sample-config`의 `@Conditional` 어노테이션을 붙여야 한다.  
어노테이션 없이 등록된 빈은 항상 활성화되어 프로파일 전환이 불가능하다.

```java
// ✅ OpenSearch 모듈
@Component
@OpenSearchOutboundEnabled  // sample-config 어노테이션
public class OpenSearchEventCommandAdapter implements EventCommandStorePort { ... }

@Component
@OpenSearchOutboundEnabled
public class OpenSearchEventStore { ... }

// ✅ ClickHouse 모듈
@Component
@ClickHouseOutboundEnabled  // sample-config 어노테이션
public class ClickHouseEventCommandAdapter implements EventCommandStorePort { ... }

// ❌ @Conditional 어노테이션 없이 등록 금지
@Component
public class OpenSearchEventCommandAdapter implements EventCommandStorePort { ... }
```

---

## 기술 예외 처리 (필수)

모든 기술 예외(DB 연결 오류, I/O 예외 등)는 **`RuntimeException`으로 래핑**하여 던진다.  
Core의 Service가 `RuntimeException`을 catch하여 도메인 예외로 변환하는 구조이므로, Adapter는 도메인 예외를 직접 throw하지 않는다.

```java
// ✅ checked 예외 → RuntimeException 래핑
@Override
public void save(EventVO eventVO) {
    try {
        EventEntity entity = mapper.toEntity(eventVO);
        store.put(entity.getId(), entity);
    } catch (IOException e) {
        log.error("Failed to save event to store: {}", eventVO.id(), e);
        throw new RuntimeException("Failed to save event: " + eventVO.id(), e);
    } catch (RuntimeException e) {
        log.error("Unexpected error saving event: {}", eventVO.id(), e);
        throw e;
    }
}

// ❌ 도메인 예외를 Adapter에서 직접 throw 금지
throw new EventException(EventErrorCode.EVENT_SAVE_FAILED, eventVO.id(), e);

// ❌ checked 예외를 그대로 전파 금지 (Port 계약 위반)
public void save(EventVO eventVO) throws IOException { ... }
```

---

## Core Exception Import 금지

Adapter는 Core의 도메인 예외 클래스를 **절대 import하지 않는다.**  
도메인 예외 변환은 Core의 Service 책임이다.

```java
// ❌ 절대 금지
import com.ahnlab.edr.*.core.application.exception.*;
import com.ahnlab.edr.*.core.application.exception.event.EventException;
import com.ahnlab.edr.*.core.application.exception.user.UserException;
```

---

## StorePort 구현 규칙

- Port 메서드에 `throws` 절 추가 금지 (Core의 Port 계약 준수)
- 파라미터는 **VO**를 받고, 내부에서 Entity로 변환 후 저장
- 반환 타입이 `Optional<EventVO>`인 경우 Entity → VO 역변환 후 반환

```java
// ✅ Command Adapter
@Component
@OpenSearchOutboundEnabled
@Slf4j
@RequiredArgsConstructor
public class OpenSearchEventCommandAdapter implements EventCommandStorePort {

    private final OpenSearchEventStore store;
    private final EventEntityMapper mapper;  // VO → Entity 변환 (Adapter 내부)

    @Override
    public void save(EventVO eventVO) {  // throws 절 없음
        try {
            EventEntity entity = mapper.toEntity(eventVO);
            store.put(entity.getId(), entity);
        } catch (RuntimeException e) {
            log.error("Failed to save event: {}", eventVO.id(), e);
            throw new RuntimeException("Failed to save event: " + eventVO.id(), e);
        }
    }
}

// ✅ Query Adapter
@Component
@OpenSearchOutboundEnabled
@Slf4j
@RequiredArgsConstructor
public class OpenSearchEventQueryAdapter implements EventQueryStorePort {

    private final OpenSearchEventStore store;
    private final EventEntityMapper mapper;

    @Override
    public Optional<EventVO> findById(String id) {  // throws 절 없음
        try {
            return store.get(id).map(mapper::toVO);  // Entity → VO 역변환
        } catch (RuntimeException e) {
            log.error("Failed to query event: {}", id, e);
            throw new RuntimeException("Failed to query event: " + id, e);
        }
    }
}
```

---

## Mapper 규칙 (VO ↔ Entity)

- VO → Entity, Entity → VO 변환은 `mapper/` 패키지에 위치
- Core의 VO와 자체 Entity만 참조 (Core의 Mapper, Exception 참조 금지)
- 각 기술 모듈이 독립적인 Mapper를 보유 (모듈 간 Mapper 공유 금지)

```java
// ✅ mapper/EventEntityMapper.java (Adapter 내부)
@Component
public class EventEntityMapper {

    public EventEntity toEntity(EventVO vo) {
        EventEntity entity = new EventEntity();
        entity.setId(vo.id());
        entity.setMessage(vo.message());
        return entity;
    }

    public EventVO toVO(EventEntity entity) {
        return new EventVO(entity.getId(), entity.getMessage());
    }
}
```

---

## Store 규칙

- `store/` 패키지에 위치, 같은 모듈의 Command/Query Adapter가 공유
- Command와 Query가 동일한 Store 인스턴스를 바라보도록 `@Component` 싱글톤으로 관리
- Store 클래스에도 반드시 `@Conditional` 어노테이션 적용

```java
// ✅
@Component
@OpenSearchOutboundEnabled
public class OpenSearchEventStore {
    private final Map<String, EventEntity> store = new ConcurrentHashMap<>();

    public void put(String id, EventEntity entity) {
        store.put(id, entity);
    }

    public Optional<EventEntity> get(String id) {
        return Optional.ofNullable(store.get(id));
    }
}
```
