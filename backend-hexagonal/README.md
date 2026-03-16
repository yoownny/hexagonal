# EDR Sample Backend — 헥사고날 아키텍처

AhnLab EDR(Endpoint Detection and Response) 솔루션의 백엔드 샘플 프로젝트.  
**헥사고날 아키텍처(Ports and Adapters)** 와 **CQRS** 패턴을 엄격하게 적용한 구조를 제공한다.

---

## 목차

1. [아키텍처 개요](#아키텍처-개요)
2. [모듈 구조](#모듈-구조)
3. [패키지 구조](#패키지-구조)
4. [CQRS 패턴](#cqrs-패턴)
5. [의존성 규칙](#의존성-규칙)
6. [예외 처리 체계](#예외-처리-체계)
7. [어댑터 ON/OFF 설정](#어댑터-onoff-설정)
8. [REST API](#rest-api)
9. [빌드 및 실행](#빌드-및-실행)
10. [Swagger UI](#swagger-ui)

---

## 아키텍처 개요

```
┌─────────────────────────────────────────────────────────────────┐
│                        Inbound Adapters                         │
│              sample-in-http        sample-in-grpc               │
│         (REST Controller)       (gRPC Facade/Service)           │
└──────────────────────┬──────────────────────────────────────────┘
                       │  UseCase Port (interface) — VO 전달
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Application Core                           │
│                       sample-core                               │
│                                                                 │
│   VO (record)  │  UseCase(in-port)  │  Service  │  Exception    │
│                           │                                     │
│                  StorePort (out-port) — VO 전달                  │
└──────────────────────┬──────────────────────────────────────────┘
                       │  StorePort (interface) — VO 전달
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Outbound Adapters                         │
│         sample-out-opensearch    sample-out-clickhouse          │
│         (Adapter + Store +       (Adapter + Store +             │
│          VO→Entity Mapper)        VO→Entity Mapper)             │
└─────────────────────────────────────────────────────────────────┘

sample-config   : @Conditional 어노테이션 — 어댑터 런타임 ON/OFF
sample-bootstrap: Spring Boot 진입점 — 모든 모듈 조합
```

### 핵심 설계 원칙

| 원칙 | 설명 |
|------|------|
| **의존성 역전** | Adapter → Core 방향만 허용. Core는 절대 Adapter를 의존하지 않음 |
| **포트와 어댑터** | Core는 인터페이스(Port)만 알고, 구현체(Adapter)는 런타임에 주입됨 |
| **VO 경계** | 레이어 간 데이터 전달은 VO(Value Object)로 통일 |
| **Entity 캡슐화** | VO → Entity 변환 책임은 Outbound Adapter 내부에만 존재 |

---

## 모듈 구조

```
backend-hexagonal/
├── sample-config/           조건부 설정 어노테이션
├── sample-core/             비즈니스 로직 (도메인 핵심)
├── sample-in-http/          HTTP REST Inbound Adapter
├── sample-in-grpc/          gRPC Inbound Adapter
├── sample-out-opensearch/   OpenSearch Outbound Adapter
├── sample-out-clickhouse/   ClickHouse Outbound Adapter
└── sample-bootstrap/        Spring Boot 진입점
```

### 모듈별 역할

| 모듈 | 역할 | 의존 모듈 |
|------|------|-----------|
| `sample-config` | `@Conditional` 어노테이션 정의 (어댑터 활성화 조건) | — |
| `sample-core` | VO, UseCase Port, StorePort, Service, Exception | — |
| `sample-in-http` | HTTP 요청 수신 → UseCase 호출, DTO ↔ VO 변환 | core, config |
| `sample-in-grpc` | gRPC 요청 수신 → UseCase 호출, 메시지 ↔ VO 변환 | core, config |
| `sample-out-opensearch` | OpenSearch 저장/조회, VO → Entity 변환 | core, config |
| `sample-out-clickhouse` | ClickHouse 저장/조회, VO → Entity 변환 | core, config |
| `sample-bootstrap` | 모든 모듈 조합, `GlobalExceptionHandler`, `OpenApiConfig` | 전체 |

---

## 패키지 구조

### sample-core

```
sample-core/src/main/java/com/ahnlab/edr/sample/core/
├── domain/
│   └── vo/
│       └── EventVO.java                  불변 값 객체 (record)
└── application/
    ├── command/
    │   ├── port/
    │   │   ├── in/
    │   │   │   └── EventCommandUseCase.java    인바운드 포트 (인터페이스)
    │   │   └── out/
    │   │       └── EventCommandStorePort.java  아웃바운드 포트 (인터페이스)
    │   └── service/
    │       └── EventCommandService.java        UseCase 구현체
    ├── query/
    │   ├── port/
    │   │   ├── in/
    │   │   │   └── EventQueryUseCase.java
    │   │   └── out/
    │   │       └── EventQueryStorePort.java
    │   └── service/
    │       └── EventQueryService.java
    └── exception/
        ├── ApplicationException.java     기반 예외
        ├── ErrorCode.java                에러 코드 인터페이스
        └── event/
            ├── EventErrorCode.java       도메인 에러 코드 (enum)
            └── EventException.java       도메인 예외
```

### sample-in-http

```
sample-in-http/src/main/java/com/ahnlab/edr/sample/in/http/
├── dto/
│   ├── command/
│   │   └── EventRequest.java    POST 요청 바디
│   └── query/
│       └── EventResponse.java   GET 응답 바디
├── mapper/
│   ├── command/
│   │   └── EventCommandMapper.java    EventRequest → EventVO
│   └── query/
│       └── EventQueryMapper.java      EventVO → EventResponse
└── web/
    ├── command/
    │   └── EventCommandController.java    POST /api/events
    └── query/
        └── EventQueryController.java      GET /api/events/{id}
```

### sample-in-grpc

```
sample-in-grpc/src/main/java/com/ahnlab/edr/sample/in/grpc/
├── command/
│   └── GrpcEventCommandFacade.java    gRPC 명령 처리 → CommandUseCase 호출
├── query/
│   └── GrpcEventQueryFacade.java      gRPC 조회 처리 → QueryUseCase 호출
└── mapper/
    ├── command/
    │   └── GrpcEventCommandMapper.java    gRPC 메시지 → VO
    └── query/
        └── GrpcEventQueryMapper.java      VO → gRPC 응답
```

### sample-out-opensearch / sample-out-clickhouse

```
sample-out-{tech}/src/main/java/com/ahnlab/edr/sample/out/{tech}/
├── store/
│   └── {Tech}EventStore.java       저장소 클라이언트 (ConcurrentHashMap 기반)
├── command/
│   └── {Tech}EventCommandAdapter.java    EventCommandStorePort 구현
├── query/
│   └── {Tech}EventQueryAdapter.java      EventQueryStorePort 구현
└── mapper/
    └── EventEntityMapper.java      VO ↔ Entity 변환 (Adapter 내부 전용)
```

> `EventEntity`는 각 Outbound 모듈 내부에만 존재하며, Core에 노출되지 않는다.

### sample-bootstrap

```
sample-bootstrap/src/main/java/com/ahnlab/edr/sample/bootstrap/
├── SampleBootstrapApplication.java    진입점 (@SpringBootApplication)
├── GlobalExceptionHandler.java        전역 예외 처리 (@RestControllerAdvice)
├── ApiResponse.java                   표준 API 응답 래퍼
└── OpenApiConfig.java                 OpenAPI 3.0 / Swagger 설정
```

---

## CQRS 패턴

쓰기(Command)와 읽기(Query)는 모든 레이어에서 완전히 분리된다.

| 레이어 | Command | Query |
|--------|---------|-------|
| **UseCase** (in-port) | `EventCommandUseCase` | `EventQueryUseCase` |
| **Service** | `EventCommandService` | `EventQueryService` |
| **StorePort** (out-port) | `EventCommandStorePort` | `EventQueryStorePort` |
| **Adapter** | `{Tech}EventCommandAdapter` | `{Tech}EventQueryAdapter` |
| **HTTP Controller** | `EventCommandController` (POST) | `EventQueryController` (GET) |
| **gRPC Facade** | `GrpcEventCommandFacade` | `GrpcEventQueryFacade` |
| **Request/Response DTO** | `dto/command/EventRequest` | `dto/query/EventResponse` |
| **HTTP Mapper** | `mapper/command/EventCommandMapper` | `mapper/query/EventQueryMapper` |

### 데이터 흐름

**Command (저장)**
```
HTTP POST /api/events
  → EventCommandController
  → EventCommandMapper (EventRequest → EventVO)
  → EventCommandUseCase#saveEvent(EventVO)
  → EventCommandService
  → EventCommandStorePort#save(EventVO)
  → {Tech}EventCommandAdapter
  → EventEntityMapper (EventVO → EventEntity)
  → {Tech}EventStore#put(id, entity)
```

**Query (조회)**
```
HTTP GET /api/events/{id}
  → EventQueryController
  → EventQueryUseCase#getEvent(id)
  → EventQueryService
  → EventQueryStorePort#findById(id)
  → {Tech}EventQueryAdapter
  → {Tech}EventStore#get(id)
  → EventEntityMapper (EventEntity → EventVO)
  → EventQueryMapper (EventVO → EventResponse)
  → HTTP 200 OK
```

---

## 의존성 규칙

### 허용 / 금지 방향

| 모듈 | 의존 가능 | 의존 금지 |
|------|----------|----------|
| `sample-core` | VO, Port, Exception, Java 표준, Spring DI | HTTP/gRPC/DB 라이브러리, Adapter 패키지 |
| `sample-in-*` | Core (UseCase Port, VO) | StorePort 직접 참조, Outbound Adapter |
| `sample-out-*` | Core (StorePort, VO), 자체 Entity | Inbound Adapter, Core의 예외 클래스 |

### 코드 예시

```java
// ✅ Core Service — Port 인터페이스에만 의존
@Service
@Slf4j
@RequiredArgsConstructor
public class EventCommandService implements EventCommandUseCase {
    private final EventCommandStorePort storePort; // ✅ 인터페이스
}

// ✅ Inbound Adapter — UseCase Port만 참조
@RestController
@RequiredArgsConstructor
public class EventCommandController {
    private final EventCommandUseCase eventCommandUseCase; // ✅ UseCase 인터페이스
}

// ❌ 절대 금지 — Core가 기술 스택 직접 import
import org.springframework.web.bind.annotation.RestController; // ❌
import com.ahnlab.edr.sample.out.opensearch.*;                 // ❌

// ❌ 절대 금지 — Inbound가 StorePort 직접 참조
private final EventCommandStorePort storePort; // ❌ (Inbound 어댑터에서)

// ❌ 절대 금지 — Outbound가 Core의 도메인 예외 사용
import com.ahnlab.edr.sample.core.application.exception.event.EventException; // ❌
```

---

## 예외 처리 체계

### 예외 계층 구조

```
RuntimeException
  └─ ApplicationException          (기반 예외, errorCode + message + cause)
      └─ EventException             (이벤트 도메인 예외)
```

### ErrorCode 체계

```java
public enum EventErrorCode implements ErrorCode {
    // Query (EVT-1XX)
    EVENT_NOT_FOUND("EVT-101", "Event with id '%s' not found", 404),
    EVENT_QUERY_FAILED("EVT-102", "Failed to query event with id '%s'", 500),

    // Command (EVT-2XX)
    EVENT_SAVE_FAILED("EVT-201", "Failed to save event with id '%s'", 500),

    // Validation (EVT-3XX)
    EVENT_ID_REQUIRED("EVT-301", "Event ID is required", 400),
    EVENT_MESSAGE_REQUIRED("EVT-302", "Event message is required", 400);
}
```

### 레이어별 예외 처리 역할

| 레이어 | 역할 |
|--------|------|
| **Port 인터페이스** | `throws` 절 선언 없음 (깔끔한 계약 유지) |
| **Outbound Adapter** | 기술 예외(IOException 등) → `RuntimeException`으로 래핑 |
| **Service (Core)** | `RuntimeException` catch → 도메인 예외(`EventException`)로 변환 |
| **GlobalExceptionHandler** | `ApplicationException` → HTTP 상태코드 + `ApiResponse` 반환 |

### API 에러 응답 형식

```json
{
  "success": false,
  "errorCode": "EVT-101",
  "message": "Event with id 'abc-123' not found"
}
```

---

## 어댑터 ON/OFF 설정

`sample-config` 모듈의 `@Conditional` 어노테이션과 `application.yml` 설정으로 어댑터를 런타임에 선택한다.

### application.yml

```yaml
sample:
  inbound:
    http-enabled: true     # HTTP REST 어댑터 활성화 여부
    grpc-enabled: false    # gRPC 어댑터 활성화 여부
  outbound: clickhouse     # clickhouse | opensearch
```

### @Conditional 어노테이션

| 어노테이션 | 조건 | 적용 대상 |
|------------|------|----------|
| `@HttpInboundEnabled` | `sample.inbound.http-enabled=true` | HTTP Controller, Mapper |
| `@GrpcInboundEnabled` | `sample.inbound.grpc-enabled=true` | gRPC Facade, Mapper |
| `@OpenSearchOutboundEnabled` | `sample.outbound=opensearch` | OpenSearch Adapter, Store |
| `@ClickHouseOutboundEnabled` | `sample.outbound=clickhouse` | ClickHouse Adapter, Store |

### 사용 예시

```java
// ✅ OpenSearch 어댑터 — 어노테이션 필수
@Component
@OpenSearchOutboundEnabled
@Slf4j
@RequiredArgsConstructor
public class OpenSearchEventCommandAdapter implements EventCommandStorePort {
    // ...
}

// ❌ @Conditional 없이 등록 시 항상 활성화되어 프로파일 전환 불가
@Component
public class OpenSearchEventCommandAdapter implements EventCommandStorePort { }
```

---

## REST API

### 엔드포인트

| 메서드 | 경로 | 설명 | 응답 코드 |
|--------|------|------|----------|
| `POST` | `/api/events` | 이벤트 저장 | `200 OK` |
| `GET` | `/api/events/{id}` | ID로 이벤트 조회 | `200 OK` / `404 Not Found` |

### 요청 / 응답 예시

**POST /api/events**
```http
POST /api/events
Content-Type: application/json

{
  "id": "evt-001",
  "message": "Suspicious process detected"
}
```
```http
HTTP/1.1 200 OK
```

**GET /api/events/{id}**
```http
GET /api/events/evt-001
```
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "evt-001",
  "message": "Suspicious process detected"
}
```

**404 응답 예시**
```http
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "success": false,
  "errorCode": "EVT-101",
  "message": "Event with id 'evt-001' not found"
}
```

---

## 빌드 및 실행

### 요구사항

- **Java 17**
- **Maven 3.6+**

### 빌드

```bash
# 전체 빌드
mvn clean install

# 테스트 스킵 후 빌드
mvn clean install -DskipTests
```

### 테스트

```bash
# 전체 테스트 실행
mvn test

# 특정 모듈 테스트
mvn test -pl sample-bootstrap

# 단일 테스트 클래스 실행
mvn test -pl sample-bootstrap -Dtest=SampleBootstrapApplicationTests

# 단일 테스트 메서드 실행
mvn test -pl sample-bootstrap -Dtest=SampleBootstrapApplicationTests#methodName
```

### 실행

```bash
cd sample-bootstrap
mvn spring-boot:run
```

기본 포트: **8080**

### 어댑터 구성 변경

`sample-bootstrap/src/main/resources/application.yml`에서 설정:

```yaml
sample:
  inbound:
    http-enabled: true
    grpc-enabled: false
  outbound: opensearch   # clickhouse 또는 opensearch
```

---

## Swagger UI

애플리케이션 실행 후 아래 URL에서 API 문서를 확인할 수 있다.

| 항목 | URL |
|------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.5.5 |
| Build | Maven |
| API 문서 | SpringDoc OpenAPI 3 (v2.8.8) |
| 로깅 | SLF4J 2.0.13 + Log4j2 2.23.1 |
| 코드 생성 | Lombok 1.18.30 |
| 테스트 | JUnit 5 + AssertJ + Mockito |
| 커버리지 | JaCoCo |
