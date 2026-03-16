# HTTP → OpenSearch 단계별 흐름 가이드

이 문서는 HTTP 요청이 어떻게 시스템 전체를 통과하여 OpenSearch에 저장/조회되는지 **단계별로** 설명합니다.

헥사고날 아키텍처의 핵심: **각 계층은 인터페이스만 알고, 구체적인 구현은 모른다.**

---

## 📝 COMMAND 경로: 이벤트 저장 (HTTP POST)

클라이언트가 `POST /api/events` 요청을 보낼 때의 흐름입니다.

### 1단계: HTTP 요청 도착 (Inbound Adapter)

```
클라이언트 (Swagger/Postman)
    │
    ├─ POST /api/events
    └─ {"id": "EVT-001", "message": "Attack detected"}
           │
           ▼
   Controller 진입
```

**코드:**
```java
// sample-in-http/web/command/EventCommandController.java
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventCommandController {

    private final EventCommandUseCase eventCommandUseCase;  // ← UseCase 인터페이스 주입
    private final EventCommandMapper mapper;

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody @Valid EventRequest request) {
        // Step 1: DTO → VO 변환 (Mapper 책임)
        EventVO vo = mapper.toVO(request);
        // DTO: {"id": "EVT-001", "message": "Attack detected"}
        // VO:  EventVO(id="EVT-001", message="Attack detected")
        
        // Step 2: UseCase 호출 (Controller는 UseCase만 알고 있음)
        eventCommandUseCase.saveEvent(vo);  // ← 실제가 뭔지 모름
        // Service가 구현했는지, 다른 게 구현했는지 Controller는 모름
        
        // Step 3: 응답 반환
        return ResponseEntity.status(201).build();
        // 성공하면 201 Created 반환
    }
}
```

**이 시점에:**
- ✅ HTTP DTO → VO 변환 완료
- ✅ UseCase 인터페이스 호출 (Service가 뭔지 모름)

---

### 2단계: UseCase 인터페이스 (Core)

```
Controller가 호출
    │
    └─ eventCommandUseCase.saveEvent(vo)
           │
           ▼
   UseCase 인터페이스 (Core)
```

**코드:**
```java
// sample-core/application/command/port/in/EventCommandUseCase.java
public interface EventCommandUseCase {
    /**
     * 이벤트를 저장한다.
     * 
     * @param eventVO 저장할 이벤트 (VO)
     */
    void saveEvent(EventVO eventVO);
    // throws 절 없음 (Port의 원칙)
}
```

**이 시점에:**
- 📍 Core의 경계
- 📍 "saveEvent를 구현해라" 라고만 정의
- 📍 실제 구현체가 뭔지는 Core 입장에서는 모름

---

### 3단계: Service가 UseCase를 구현 (Core)

```
UseCase 인터페이스 호출
    │
    └─ 누가 구현했는가?
           │
           ▼
   Service (Core 내부)
```

**코드:**
```java
// sample-core/application/command/service/EventCommandService.java
@Service
@Slf4j
@RequiredArgsConstructor
public class EventCommandService implements EventCommandUseCase {
    // ↑ SaveUserUseCase 인터페이스를 구현

    private final EventCommandStorePort storePort;  // ← StorePort 인터페이스 주입
    // OpenSearch인지 ClickHouse인지 Service는 모름

    @Override
    public void saveEvent(EventVO eventVO) {
        log.info("Saving event: {}", eventVO.id());
        
        // 비즈니스 검증
        if (eventVO.id() == null || eventVO.id().isBlank()) {
            throw new EventException(EventErrorCode.EVENT_ID_REQUIRED);
        }
        
        // 비즈니스 로직 수행
        try {
            storePort.save(eventVO);  // ← StorePort 인터페이스 호출
            // 실제 DB가 뭔지 Service는 모름
            log.info("Event saved successfully: {}", eventVO.id());
        } catch (RuntimeException e) {
            log.error("Failed to save event: {}", eventVO.id(), e);
            throw new EventException(EventErrorCode.EVENT_SAVE_FAILED, eventVO.id(), e);
        }
    }
}
```

**이 시점에:**
- ✅ 비즈니스 검증 수행
- ✅ StorePort 인터페이스 호출 (실제가 뭔지 모름)
- ✅ 예외 발생 시 도메인 예외로 변환

---

### 4단계: StorePort 인터페이스 (Core)

```
Service가 호출
    │
    └─ storePort.save(vo)
           │
           ▼
   StorePort 인터페이스 (Core)
```

**코드:**
```java
// sample-core/application/command/port/out/EventCommandStorePort.java
public interface EventCommandStorePort {
    /**
     * 이벤트를 저장한다.
     * 
     * @param eventVO 저장할 이벤트 (VO)
     */
    void save(EventVO eventVO);
    // throws 절 없음 (Port의 원칙)
    // 어느 DB에 저장할지는 정의하지 않음
}
```

**이 시점에:**
- 📍 Core의 경계
- 📍 "이벤트를 저장해라" 라고만 정의
- 📍 OpenSearch인지, ClickHouse인지, 파일인지 Core는 모름

---

### 5단계: Adapter가 StorePort를 구현 (Outbound)

```
StorePort 호출
    │
    └─ 누가 구현했는가?
           │
           ▼
   OpenSearch Adapter (Outbound)
```

**코드:**
```java
// sample-out-opensearch/src/main/java/.../command/OpenSearchEventCommandAdapter.java
@Component
@OpenSearchOutboundEnabled  // 이 Adapter가 실제로 사용될지 결정
@Slf4j
@RequiredArgsConstructor
public class OpenSearchEventCommandAdapter implements EventCommandStorePort {
    // ↑ StorePort 인터페이스를 구현

    private final OpenSearchEventStore store;
    private final EventEntityMapper mapper;  // VO → Entity 변환

    @Override
    public void save(EventVO eventVO) {
        log.info("OpenSearch: Saving event VO: {}", eventVO.id());
        
        try {
            // Step 1: VO → Entity 변환 (Adapter 책임)
            EventEntity entity = mapper.toEntity(eventVO);
            // VO:     EventVO(id="EVT-001", message="Attack detected")
            // Entity: EventEntity { id="EVT-001", message="Attack detected" }
            log.debug("Converted VO to Entity: {}", entity.getId());
            
            // Step 2: 실제 OpenSearch에 저장 (인메모리 저장소)
            store.save(entity);
            log.info("OpenSearch: Event saved: id={}, store_size={}", 
                eventVO.id(), store.size());
        } catch (RuntimeException e) {
            log.error("OpenSearch: Failed to save event: {}", eventVO.id(), e);
            throw new RuntimeException("Failed to save event: " + eventVO.id(), e);
            // ↑ checked 예외 없음, RuntimeException만 던짐
            // Service가 이 RuntimeException을 catch해서 EventException으로 변환
        }
    }
}
```

**EventEntity로의 변환:**
```java
// sample-out-opensearch/mapper/EventEntityMapper.java
@Component
public class EventEntityMapper {
    public EventEntity toEntity(EventVO vo) {
        EventEntity entity = new EventEntity();
        entity.setId(vo.id());           // ← VO의 id
        entity.setMessage(vo.message()); // ← VO의 message
        return entity;
        // OpenSearch 전용 필드 추가 가능
    }
}
```

**저장소 호출:**
```java
// sample-out-opensearch/store/OpenSearchEventStore.java
@Component
@OpenSearchOutboundEnabled
@Slf4j
public class OpenSearchEventStore {
    
    private final Map<String, EventEntity> store = new ConcurrentHashMap<>();
    
    public void save(EventEntity entity) {
        store.put(entity.getId(), entity);
        log.info("Event stored in OpenSearch: id={}, current_size={}", 
            entity.getId(), store.size());
    }
    
    public Optional<EventEntity> get(String id) {
        log.info("Querying OpenSearch: id={}", id);
        return Optional.ofNullable(store.get(id));
    }
}
```

**이 시점에:**
- ✅ VO → Entity 변환 완료
- ✅ 실제 OpenSearch(인메모리) 저장 완료
- ✅ RuntimeException으로 래핑

---

### 📊 COMMAND 전체 흐름도

```
┌─────────────────────────────────────────────────────────────────────┐
│ 1️⃣ HTTP 요청 (Inbound Adapter)                                      │
│   POST /api/events                                                   │
│   DTO: {"id": "EVT-001", "message": "Attack detected"}              │
└──────────────────┬──────────────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 2️⃣ DTO → VO 변환 (EventCommandMapper)                               │
│   EventRequest → EventVO(id="EVT-001", message="Attack detected")   │
└──────────────────┬──────────────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 3️⃣ UseCase 호출 (eventCommandUseCase.saveEvent(vo))                 │
│   Controller는 ServiceImpl을 모름, Port만 알고 있음                   │
└──────────────────┬──────────────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 4️⃣ Service 비즈니스 로직 (EventCommandService)                      │
│   - 검증 (id != null)                                               │
│   - StorePort 호출 (storePort.save(vo))                             │
│   Service는 OpenSearch인지 ClickHouse인지 모름                       │
└──────────────────┬──────────────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 5️⃣ Adapter 저장 (OpenSearchEventCommandAdapter)                     │
│   - VO → Entity 변환 (mapper.toEntity(vo))                          │
│   - 실제 저장 (store.save(entity))                                  │
│   Store에 저장됨: {EVT-001: EventEntity(...)}                       │
└──────────────────┬──────────────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 6️⃣ 응답 반환                                                        │
│   201 Created                                                       │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🔍 QUERY 경로: 이벤트 조회 (HTTP GET)

클라이언트가 `GET /api/events/{id}` 요청을 보낼 때의 흐름입니다.

### 1단계: HTTP 요청 도착 (Inbound Adapter)

```
클라이언트 (Swagger/Postman)
    │
    ├─ GET /api/events/EVT-001
    └─ (요청 바디 없음)
           │
           ▼
   Query Controller 진입
```

**코드:**
```java
// sample-in-http/web/query/EventQueryController.java
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventQueryController {

    private final EventQueryUseCase eventQueryUseCase;  // ← Query UseCase 인터페이스 주입
    private final EventQueryMapper mapper;

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> get(@PathVariable("id") String id) {
        // Step 1: Query UseCase 호출 (조회만 함)
        Optional<EventVO> vo = eventQueryUseCase.getEvent(id);
        // 실제가 뭔지 Controller는 모름
        
        // Step 2: VO → Response DTO 변환 (Mapper 책임)
        return vo.map(mapper::toResponse)  // EventVO → EventResponse
                .map(ResponseEntity::ok)   // 200 OK
                .orElse(ResponseEntity.notFound().build());  // 404 Not Found
        
        // 예외 발생 시 GlobalExceptionHandler가 처리
    }
}
```

**이 시점에:**
- ✅ HTTP 파라미터 추출: id="EVT-001"
- ✅ Query UseCase 호출
- ✅ VO → Response DTO 변환

---

### 2단계: UseCase 인터페이스 (Core)

```
Controller가 호출
    │
    └─ eventQueryUseCase.getEvent(id)
           │
           ▼
   Query UseCase 인터페이스 (Core)
```

**코드:**
```java
// sample-core/application/query/port/in/EventQueryUseCase.java
public interface EventQueryUseCase {
    /**
     * ID로 이벤트를 조회한다.
     * 
     * @param id 이벤트 ID
     * @return 이벤트 VO (없으면 empty)
     */
    Optional<EventVO> getEvent(String id);
    // throws 절 없음 (Port의 원칙)
}
```

**이 시점에:**
- 📍 Core의 경계
- 📍 "이벤트를 조회해라" 라고만 정의
- 📍 실제 구현체가 뭔지는 Core 입장에서는 모름

---

### 3단계: Service가 UseCase를 구현 (Core)

```
Query UseCase 호출
    │
    └─ 누가 구현했는가?
           │
           ▼
   Query Service (Core 내부)
```

**코드:**
```java
// sample-core/application/query/service/EventQueryService.java
@Service
@Slf4j
@RequiredArgsConstructor
public class EventQueryService implements EventQueryUseCase {
    // ↑ EventQueryUseCase 인터페이스를 구현

    private final EventQueryStorePort storePort;  // ← Query StorePort 인터페이스 주입
    // OpenSearch인지 ClickHouse인지 Service는 모름

    @Override
    public Optional<EventVO> getEvent(String id) {
        log.info("Querying event: {}", id);
        
        // 비즈니스 검증
        if (id == null || id.isBlank()) {
            throw new EventException(EventErrorCode.EVENT_ID_REQUIRED);
        }
        
        // 저장소에서 조회
        try {
            Optional<EventVO> result = storePort.findById(id);  // ← StorePort 호출
            // 실제 DB가 뭔지 Service는 모름
            
            if (result.isPresent()) {
                log.info("Event found: {}", id);
            } else {
                log.info("Event not found: {}", id);
            }
            return result;
        } catch (RuntimeException e) {
            log.error("Failed to query event: {}", id, e);
            throw new EventException(EventErrorCode.EVENT_QUERY_FAILED, id, e);
        }
    }
}
```

**이 시점에:**
- ✅ 비즈니스 검증 수행
- ✅ StorePort 인터페이스 호출 (조회만 함)
- ✅ 찾으면 VO 반환, 없으면 empty 반환

---

### 4단계: StorePort 인터페이스 (Core)

```
Service가 호출
    │
    └─ storePort.findById(id)
           │
           ▼
   Query StorePort 인터페이스 (Core)
```

**코드:**
```java
// sample-core/application/query/port/out/EventQueryStorePort.java
public interface EventQueryStorePort {
    /**
     * ID로 이벤트를 조회한다.
     * 
     * @param id 이벤트 ID
     * @return 이벤트 VO (없으면 empty)
     */
    Optional<EventVO> findById(String id);
    // throws 절 없음 (Port의 원칙)
    // 어느 DB에서 조회할지는 정의하지 않음
}
```

**이 시점에:**
- 📍 Core의 경계
- 📍 "이벤트를 조회해라" 라고만 정의
- 📍 OpenSearch인지, ClickHouse인지, 캐시인지 Core는 모름

---

### 5단계: Adapter가 StorePort를 구현 (Outbound)

```
Query StorePort 호출
    │
    └─ 누가 구현했는가?
           │
           ▼
   OpenSearch Query Adapter (Outbound)
```

**코드:**
```java
// sample-out-opensearch/src/main/java/.../query/OpenSearchEventQueryAdapter.java
@Component
@OpenSearchOutboundEnabled  // 이 Adapter가 실제로 사용될지 결정
@Slf4j
@RequiredArgsConstructor
public class OpenSearchEventQueryAdapter implements EventQueryStorePort {
    // ↑ Query StorePort 인터페이스를 구현

    private final OpenSearchEventStore store;
    private final EventEntityMapper mapper;  // Entity → VO 변환

    @Override
    public Optional<EventVO> findById(String id) {
        log.info("OpenSearch: Querying event: {}", id);
        
        try {
            // Step 1: 저장소에서 조회 (인메모리)
            Optional<EventEntity> entity = store.get(id);
            log.debug("OpenSearch: Query result - found: {}", entity.isPresent());
            
            // Step 2: Entity → VO 역변환 (Adapter 책임)
            Optional<EventVO> result = entity.map(mapper::toVO);
            // Entity: EventEntity { id="EVT-001", message="Attack detected" }
            // VO:     EventVO(id="EVT-001", message="Attack detected")
            
            if (result.isPresent()) {
                log.info("OpenSearch: Event found: {}", id);
            } else {
                log.info("OpenSearch: Event not found: {}", id);
            }
            return result;
        } catch (RuntimeException e) {
            log.error("OpenSearch: Failed to query event: {}", id, e);
            throw new RuntimeException("Failed to query event: " + id, e);
            // ↑ RuntimeException으로 래핑
            // Service가 이 RuntimeException을 catch해서 EventException으로 변환
        }
    }
}
```

**Entity → VO 역변환:**
```java
// sample-out-opensearch/mapper/EventEntityMapper.java
@Component
public class EventEntityMapper {
    
    public EventVO toVO(EventEntity entity) {
        // Entity의 필드를 VO로 변환
        return new EventVO(
            entity.getId(),           // ← Entity의 id
            entity.getMessage()       // ← Entity의 message
        );
        // OpenSearch 전용 필드는 버려짐
    }
}
```

**저장소 조회:**
```java
// sample-out-opensearch/store/OpenSearchEventStore.java
@Component
@OpenSearchOutboundEnabled
@Slf4j
public class OpenSearchEventStore {
    
    private final Map<String, EventEntity> store = new ConcurrentHashMap<>();
    
    public Optional<EventEntity> get(String id) {
        log.info("OpenSearch Store: Getting event: {}", id);
        Optional<EventEntity> result = Optional.ofNullable(store.get(id));
        log.info("OpenSearch Store: Found={}", result.isPresent());
        return result;
    }
}
```

**이 시점에:**
- ✅ 저장소에서 조회 완료
- ✅ Entity → VO 변환 완료
- ✅ 없으면 empty Optional 반환

---

### 📊 QUERY 전체 흐름도

```
┌─────────────────────────────────────────────────────────────────────┐
│ 1️⃣ HTTP 요청 (Inbound Adapter)                                      │
│   GET /api/events/EVT-001                                           │
│   PathVariable: id="EVT-001"                                        │
└──────────────────┬──────────────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 2️⃣ Query UseCase 호출 (eventQueryUseCase.getEvent(id))              │
│   Controller는 ServiceImpl을 모름, Port만 알고 있음                   │
└──────────────────┬──────────────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 3️⃣ Service 비즈니스 로직 (EventQueryService)                        │
│   - 검증 (id != null)                                               │
│   - StorePort 호출 (storePort.findById(id))                         │
│   Service는 OpenSearch인지 ClickHouse인지 모름                       │
└──────────────────┬──────────────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 4️⃣ Adapter 조회 (OpenSearchEventQueryAdapter)                       │
│   - 저장소에서 조회 (store.get(id))                                 │
│   - Entity → VO 변환 (mapper.toVO(entity))                          │
│   Store에서 조회: EVT-001 → EventEntity(...)                        │
└──────────────────┬──────────────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 5️⃣ VO → Response DTO 변환 (EventQueryMapper)                        │
│   EventVO → EventResponse(id="EVT-001", message="...")              │
└──────────────────┬──────────────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────────────┐
│ 6️⃣ 응답 반환                                                        │
│   200 OK: {"id": "EVT-001", "message": "Attack detected"}           │
│   또는 404 Not Found (해당 ID 없으면)                               │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🎯 핵심 개념

### 1. **의존성 방향: 안쪽을 향함**

```
Controller
    ↓ (의존)
UseCase Port (Core)
    ↓ (의존)
Service (Core 구현체)
    ↓ (의존)
StorePort (Core)
    ↓ (의존)
Adapter (구현체)
    ↓ (의존)
Store (기술 저장소)
```

**핵심:** Controller와 Service는 **Port(인터페이스)만** 의존한다. 구현체가 뭔지 모른다!

### 2. **계층별 책임**

| 계층 | 책임 |
|------|------|
| **Controller** | HTTP 요청 수신, DTO ↔ VO 변환, UseCase 호출 |
| **Service** | 비즈니스 검증, StorePort 호출, 예외 변환 |
| **Adapter** | VO ↔ Entity 변환, 실제 저장/조회 |
| **Store** | 기술 저장소 (인메모리, DB 등) |

### 3. **변환 지점**

```
HTTP DTO
    ↓ (EventCommandMapper.toVO)
    EventVO
    ↓ (비즈니스 로직)
    StorePort 호출
    ↓ (EventEntityMapper.toEntity)
    EventEntity
    ↓ (저장소)
    저장됨

조회 시 역순:
저장소에서 조회
    ↓ (EventEntityMapper.toVO)
    EventVO
    ↓ (EventQueryMapper.toResponse)
    HTTP Response DTO
```

### 4. **각 계층이 알고 있는 것**

| 계층 | 알고 있는 것 | 모르는 것 |
|------|-----------|---------|
| **Controller** | HTTP, DTO, UseCase Port | Service 구현체, DB 기술 |
| **Service** | 비즈니스 로직, StorePort Port | Adapter 구현체, DB 기술 |
| **Adapter** | DB 기술, Entity | Controller, Service |

---

## 🔌 다른 조합 예시

### OpenSearch → ClickHouse로 전환하려면?

**변경할 코드:**
```
변경 필요:  sample-out-clickhouse 모듈 선택
변경 불필요: sample-in-http, sample-core 전부
```

**방법:**
```yaml
# application.yml
sample:
  outbound: clickhouse  # opensearch → clickhouse로 변경
```

- `@OpenSearchOutboundEnabled` 빈은 로드되지 않음
- `@ClickHouseOutboundEnabled` 빈이 로드됨
- Controller, Service 코드는 **단 한 줄도 변경 안 됨**
- Store, Mapper만 ClickHouse 버전으로 사용됨
