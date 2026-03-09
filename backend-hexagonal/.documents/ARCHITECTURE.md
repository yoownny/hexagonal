# EDR Backend - 헥사고날 아키텍처 가이드

> **작성일**: 2026-02-27  
> **대상**: EDR Backend 개발팀  
> **목적**: 프로젝트의 헥사고날 아키텍처와 CQRS 패턴 이해 및 준수 규칙 공유

---

## 📋 목차

1. [아키텍처 개요](#1-아키텍처-개요)
2. [핵심 원칙](#2-핵심-원칙)
3. [모듈 구조](#3-모듈-구조)
4. [패키지 상세 설명](#4-패키지-상세-설명)
5. [CQRS 패턴 적용](#5-cqrs-패턴-적용)
6. [의존성 규칙 (필수)](#6-의존성-규칙-필수)
7. [명명 규칙](#7-명명-규칙)
8. [예외 처리 체계](#8-예외-처리-체계)
9. [주의사항 및 제약사항](#9-주의사항-및-제약사항)
10. [개발 가이드](#10-개발-가이드)
11. [FAQ](#11-faq)

---

## 1. 아키텍처 개요

### 1.1 헥사고날 아키텍처 (Ports and Adapters)

```
┌─────────────────────────────────────────────────────────────┐
│                      Inbound Adapters                        │
│    (HTTP REST, gRPC - 외부에서 들어오는 요청 처리)          │
│         sample-in-http, sample-in-grpc                       │
└──────────────────────┬──────────────────────────────────────┘
                       │ Port (Interface)
┌──────────────────────▼──────────────────────────────────────┐
│                   Application Core                           │
│              (Business Logic - 도메인)                       │
│                  sample-core                                 │
│  ┌────────────────────────────────────────────────────┐     │
│  │  Command Service  ◄───► Query Service              │     │
│  │       (CQRS Pattern)                               │     │
│  └────────────────────────────────────────────────────┘     │
└──────────────────────┬──────────────────────────────────────┘
                       │ Port (Interface)
┌──────────────────────▼──────────────────────────────────────┐
│                     Outbound Adapters                        │
│  (OpenSearch, ClickHouse - 데이터 저장소 연결)              │
│    sample-out-opensearch, sample-out-clickhouse              │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 핵심 개념

| 구성요소 | 설명 | 예시 |
|---------|------|------|
| **Domain (Core)** | 비즈니스 로직이 담긴 핵심 영역, 외부 의존성 없음 | EventVO, UserVO, Service |
| **Port** | 코어와 외부를 연결하는 인터페이스 | EventCommandUseCase, EventQueryStorePort |
| **Inbound Adapter** | 외부 → 코어 (요청 수신) | HTTP Controller, gRPC Service |
| **Outbound Adapter** | 코어 → 외부 (데이터 저장/조회) | OpenSearch Adapter, ClickHouse Adapter |

---

## 2. 핵심 원칙

### ✅ 반드시 지켜야 할 3대 원칙

1. **도메인(Core)은 외부를 모른다**
   - `sample-core`는 HTTP, gRPC, OpenSearch 등을 직접 의존하지 않음
   - 모든 외부 연동은 Port(인터페이스)를 통해서만 가능

2. **의존성 방향은 항상 안쪽으로**
   ```
   Adapter (외부) ──의존──> Port (인터페이스) <──구현── Core (도메인)
   ```
   - ❌ Core가 Adapter를 직접 import
   - ✅ Adapter가 Core의 Port를 구현

3. **Command와 Query는 완전 분리 (CQRS)**
   - 쓰기(Command)와 읽기(Query)는 별도 Port/Service/Adapter로 완전 분리
   - DTO, Mapper, Controller도 command/query 패키지로 분류

---

## 3. 모듈 구조

```
Src/web/
├── sample-core/              ⭐ 도메인 (비즈니스 로직)
│   ├── domain/              # VO, Entity
│   └── application/          # Service, Port, Exception
│       ├── command/         # 쓰기 담당
│       ├── query/           # 읽기 담당
│       ├── mapper/          # VO ↔ Entity 변환
│       └── exception/       # 도메인 예외
│
├── sample-in-http/          📥 HTTP Inbound Adapter
│   ├── dto/
│   │   ├── command/        # Request DTO (POST/PUT/DELETE)
│   │   └── query/          # Response DTO (GET)
│   ├── mapper/
│   │   ├── command/        # Request → VO 변환
│   │   └── query/          # VO → Response 변환
│   └── web/
│       ├── command/        # Command Controller
│       └── query/          # Query Controller
│
├── sample-in-grpc/          📥 gRPC Inbound Adapter
│   ├── dto/
│   │   ├── command/        # Command Request DTO (.proto)
│   │   └── query/          # Query Response DTO (.proto)
│   ├── mapper/
│   │   ├── command/        # Command Mapper (gRPC → Core)
│   │   └── query/          # Query Mapper (Core → gRPC)
│   ├── command/            # Command Facade
│   └── query/              # Query Facade
│
├── sample-out-opensearch/  📤 OpenSearch Outbound Adapter
│   ├── store/              # 공유 저장소 (In-Memory)
│   ├── command/            # Command Adapter
│   └── query/              # Query Adapter
│
├── sample-out-clickhouse/  📤 ClickHouse Outbound Adapter
│   ├── store/              # 공유 저장소 (In-Memory)
│   ├── command/            # Command Adapter
│   └── query/              # Query Adapter
│
├── sample-config/          ⚙️ 설정 모듈
│   └── @Conditional 어노테이션 정의
│
└── sample-bootstrap/       🚀 실행 진입점
    └── Main Application Class
```

---

## 4. 패키지 상세 설명

### 4.1 sample-core (도메인 핵심)

#### 📁 `domain/`
```java
// VO (Value Object) - 불변 객체, 비즈니스 검증 포함
public record EventVO(String id, String message) {
    public EventVO {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }
    }
}

// Entity - 영속성 계층 객체
@Data
public class EventEntity {
    private String id;
    private String message;
}
```

#### 📁 `application/command/`
```
command/
├── port/
│   ├── in/                      # Inbound Port (UseCase 인터페이스)
│   │   ├── EventCommandUseCase
│   │   └── UserCommandUseCase
│   └── out/                     # Outbound Port (저장소 인터페이스)
│       ├── EventCommandStorePort
│       └── UserCommandStorePort
└── service/                     # 비즈니스 로직 구현
    ├── EventCommandService
    └── UserCommandService
```

**역할**:
- **Inbound Port (UseCase)**: 외부에서 호출할 수 있는 쓰기 작업 정의
- **Outbound Port (StorePort)**: 데이터 저장을 위한 인터페이스
- **Service**: UseCase를 구현하고 StorePort를 호출

#### 📁 `application/query/`
```
query/
├── port/
│   ├── in/                      # Inbound Port (Query 인터페이스)
│   │   ├── EventQueryUseCase
│   │   └── UserQueryUseCase
│   └── out/                     # Outbound Port (조회 인터페이스)
│       ├── EventQueryStorePort
│       └── UserQueryStorePort
└── service/                     # 조회 로직 구현
    ├── EventQueryService
    └── UserQueryService
```

#### 📁 `application/exception/`
```
exception/
├── ErrorCode.java                        # 인터페이스
├── ApplicationException.java             # Base 예외 클래스
└── [domain]/                             # 도메인별 예외
    ├── event/
    │   ├── EventException.java
    │   └── EventErrorCode.java (enum)    # EVT-001, EVT-002 ...
    └── user/
        ├── UserException.java
        └── UserErrorCode.java (enum)     # USR-001, USR-002 ...
```

---

### 4.2 sample-in-http (HTTP Adapter)

#### 📁 `dto/command/` vs `dto/query/`
```java
// dto/command/EventRequest.java (쓰기 요청)
@Data
public class EventRequest {
    private String id;
    private String message;
}

// dto/query/EventResponse.java (읽기 응답)
@Data
public class EventResponse {
    private String id;
    private String message;
}
```

#### 📁 `mapper/command/` vs `mapper/query/`
```java
// mapper/command/EventCommandMapper.java
@Mapper(componentModel = "spring")
public interface EventCommandMapper {
    EventVO toVO(EventRequest request);  // Request → VO만 담당
}

// mapper/query/EventQueryMapper.java
@Mapper(componentModel = "spring")
public interface EventQueryMapper {
    EventResponse toResponse(EventVO vo);  // VO → Response만 담당
}
```

#### 📁 `web/command/` vs `web/query/`
```java
// web/command/EventCommandController.java
@RestController
@RequestMapping("/api/events")
public class EventCommandController {
    private final EventCommandUseCase eventCommandUseCase;
    private final EventCommandMapper eventCommandMapper;
    
    @PostMapping
    public ResponseEntity<Void> save(@RequestBody EventRequest request) {
        EventVO vo = eventCommandMapper.toVO(request);
        eventCommandUseCase.saveEvent(vo);
        return ResponseEntity.ok().build();
    }
}

// web/query/EventQueryController.java
@RestController
@RequestMapping("/api/events")
public class EventQueryController {
    private final EventQueryUseCase eventQueryUseCase;
    private final EventQueryMapper eventQueryMapper;
    
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> get(@PathVariable String id) {
        Optional<EventVO> vo = eventQueryUseCase.getEvent(id);
        return vo.map(eventQueryMapper::toResponse)
                 .map(ResponseEntity::ok)
                 .orElse(ResponseEntity.notFound().build());
    }
}
```

---

### 4.3 sample-in-grpc (gRPC Adapter)

#### 📁 `dto/command/` vs `dto/query/`

gRPC 메시지 정의 (`.proto` 파일로 생성) - 패키지로 분리

```protobuf
// event_command.proto (command 패키지)
syntax = "proto3";
package com.ahnlab.edr.sample.in.grpc.dto.command;

message EventCommandRequest {
    string id = 1;
    string message = 2;
}

// event_query.proto (query 패키지)
syntax = "proto3";
package com.ahnlab.edr.sample.in.grpc.dto.query;

message EventQueryResponse {
    string id = 1;
    string message = 2;
}
```

**생성 구조**:
```
dto/
├── command/
│   └── EventCommandRequest.java         # Protobuf 자동 생성
├── query/
│   └── EventQueryResponse.java          # Protobuf 자동 생성
```

#### 📁 `mapper/command/` vs `mapper/query/`

gRPC 메시지 ↔ Core 객체 매핑 (HTTP Mapper 재사용)

```java
// mapper/command/EventCommandGrpcMapper.java
@Component
public class EventCommandGrpcMapper {
    private final EventCommandMapper eventCommandMapper;  // HTTP Mapper 주입
    
    /**
     * gRPC EventCommandRequest → EventVO 변환
     * 1. gRPC DTO → HTTP DTO 변환
     * 2. HTTP Mapper로 HTTP DTO → VO 변환
     */
    public EventVO toVO(EventCommandRequest request) {
        // gRPC 메시지를 HTTP DTO로 변환
        EventRequest httpRequest = new EventRequest(
            request.getId(),
            request.getMessage()
        );
        
        // HTTP Mapper를 통해 VO로 변환
        return eventCommandMapper.toVO(httpRequest);
    }
}

// mapper/query/EventQueryGrpcMapper.java
@Component
public class EventQueryGrpcMapper {
    private final EventQueryMapper eventQueryMapper;  // HTTP Mapper 주입
    
    /**
     * EventVO → gRPC EventQueryResponse 변환
     * 1. HTTP Mapper로 VO → HTTP DTO 변환
     * 2. HTTP DTO → gRPC Response 변환
     */
    public EventQueryResponse toResponse(EventVO vo) {
        // HTTP Mapper로 Response DTO로 변환
        EventResponse httpResponse = eventQueryMapper.toResponse(vo);
        
        // HTTP Response를 gRPC Response로 변환
        return EventQueryResponse.newBuilder()
                .setId(httpResponse.getId())
                .setMessage(httpResponse.getMessage())
                .build();
    }
}
```

**매핑 흐름**:
```
[Command 흐름]
gRPC Request → gRPC Mapper (command) → HTTP DTO → HTTP Mapper → VO → Service

[Query 흐름]
Service → VO → HTTP Mapper → HTTP DTO → gRPC Mapper (query) → gRPC Response
```

#### 📁 `command/` vs `query/` - Facade/Service

```java
// command/EventCommandGrpcFacade.java
@Component
public class EventCommandGrpcFacade {
    private final EventCommandUseCase eventCommandUseCase;
    private final EventCommandGrpcMapper mapper;
    
    /**
     * gRPC 요청을 받아 명령 처리 실행
     */
    public void saveEvent(EventCommandRequest request) {
        // Mapper를 통해 gRPC 요청을 VO로 변환
        EventVO vo = mapper.toVO(request);
        
        // Core UseCase 실행 (HTTP와 동일)
        eventCommandUseCase.saveEvent(vo);
    }
}

// query/EventQueryGrpcFacade.java
@Component
public class EventQueryGrpcFacade {
    private final EventQueryUseCase eventQueryUseCase;
    private final EventQueryGrpcMapper mapper;
    
    /**
     * gRPC 요청을 받아 조회 처리 실행
     */
    public EventQueryResponse getEvent(String id) {
        // Core UseCase 실행 (HTTP와 동일)
        return eventQueryUseCase.getEvent(id)
                                .map(mapper::toResponse)
                                .orElse(null);
    }
}

// gRPC Service (별도 패키지에서 생성된 .proto Stub 구현)
@GrpcService
public class EventCommandGrpcService extends EventCommandServiceGrpc.EventCommandServiceImplBase {
    private final EventCommandGrpcFacade facade;
    
    @Override
    public void saveEvent(EventCommandRequest request, 
                         StreamObserver<Empty> responseObserver) {
        try {
            facade.saveEvent(request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}

@GrpcService
public class EventQueryGrpcService extends EventQueryServiceGrpc.EventQueryServiceImplBase {
    private final EventQueryGrpcFacade facade;
    
    @Override
    public void getEvent(GetEventRequest request, 
                        StreamObserver<EventQueryResponse> responseObserver) {
        try {
            EventQueryResponse response = facade.getEvent(request.getId());
            if (response != null) {
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(
                    new io.grpc.StatusRuntimeException(io.grpc.Status.NOT_FOUND)
                );
            }
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }
}
```

**역할**:
- **DTO (command/query)**: 생성된 gRPC 프로토콜 버퍼 메시지 (`.proto` → 자동 생성)
- **Mapper (command/query)**: gRPC ↔ Core 변환 (HTTP Mapper 활용)
- **Facade (command/query)**: gRPC 요청을 받아 관련 UseCase 호출
- **gRPC Service**: Facade를 통해 비즈니스 로직 실행 (gRPC 에러 처리 담당)

---

### 4.4 sample-out-opensearch / sample-out-clickhouse

#### 📁 `store/` - 공유 저장소
```java
@Component
@OpenSearchOutboundEnabled  // @Conditional 어노테이션
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

#### 📁 `command/` - Command Adapter
```java
@Component
@OpenSearchOutboundEnabled
public class OpenSearchEventCommandAdapter implements EventCommandStorePort {
    
    private final OpenSearchEventStore store;
    
    @Override
    public void save(EventEntity entity) {
        store.put(entity.getId(), entity);
    }
}
```

#### 📁 `query/` - Query Adapter
```java
@Component
@OpenSearchOutboundEnabled
public class OpenSearchEventQueryAdapter implements EventQueryStorePort {
    
    private final OpenSearchEventStore store;
    
    @Override
    public Optional<EventEntity> findById(String id) {
        return store.get(id);
    }
}
```

---

## 5. CQRS 패턴 적용

### 5.1 개념

**CQRS (Command Query Responsibility Segregation)**는 쓰기(Command)와 읽기(Query)를 완전히 분리하는 패턴입니다.

### 5.2 분리 기준

| 레이어 | Command (쓰기) | Query (읽기) |
|--------|---------------|-------------|
| **HTTP Method** | POST, PUT, DELETE | GET |
| **UseCase** | EventCommandUseCase | EventQueryUseCase |
| **Service** | EventCommandService | EventQueryService |
| **StorePort** | EventCommandStorePort | EventQueryStorePort |
| **Adapter** | XxxCommandAdapter | XxxQueryAdapter |
| **DTO** | EventRequest (command/) | EventResponse (query/) |
| **Mapper** | EventCommandMapper | EventQueryMapper |
| **Controller** | EventCommandController | EventQueryController |

### 5.3 적용 이유

1. **단일 책임 원칙**: 각 클래스가 쓰기 또는 읽기 하나만 담당
2. **확장성**: 읽기/쓰기 최적화를 독립적으로 수행 가능
3. **명확성**: 코드를 보면 쓰기/읽기 구분이 즉시 명확
4. **유지보수**: 한쪽 변경이 다른 쪽에 영향을 최소화

---

## 6. 의존성 규칙 (필수)

### ⛔ 절대 금지 사항

```java
// ❌ Core가 외부 기술 의존
package com.ahnlab.edr.sample.core.application.service;

import org.springframework.web.bind.annotation.RestController;  // ❌ 금지!
import com.fasterxml.jackson.databind.ObjectMapper;             // ❌ 금지!

// ❌ Adapter를 직접 import
import com.ahnlab.edr.sample.out.opensearch.OpensearchAdapter; // ❌ 금지!
```

### ✅ 올바른 의존성

```java
// ✅ Core는 Port(인터페이스)만 의존
package com.ahnlab.edr.sample.core.application.command.service;

import com.ahnlab.edr.sample.core.application.command.port.out.EventCommandStorePort;
import com.ahnlab.edr.sample.core.domain.vo.EventVO;

@Service
public class EventCommandService {
    private final EventCommandStorePort storePort;  // ✅ 인터페이스 의존
    
    public EventCommandService(EventCommandStorePort storePort) {
        this.storePort = storePort;
    }
}
```

### 📌 의존성 방향 요약

```
                      [실행 흐름]
HTTP Request → Controller → Service → Port (Interface) ← Adapter → DB

                    [의존성 방향]
HTTP Request ─────────────────────→ Core ←───────── Adapter
              (Adapter가 Core를 의존)
```

### 📋 의존성 체크리스트

| 모듈 | 의존 가능 | 의존 금지 |
|------|----------|----------|
| **sample-core** | VO, Entity, Port(Interface), Exception | HTTP, gRPC, DB 라이브러리 |
| **sample-in-http** | Core (Port, VO) | Outbound Adapter 직접 참조 |
| **sample-in-grpc** | Core (Port, VO) | Outbound Adapter 직접 참조 |
| **sample-out-\*** | Core (Port, Entity) | Inbound Adapter, Controller, Facade |

---

## 7. 명명 규칙

### 7.1 인터페이스 (Port)

| 타입 | 명명 규칙 | 예시 |
|------|----------|------|
| **Inbound Port (UseCase)** | `{Domain}CommandUseCase` / `{Domain}QueryUseCase` | `EventCommandUseCase` |
| **Outbound Port (Store)** | `{Domain}CommandStorePort` / `{Domain}QueryStorePort` | `EventQueryStorePort` |

### 7.2 구현체

| 타입 | 명명 규칙 | 예시 |
|------|----------|------|
| **Service** | `{Domain}CommandService` / `{Domain}QueryService` | `EventCommandService` |
| **Adapter** | `{Tech}{Domain}CommandAdapter` / `{Tech}{Domain}QueryAdapter` | `OpenSearchEventCommandAdapter` |
| **Store** | `{Tech}{Domain}Store` | `ClickHouseEventStore` |

### 7.3 DTO & Mapper

| 타입 | 위치 | 명명 규칙 | 예시 |
|------|------|----------|------|
| **Request DTO** | `dto/command/` | `{Domain}Request` | `EventRequest` |
| **Response DTO** | `dto/query/` | `{Domain}Response` | `EventResponse` |
| **Command Mapper** | `mapper/command/` | `{Domain}CommandMapper` | `EventCommandMapper` |
| **Query Mapper** | `mapper/query/` | `{Domain}QueryMapper` | `EventQueryMapper` |

### 7.4 Controller

| 타입 | 위치 | 명명 규칙 | HTTP Method |
|------|------|----------|-------------|
| **Command** | `web/command/` | `{Domain}CommandController` | POST, PUT, DELETE |
| **Query** | `web/query/` | `{Domain}QueryController` | GET |

---

## 8. 예외 처리 체계

### 8.1 구조

```
ErrorCode (interface)
    ↑
    │ implements
    │
EventErrorCode (enum) ─────→ EventException ──────┐
UserErrorCode (enum)  ─────→ UserException       │
                                                  ↓
                                    ApplicationException (base)
```

### 8.2 ErrorCode 정의

```java
public enum EventErrorCode implements ErrorCode {
    
    // Query errors (EVT-1XX)
    EVENT_NOT_FOUND("EVT-101", "Event with id '%s' not found", 404),
    EVENT_QUERY_FAILED("EVT-102", "Failed to query event", 500),
    
    // Command errors (EVT-2XX)
    EVENT_SAVE_FAILED("EVT-201", "Failed to save event", 500),
    
    // Validation errors (EVT-3XX)
    EVENT_ID_REQUIRED("EVT-301", "Event ID is required", 400),
    EVENT_MESSAGE_REQUIRED("EVT-302", "Event message is required", 400);
    
    private final String code;
    private final String message;
    private final int httpStatus;
    
    // getter 메서드들...
}
```

### 8.3 예외 사용 예시

#### Service 계층: RuntimeException catch → 도메인 예외로 변환
```java
@Service
@Slf4j
public class EventCommandService implements EventCommandUseCase {
    
    @Override
    public void saveEvent(EventVO eventVO) {
        // 1. 입력 검증 → 비즈니스 예외
        if (eventVO == null) {
            throw new EventException(EventErrorCode.EVENT_INVALID_DATA, "event cannot be null");
        }
        
        if (eventVO.id() == null || eventVO.id().isBlank()) {
            throw new EventException(EventErrorCode.EVENT_ID_REQUIRED);
        }
        
        // 2. 저장 로직 → Adapter가 런타임 예외로 던짐
        EventEntity entity = toEntity(eventVO);
        try {
            storePort.save(entity);  // Port는 throws 절이 없음
        } catch (RuntimeException e) {
            // ✅ Adapter가 래핑한 RuntimeException → Core 도메인 예외로 변환
            log.error("Failed to save event: {}", eventVO.id(), e);
            throw new EventException(
                EventErrorCode.EVENT_SAVE_FAILED, 
                eventVO.id(), 
                e
            );
        }
    }
}
```

#### Outbound Adapter: 모든 기술 예외 → RuntimeException으로 래핑
```java
@Component
@Slf4j
public class OpenSearchEventCommandAdapter implements EventCommandStorePort {
    
    private final RestHighLevelClient client;
    private final OpenSearchEventStore store;
    
    /**
     * Event를 저장소에 저장
     * 모든 기술 예외(IOException, RuntimeException 등)는 RuntimeException으로 래핑
     * Service에서 catch하여 도메인 예외로 변환함
     */
    @Override
    public void save(EventEntity entity) {  // ✅ throws 절 없음
        try {
            // 실제 저장소 호출들
            IndexRequest request = new IndexRequest("events")
                    .id(entity.getId())
                    .source(toDocument(entity), XContentType.JSON);
            
            client.index(request, RequestOptions.DEFAULT);  // IOException 발생 가능
            store.save(entity);  // RuntimeException 발생 가능
            
        } catch (IOException e) {
            // ✅ checked exception(IOException) → RuntimeException으로 래핑
            log.error("Failed to save event to OpenSearch: {}", entity.getId(), e);
            throw new RuntimeException(
                "Failed to save event to OpenSearch: " + entity.getId(), 
                e
            );
        } catch (RuntimeException e) {
            // ✅ unchecked exception → 그대로 래핑하거나 로깅 후 전파
            log.error("Unexpected error saving event: {}", entity.getId(), e);
            throw e;  // 또는 throw new RuntimeException("Failed to save", e);
        }
    }
}

// Query Adapter도 동일한 패턴
@Component
@Slf4j
public class OpenSearchEventQueryAdapter implements EventQueryStorePort {
    
    private final RestHighLevelClient client;
    private final OpenSearchEventStore store;
    
    @Override
    public Optional<EventEntity> findById(String id) {  // ✅ throws 절 없음
        try {
            GetRequest request = new GetRequest("events", id);
            GetResponse response = client.get(request, RequestOptions.DEFAULT);
            
            if (response.isExists()) {
                return Optional.of(fromDocument(response.getSourceAsMap()));
            }
            return Optional.ofNullable(store.findById(id));
            
        } catch (IOException e) {
            // ✅ IOException → RuntimeException으로 래핑
            log.error("Failed to query event from OpenSearch: {}", id, e);
            throw new RuntimeException(
                "Failed to query event from OpenSearch: " + id, 
                e
            );
        } catch (RuntimeException e) {
            // ✅ RuntimeException 그대로 전파
            log.error("Unexpected error querying event: {}", id, e);
            throw e;
        }
    }
}
```

#### Port 인터페이스: throws 절 없음 (깔끔한 계약)
```java
// EventCommandStorePort.java
public interface EventCommandStorePort {
    
    /**
     * Event를 저장소에 저장
     * 
     * @param entity 저장할 Event Entity
     * @throws RuntimeException 저장소 작업 실패 시 (Adapter에서 기술 예외를 래핑)
     * 
     * 주의: Service가 RuntimeException을 catch하여 도메인 예외로 변환함
     */
    void save(EventEntity entity);  // ✅ throws 절 없음
}

// EventQueryStorePort.java
public interface EventQueryStorePort {
    
    /**
     * ID로 Event 조회
     * 
     * @param id 조회할 Event ID
     * @return Event Entity (없으면 Optional.empty())
     * @throws RuntimeException 저장소 작업 실패 시 (Adapter에서 기술 예외를 래핑)
     * 
     * 주의: Service가 RuntimeException을 catch하여 도메인 예외로 변환함
     */
    Optional<EventEntity> findById(String id);  // ✅ throws 절 없음
}
```

### 8.4 에러 코드 체계

| 도메인 | 범위 | 카테고리 | 예시 |
|--------|------|---------|------|
| **Event** | EVT-1XX | Query 오류 | EVT-101 (NOT_FOUND) |
|  | EVT-2XX | Command 오류 | EVT-201 (SAVE_FAILED) |
|  | EVT-3XX | Validation 오류 | EVT-301 (ID_REQUIRED) |
| **User** | USR-1XX | Query 오류 | USR-101 (NOT_FOUND) |
|  | USR-2XX | Command 오류 | USR-201 (SAVE_FAILED) |
|  | USR-3XX | Validation 오류 | USR-301 (ID_REQUIRED) |

---

## 9. 주의사항 및 제약사항

### 🔴 절대 하지 말아야 할 것

#### 9.1 의존성 위반
```java
// ❌ Core에서 Controller import 금지
package com.ahnlab.edr.sample.core.application.service;

import org.springframework.web.bind.annotation.*;  // ❌
import com.ahnlab.edr.sample.in.http.web.EventCommandController;  // ❌
```

#### 9.2 Command/Query 혼합
```java
// ❌ Command Controller에서 Query 작업 금지
@RestController
public class EventCommandController {
    
    @PostMapping
    public ResponseEntity<Void> save(@RequestBody EventRequest request) {
        // ...
    }
    
    @GetMapping("/{id}")  // ❌ Command Controller에 GET 메서드 금지!
    public ResponseEntity<EventResponse> get(@PathVariable String id) {
        // ...
    }
}
```

#### 9.3 Port 구현 누락
```java
// ❌ Service가 Port를 구현하지 않음
@Service
public class EventCommandService {  // ❌ implements EventCommandUseCase 누락!
    
    public void saveEvent(EventVO vo) {
        // ...
    }
}
```

#### 9.4 직접 Adapter 주입
```java
// ❌ Service에서 Adapter를 직접 주입
@Service
public class EventCommandService {
    
    private final OpenSearchEventCommandAdapter adapter;  // ❌ Port가 아닌 구현체 주입!
    
    public EventCommandService(OpenSearchEventCommandAdapter adapter) {  // ❌
        this.adapter = adapter;
    }
}

// ✅ 올바른 방법: Port 주입
@Service
public class EventCommandService implements EventCommandUseCase {
    
    private final EventCommandStorePort storePort;  // ✅ 인터페이스 주입!
    
    public EventCommandService(EventCommandStorePort storePort) {  // ✅
        this.storePort = storePort;
    }
}
```

#### 9.5 DTO 패키지 오배치
```java
// ❌ Request DTO를 query 패키지에 배치
package com.ahnlab.edr.sample.in.http.dto.query;  // ❌ query 패키지

public class EventRequest {  // ❌ Request는 command에 위치해야 함
    private String id;
}

// ✅ 올바른 위치
package com.ahnlab.edr.sample.in.http.dto.command;  // ✅
public class EventRequest { ... }
```

#### 9.6 Mapper 책임 혼합
```java
// ❌ 하나의 Mapper에 Request→VO, VO→Response 모두 포함
@Mapper(componentModel = "spring")
public interface EventHttpMapper {  // ❌ Command와 Query 혼합!
    EventVO toVO(EventRequest request);
    EventResponse toResponse(EventVO vo);
}

// ✅ 올바른 분리
// mapper/command/EventCommandMapper.java
public interface EventCommandMapper {
    EventVO toVO(EventRequest request);  // Request→VO만
}

// mapper/query/EventQueryMapper.java
public interface EventQueryMapper {
    EventResponse toResponse(EventVO vo);  // VO→Response만
}
```

---

### 🟡 주의해야 할 것

#### 9.7 공유 저장소 (Store) 사용
- Outbound Adapter의 Command/Query는 동일한 Store를 공유
- Store는 **스레드 안전**해야 함 (`ConcurrentHashMap` 사용)

```java
// ✅ 올바른 Store 구현
@Component
public class OpenSearchEventStore {
    private final Map<String, EventEntity> store = new ConcurrentHashMap<>();  // ✅
    // ...
}

// ❌ 잘못된 Store 구현
@Component
public class OpenSearchEventStore {
    private final Map<String, EventEntity> store = new HashMap<>();  // ❌ 동시성 문제!
    // ...
}
```

#### 9.8 VO 검증
- VO(Value Object)는 생성 시점에 검증 로직 포함 (Record compact constructor)

```java
public record EventVO(String id, String message) {
    public EventVO {  // Compact constructor
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Event message cannot be null");
        }
    }
}
```

#### 9.9 Service 계층 검증
- Service는 VO 생성 전에 추가 비즈니스 검증 수행

```java
@Override
public void saveEvent(EventVO eventVO) {
    // 1. Null 체크
    if (eventVO == null) {
        throw new EventException(EventErrorCode.EVENT_INVALID_DATA);
    }
    
    // 2. 비즈니스 규칙 검증
    if (eventVO.id() == null || eventVO.id().isBlank()) {
        throw new EventException(EventErrorCode.EVENT_ID_REQUIRED);
    }
    
    // 3. 저장 로직
    storePort.save(entity);
}
```

#### 9.10 @Conditional 어노테이션 활용
- Adapter는 설정에 따라 활성화/비활성화 가능
- 반드시 대응하는 `@Conditional` 어노테이션 사용

```java
@Component
@OpenSearchOutboundEnabled  // sample.outbound.opensearch-enabled=true
public class OpenSearchEventCommandAdapter implements EventCommandStorePort {
    // ...
}

@Component
@ClickHouseOutboundEnabled  // sample.outbound.clickhouse-enabled=true
public class ClickHouseEventCommandAdapter implements EventCommandStorePort {
    // ...
}
```

#### 9.11 Outbound Adapter 예외 처리
- Adapter는 **저장소 기술 예외를 그대로 던짐** (IOException, SQLException 등)
- Core(Service)에서 기술 예외를 catch하여 **도메인 예외로 변환**
- 이를 통해 **팀 간 의존성 최소화**: Adapter 팀은 기술만 이해하면 됨

```java
// ❌ 잘못된 방법: Adapter에서 도메인 예외로 변환 (Core ErrorCode 의존)
@Component
public class OpenSearchEventCommandAdapter implements EventCommandStorePort {
    @Override
    public void save(EventEntity entity) {
        try {
            client.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            // ❌ Adapter가 Core의 EventException, EventErrorCode 의존
            throw new EventException(EventErrorCode.EVENT_SAVE_FAILED, entity.getId(), e);
        }
    }
}

// ✅ 올바른 방법: Adapter는 기술 예외를 RuntimeException으로 래핑
@Component
@Slf4j
public class OpenSearchEventCommandAdapter implements EventCommandStorePort {
    
    @Override
    public void save(EventEntity entity) {  // ✅ throws 절 없음
        try {
            IndexRequest request = new IndexRequest("events")
                    .id(entity.getId())
                    .source(toDocument(entity), XContentType.JSON);
            
            client.index(request, RequestOptions.DEFAULT);  // IOException 가능
        } catch (IOException e) {
            log.error("Failed to save event to OpenSearch: {}", entity.getId(), e);
            throw new RuntimeException("Failed to save event", e);  // ✅ RuntimeException 래핑
        } catch (RuntimeException e) {
            log.error("Unexpected error saving event: {}", entity.getId(), e);
            throw e;  // ✅ RuntimeException 그대로 전파
        }
    }
}

// ✅ Service에서 RuntimeException을 catch하고 도메인 예외로 변환
@Service
@Slf4j
public class EventCommandService implements EventCommandUseCase {
    
    @Override
    public void saveEvent(EventVO eventVO) {
        try {
            storePort.save(entity);  // Port는 throws 절이 없음
        } catch (RuntimeException e) {
            // ✅ 한 가지 예외 타입만 처리 (깔끔함)
            log.error("Failed to save event: {}", eventVO.id(), e);
            throw new EventException(
                EventErrorCode.EVENT_SAVE_FAILED,
                eventVO.id(),
                e
            );
        }
    }
}
```

**팀 간 책임 분리**:
- **Adapter 팀**: 저장소 기술 이해 → IOException 등을 모두 **RuntimeException으로 래핑**
- **Core 팀**: 비즈니스 로직 → RuntimeException을 catch하여 **도메인 예외로 변환**
- **계약**: Port 인터페이스는 **throws 절이 없음** (깔끔함)

**장점**:
1. **깔끔한 Interface**: throws 절이 없어서 복잡하지 않음
2. **타입 안전성**: Service는 RuntimeException 한 가지만 catch하면 됨
3. **팀 독립성**: Adapter 팀이 Core 패키지 import 불필요
4. **새로운 저장소 추가**: throws절 변경 없이 Adapter만 추가 가능
5. **Spring 표준 패턴**: DataAccessException 등 Spring도 이 패턴 사용

---

## 10. 개발 가이드

### 10.1 새로운 도메인 추가 절차 (요약)

#### Step 1: Core 계층 (sample-core)
1. VO 생성 (`domain/vo/`)
2. Entity 생성 (`domain/entity/`)
3. Exception 생성 (`application/exception/{domain}/`)
4. Command Port 생성 (`application/command/port/`)
5. Query Port 생성 (`application/query/port/`)
6. Service 구현 (`application/command/service/`, `application/query/service/`)
7. Mapper 생성 (`application/mapper/`)

#### Step 2: HTTP Adapter (sample-in-http)
8. DTO 생성 (`dto/command/`, `dto/query/`)
9. Mapper 생성 (`mapper/command/`, `mapper/query/`)
10. Controller 생성 (`web/command/`, `web/query/`)

#### Step 2.5: gRPC Adapter (sample-in-grpc)
10.5. gRPC DTO 정의 (`dto/command/`, `dto/query/`, `.proto` 파일)
10.6. gRPC Mapper 생성 (`mapper/command/`, `mapper/query/`)
10.7. gRPC Facade 생성 (`command/`, `query/`)
10.8. gRPC Service 생성 (Facade 호출)

#### Step 3: Outbound Adapter (sample-out-opensearch, sample-out-clickhouse)
11. Store 생성 (`store/`)
12. Adapter 생성 (`command/`, `query/`)
    - ✅ Port 인터페이스는 **throws 절이 없음**
    - ✅ Adapter는 **모든 기술 예외를 RuntimeException으로 래핑**
    - ✅ Service에서 RuntimeException을 catch하여 도메인 예외로 변환

### 10.2 개발 체크리스트

#### ✅ 코드 작성 전
- [ ] 도메인 이름 및 책임 명확히 정의
- [ ] Command/Query 분리 필요성 확인
- [ ] 의존성 방향이 올바른지 검토

#### ✅ 코드 작성 중
- [ ] Port(인터페이스)를 먼저 정의
- [ ] Core는 외부 기술 의존 없는지 확인
- [ ] DTO는 command/query 패키지에 올바르게 배치
- [ ] Mapper는 단일 책임만 가지도록 분리

#### ✅ 코드 작성 후
- [ ] 모든 UseCase 인터페이스가 구현되었는지 확인
- [ ] 모든 StorePort 인터페이스가 Adapter에서 구현되었는지 확인
- [ ] Exception 및 ErrorCode가 정의되었는지 확인
- [ ] 검증 로직이 Service 계층에 포함되었는지 확인
- [ ] Controller가 command/query 패키지에 올바르게 분리되었는지 확인

#### ✅ 테스트
- [ ] 단위 테스트 작성 (Service, Mapper)
- [ ] 통합 테스트 작성 (Controller, Adapter)
- [ ] 예외 시나리오 테스트 작성

### 10.3 리뷰 포인트

#### PR 리뷰 시 체크 항목

1. **의존성 방향**
   - Core가 외부 기술을 직접 import하지 않았는가?
   - Adapter가 Port를 올바르게 구현했는가?

2. **CQRS 분리**
   - Command와 Query가 명확히 분리되었는가?
   - HTTP/gRPC DTO/Mapper/Controller(Facade)가 올바른 패키지에 위치하는가?
   - gRPC의 경우 Mapper가 Core와 gRPC 객체 간 변환을 올바르게 수행하는가?

3. **예외 처리**
   - 도메인별 ErrorCode가 정의되었는가?
   - Service에서 비즈니스 검증이 수행되는가?
   - ✅ **Adapter는 모든 기술 예외를 RuntimeException으로 래핑하는가?**
   - ✅ **Port 인터페이스가 throws 절 없이 깔끔한가?**
   - ✅ **Service에서 RuntimeException을 catch하여 도메인 예외로 변환하는가?**

4. **명명 규칙**
   - UseCase, StorePort, Service, Adapter 이름이 규칙을 따르는가?
   - DTO 이름이 Request/Response로 명확한가?

5. **테스트**
   - 주요 비즈니스 로직에 대한 테스트가 작성되었는가?
   - 예외 케이스 테스트가 포함되었는가?

---

## 11. FAQ

### Q1. 왜 Command와 Query를 이렇게까지 분리하나요?
**A**: 단일 책임 원칙과 확장성 때문입니다. 읽기와 쓰기는 특성이 다르며, 독립적으로 최적화할 수 있습니다. 또한 향후 Read Replica나 CQRS Event Sourcing으로 확장할 가능성을 열어둡니다.

### Q2. Port가 인터페이스라면 구현체는 어디서 주입되나요?
**A**: Spring의 DI(Dependency Injection)가 자동으로 처리합니다. 예를 들어 `EventCommandStorePort`를 구현한 `OpenSearchEventCommandAdapter`가 있다면, Service에 Port를 주입할 때 Spring이 자동으로 구현체를 찾아 주입합니다.

### Q3. 여러 Adapter가 같은 Port를 구현하면 어떻게 되나요?
**A**: `@Conditional` 어노테이션으로 환경에 따라 활성화될 Adapter를 제어합니다. 예: `@OpenSearchOutboundEnabled`, `@ClickHouseOutboundEnabled`

### Q4. VO와 Entity의 차이는 무엇인가요?
**A**:
- **VO (Value Object)**: 도메인 계층, 불변 객체, 비즈니스 검증 포함
- **Entity**: 영속성 계층, 가변 객체, DB 저장 형태

### Q5. Mapper가 너무 많은 것 같은데, 합쳐도 되나요?
**A**: 안 됩니다. Command Mapper와 Query Mapper는 책임이 다르므로 분리해야 합니다. 코드 양보다 응집도와 명확성이 우선입니다.

### Q6. 왜 Store를 별도로 만들어야 하나요?
**A**: Command와 Query Adapter가 동일한 데이터를 참조해야 하기 때문입니다. Store를 공유함으로써 데이터 일관성을 유지하면서도 Command/Query를 분리할 수 있습니다.

### Q7. 모든 계층에서 DTO를 변환해야 하나요?
**A**: 네. 각 계층은 자신에게 맞는 객체 형태를 가져야 합니다:
- HTTP Request/Response → DTO
- 도메인 로직 → VO
- 영속성 → Entity

이를 통해 각 계층의 변경이 다른 계층에 영향을 주지 않도록 보호합니다.

### Q8. 간단한 CRUD도 이렇게 복잡하게 해야 하나요?
**A**: 초기에는 과하게 느껴질 수 있지만, 프로젝트가 성장하면서 그 가치가 드러납니다. 명확한 책임 분리와 의존성 제어는 장기적으로 유지보수 비용을 크게 줄여줍니다.

### Q9. HTTP와 gRPC 모두 지원하려면 코드를 두 배로 작성해야 하나요?
**A**: 아닙니다. HTTP와 gRPC는 Inbound Adapter일 뿐, 실제 비즈니스 로직은 Core에 한 번만 정의합니다:
- **공유**: Core의 UseCase, Service, Exception, Mapper는 HTTP/gRPC 모두에서 사용
- **분리**: HTTP DTO/Controller와 gRPC DTO/Facade만 각각 정의
- **결과**: 어댑터 수준의 변환 로직만 추가되고, 비즈니스 로직은 공유

### Q10. gRPC Mapper에서 HTTP Mapper를 호출하는 것이 맞나요?
**A**: 네, 정확합니다. gRPC Mapper는:
1. gRPC 메시지를 HTTP DTO로 변환
2. HTTP Mapper를 호출해 HTTP DTO → VO 변환
3. 결과적으로 gRPC 메시지 → VO 변환

이렇게 함으로써 VO ↔ DTO 변환 로직을 한 곳(HTTP Mapper)에서만 관리합니다.

### Q11. Outbound Adapter에서 저장소 예외가 발생하면 어떻게 처리해야 할까?

**A**: **모든 기술 예외를 RuntimeException으로 래핑**하고, Service에서 catch하여 도메인 예외로 변환하세요.

#### 왜 이렇게?

**깔끔한 Interface + 타입 안전성**:

1. **Port 인터페이스**: throws 절이 없음 → 깔끔함
   ```java
   public interface EventCommandStorePort {
       void save(EventEntity entity);  // ✅ throws 절 없음
   }
   ```

2. **Adapter**: 모든 예외 → RuntimeException으로 래핑
   ```java
   @Component
   public class OpenSearchEventCommandAdapter implements EventCommandStorePort {
       @Override
       public void save(EventEntity entity) {
           try {
               client.index(request, RequestOptions.DEFAULT);
           } catch (IOException e) {
               throw new RuntimeException("Failed to save", e);  // ✅ 래핑
           }
       }
   }
   ```

3. **Service**: RuntimeException catch → 도메인 예외로 변환
   ```java
   @Service
   public class EventCommandService implements EventCommandUseCase {
       @Override
       public void saveEvent(EventVO eventVO) {
           try {
               storePort.save(entity);
           } catch (RuntimeException e) {
               // ✅ 한 가지 타입만 처리
               throw new EventException(EventErrorCode.EVENT_SAVE_FAILED, eventVO.id(), e);
           }
       }
   }
   ```

#### 비교: throws 절 vs RuntimeException 래핑

| 방식 | throws절 | Adapter | Service | Interface | 문제점 |
|------|---------|--------|---------|-----------|--------|
| ❌ throws 명시 | `throws IOException, SQLException, ...` | 그대로 던짐 | 다양한 catch | 복잡함 | 새로운 저장소 추가 시 Port 수정 필요 |
| ✅ RuntimeException 래핑 | 없음 | 예외 래핑 | 한 가지 catch | 깔끔함 | Port 수정 불필요, 새로운 저장소 추가 간편 |

#### Spring이 이 패턴을 사용하는 이유

Spring의 `DataAccessException`도 RuntimeException 기반입니다:
```java
// Spring Data Access
public interface JdbcTemplate {
    List<T> query(String sql, RowMapper<T> mapper);  
    // throws 절 없음! SQLException → DataAccessException으로 래핑
}
```

### Q12. 모든 Adapter가 같은 RuntimeException을 던지면 Service에서 구분 불가능하지 않을까?

**A**: 구분할 필요가 없습니다. Service는 **모든 저장소 실패를 동일하게 처리**:

```java
@Service
public class EventCommandService implements EventCommandUseCase {
    @Override
    public void saveEvent(EventVO eventVO) {
        try {
            storePort.save(entity);  // OpenSearch든 ClickHouse든 상관없음
        } catch (RuntimeException e) {
            throw new EventException(EventErrorCode.EVENT_SAVE_FAILED, eventVO.id(), e);
        }
    }
}
```

**구분이 필요하면?** `getCause()` 또는 로깅으로 확인:
```java
try {
    storePort.save(entity);
} catch (RuntimeException e) {
    if (e.getCause() instanceof IOException) {
        // I/O 오류 처리
    } else if (e.getCause() instanceof SQLException) {
        // DB 오류 처리
    }
}
```

### Q13. RuntimeException으로 래핑하면 원인(cause)을 잃지 않을까?

**A**: 아닙니다. **원인을 전달**하면 됩니다:

```java
@Component
public class OpenSearchEventCommandAdapter implements EventCommandStorePort {
    @Override
    public void save(EventEntity entity) {
        try {
            client.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            // ✅ 원인을 포함한 RuntimeException 생성
            throw new RuntimeException("Failed to save event", e);
            //                                          ↑ 원인 전달
        }
    }
}
```

로깅/모니터링에서 원인 확인:
```java
catch (EventException e) {
    log.error("Event save failed", e);  // 원인까지 모두 로깅됨
    // [주요정보] Failed to save event - IOException: Connection timeout
}
```

---

## 📞 문의

구조 관련 질문이나 개선 제안은 팀 채널 또는 Tech Lead에게 문의하세요.

**문서 버전**: 1.0  
**최종 수정일**: 2026-02-27
