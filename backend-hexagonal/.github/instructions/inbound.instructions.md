---
applyTo: "**/in/**/*.java"
---

# Inbound Adapter Rules

`sample-in-http`, `sample-in-grpc` 모듈에 적용되는 규칙이다.  
Inbound Adapter는 외부 요청을 받아 Core의 UseCase를 호출하는 역할만 담당한다.

---

## 공통 규칙

- Core의 **UseCase Port(`XxxCommandUseCase`, `XxxQueryUseCase`)만** 의존
- **StorePort 직접 참조 금지** (`XxxCommandStorePort`, `XxxQueryStorePort`)
- **Outbound Adapter 직접 참조 금지** (`*.out.*` 패키지 import 금지)
- Command와 Query는 모든 레이어에서 완전 분리

```java
// ✅
private final EventCommandUseCase eventCommandUseCase;

// ❌
private final EventCommandStorePort storePort;       // StorePort 직접 참조 금지
private final OpenSearchEventCommandAdapter adapter; // Outbound Adapter 직접 참조 금지
```

---

## HTTP (`sample-in-http`)

### 패키지 구조

```
sample-in-http/
├── dto/
│   ├── command/   ← Request DTO (POST/PUT/DELETE 요청 바디)
│   └── query/     ← Response DTO (GET 응답 바디)
├── mapper/
│   ├── command/   ← Request DTO → VO 변환
│   └── query/     ← VO → Response DTO 변환
└── web/
    ├── command/   ← Command Controller (POST/PUT/DELETE)
    └── query/     ← Query Controller (GET)
```

### Command Controller 규칙

- `web/command/` 패키지에 위치
- `@PostMapping`, `@PutMapping`, `@DeleteMapping`만 사용
- `@GetMapping` 사용 금지
- 반환 타입: `ResponseEntity<Void>` (응답은 GlobalExceptionHandler가 ApiResponse로 변환)
- 예외 발생 시 GlobalExceptionHandler가 `ApiResponse<Void>`로 변환하여 클라이언트에 반환

```java
// ✅ GlobalExceptionHandler가 ApiResponse로 변환
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventCommandController {

    private final EventCommandUseCase eventCommandUseCase;
    private final EventCommandMapper eventCommandMapper;

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody @Valid EventRequest request) {
        eventCommandUseCase.saveEvent(eventCommandMapper.toVO(request));
        return ResponseEntity.ok().build();  // ✅ GlobalExceptionHandler가 ApiResponse 래핑
    }
}

// ❌ Command Controller에 GET 매핑 금지
@GetMapping("/{id}")
public ResponseEntity<EventResponse> get(...) { ... }
```

> **ApiResponse 변환 흐름:**
> - 성공: `ResponseEntity.ok().build()` → GlobalExceptionHandler 없음 → 그대로 200 OK 반환
> - 실패: Service에서 `EventException` → GlobalExceptionHandler → `ApiResponse.error()` + HTTP 상태코드

### Query Controller 규칙

- `web/query/` 패키지에 위치
- `@GetMapping`만 사용
- `@PostMapping`, `@PutMapping`, `@DeleteMapping` 사용 금지
- 반환 타입: `ResponseEntity<EventResponse>` (응답은 GlobalExceptionHandler가 ApiResponse로 변환)
- 404 Not Found는 EventException으로 던져 GlobalExceptionHandler가 처리

```java
// ✅ EventException 던지기 → GlobalExceptionHandler가 ApiResponse.error()로 변환
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventQueryController {

    private final EventQueryUseCase eventQueryUseCase;
    private final EventQueryMapper eventQueryMapper;

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> get(@PathVariable String id) {
        return eventQueryUseCase.getEvent(id)
                .map(eventQueryMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND, id));
                // ✅ EventException 발생 → GlobalExceptionHandler → ApiResponse.error() 자동 변환
    }
}
```

### DTO 규칙

- **Request DTO** → `dto/command/` 패키지, 클래스명: `{Domain}Request`
- **Response DTO** → `dto/query/` 패키지, 클래스명: `{Domain}Response`
- DTO는 `@Data` (Lombok) 또는 `record` 사용
- VO, Entity, StorePort 타입을 DTO 필드로 포함 금지

```java
// ✅ dto/command/EventRequest.java
@Data
public class EventRequest {
    private String id;
    private String message;
}

// ✅ dto/query/EventResponse.java
@Data
public class EventResponse {
    private String id;
    private String message;
}
```

### Mapper 규칙

- **Command Mapper** → `mapper/command/`, `Request → VO` 변환만 담당
- **Query Mapper** → `mapper/query/`, `VO → Response` 변환만 담당
- Command Mapper에서 VO → Response 변환 금지 (반대 방향도 마찬가지)

```java
// ✅ mapper/command/EventCommandMapper.java
@Component
public class EventCommandMapper {
    public EventVO toVO(EventRequest request) {
        return new EventVO(request.getId(), request.getMessage());
    }
}

// ✅ mapper/query/EventQueryMapper.java
@Component
public class EventQueryMapper {
    public EventResponse toResponse(EventVO vo) {
        EventResponse response = new EventResponse();
        response.setId(vo.id());
        response.setMessage(vo.message());
        return response;
    }
}
```

---

## gRPC (`sample-in-grpc`)

### 패키지 구조

```
sample-in-grpc/
├── dto/
│   ├── command/   ← Protobuf 생성 Command 메시지
│   └── query/     ← Protobuf 생성 Query 메시지
├── mapper/
│   ├── command/   ← gRPC Command 메시지 → VO 변환
│   └── query/     ← VO → gRPC Query 응답 변환
├── command/       ← Command Facade
└── query/         ← Query Facade
```

### Facade 규칙

- **Command Facade** → `command/` 패키지, `CommandUseCase`만 호출
- **Query Facade** → `query/` 패키지, `QueryUseCase`만 호출
- gRPC Service 클래스(Stub 구현체)는 Facade에 위임만 하고 비즈니스 로직 포함 금지

```java
// ✅ command/GrpcEventCommandFacade.java
@Component
@RequiredArgsConstructor
public class GrpcEventCommandFacade {

    private final EventCommandUseCase eventCommandUseCase;
    private final GrpcEventCommandMapper mapper;

    public void save(GrpcEventRequest request) {
        eventCommandUseCase.saveEvent(mapper.toVO(request));
    }
}

// ✅ gRPC Service — Facade에 위임
@GrpcService
@RequiredArgsConstructor
public class EventCommandGrpcService extends EventCommandServiceGrpc.EventCommandServiceImplBase {

    private final GrpcEventCommandFacade facade;

    @Override
    public void save(GrpcEventRequest request, StreamObserver<Empty> responseObserver) {
        try {
            facade.save(request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
```

---

## 예외 처리 흐름 (Bootstrap Common과 연계)

모든 예외는 **GlobalExceptionHandler**에서 중앙 처리되므로, Controller에서는 예외 처리 코드가 불필요하다.

### HTTP Adapter 예외 흐름

```
Controller
  ↓ UseCase 호출
Service (RuntimeException catch → EventException throw)
  ↓ 예외 전파
GlobalExceptionHandler @ExceptionHandler(ApplicationException.class)
  ↓
ApiResponse.error(code, message)
  ↓ HTTP 상태코드 자동 매핑
클라이언트 응답 (JSON)
```

### 사용 예시

```java
// ✅ Controller — 예외 처리 코드 불필요 (GlobalExceptionHandler가 처리)
@PostMapping
public ResponseEntity<ApiResponse<Void>> save(@RequestBody @Valid EventRequest request) {
    eventCommandUseCase.saveEvent(eventCommandMapper.toVO(request));
    return ResponseEntity.ok(ApiResponse.ok());
}
// EventCommandUseCase 내부에서 예외 발생 → GlobalExceptionHandler 자동 처리

// ✅ Service — 도메인 예외로 변환 (RuntimeException → EventException)
@Service
public class EventCommandService implements EventCommandUseCase {
    @Override
    public void saveEvent(EventVO eventVO) {
        try {
            storePort.save(eventVO);  // RuntimeException 가능
        } catch (RuntimeException e) {
            throw new EventException(EventErrorCode.EVENT_SAVE_FAILED, eventVO.id(), e);
        }
    }
}

// ❌ Controller에서 try-catch 추가 금지 (불필요함)
@PostMapping
public ResponseEntity<ApiResponse<Void>> save(@RequestBody @Valid EventRequest request) {
    try {  // ❌ 불필요한 예외 처리
        eventCommandUseCase.saveEvent(eventCommandMapper.toVO(request));
        return ResponseEntity.ok(ApiResponse.ok());
    } catch (EventException e) {
        return ResponseEntity.badRequest().body(ApiResponse.error(...));
    }
}
```

### API 에러 응답 형식

GlobalExceptionHandler가 자동으로 생성하는 응답:

```json
{
  "success": false,
  "errorCode": "EVT-101",
  "message": "Event with id 'evt-001' not found"
}
```

HTTP 상태 코드는 `ErrorCode.getHttpStatus()`에서 자동으로 매핑된다.
