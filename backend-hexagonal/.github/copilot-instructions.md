# GitHub Copilot Instructions — EDR Backend (Hexagonal Architecture)

## 프로젝트 개요

AhnLab EDR(Endpoint Detection and Response) 솔루션의 백엔드.  
**헥사고날 아키텍처(Ports and Adapters)** + **CQRS** 패턴을 엄격하게 준수한다.  
빌드 시스템: **Maven** (Java 17, Spring Boot 3.5.5)

---

## 빌드 / 테스트 명령

```bash
# 전체 빌드 (루트에서)
mvn clean install

# 테스트만 실행
mvn test

# 특정 모듈 테스트
mvn test -pl sample-bootstrap

# 단일 테스트 클래스 실행
mvn test -pl sample-bootstrap -Dtest=SampleBootstrapApplicationTests

# 단일 테스트 메서드 실행
mvn test -pl sample-bootstrap -Dtest=SampleBootstrapApplicationTests#methodName

# 테스트 스킵 후 빌드
mvn clean install -DskipTests
```

테스트: **JUnit 5 + AssertJ + Mockito**  
통합 테스트: `@SpringBootTest`  
커버리지: JaCoCo

---

## 아키텍처 개요

```
Inbound Adapters (sample-in-http, sample-in-grpc)
        │
        ▼  UseCase Port (interface) — VO 전달
Application Core (sample-core)  ←── Domain: VO, Service, Exception
        │
        ▼  StorePort (interface) — VO 전달
Outbound Adapters (sample-out-opensearch, sample-out-clickhouse)
  └── Adapter 내부에서 VO → Entity 변환 후 저장

sample-config   : @Conditional 어노테이션 정의 (어댑터 ON/OFF 제어)
sample-bootstrap: Spring Boot 진입점 (모든 모듈을 조합)
```

**의존성 방향**: Adapter → Core (Core는 절대 Adapter를 의존하지 않음)

**핵심 변경**: VO → Entity 변환 책임은 `sample-core`가 아닌 **Outbound Adapter**에 있다.  
 `StorePort` 메서드는 VO를 받고, 각 Adapter가 내부적으로 Entity로 변환한 뒤 저장한다.

---

## 모듈 구조

| 모듈 | 역할 |
|------|------|
| `sample-core` | 비즈니스 로직. domain(VO), application(UseCase/StorePort/Service/Exception) |
| `sample-in-http` | HTTP REST Inbound Adapter — Request→VO Mapper 포함 |
| `sample-in-grpc` | gRPC Inbound Adapter — gRPC Message→VO Mapper 포함 |
| `sample-out-opensearch` | OpenSearch Outbound Adapter — **VO→Entity Mapper 포함** |
| `sample-out-clickhouse` | ClickHouse Outbound Adapter — **VO→Entity Mapper 포함** |
| `sample-config` | `@Conditional` 어노테이션 모음 |
| `sample-bootstrap` | 실행 진입점 + Common (ApiResponse, GlobalExceptionHandler, OpenApiConfig) |

---

## 전체 아키텍처 레이어

```
┌─────────────────────────────────────────────────────────────────┐
│                   Inbound Adapters                              │
│      sample-in-http (REST)    sample-in-grpc (gRPC)             │
│   ├─ Controller/Facade        ├─ Facade                         │
│   ├─ DTO (Request/Response)   ├─ Mapper                         │
│   └─ Mapper                   └─ gRPC Service                   │
└──────────────────────┬──────────────────────────────────────────┘
                       │ UseCase Port (in-port) — VO 전달
┌──────────────────────▼──────────────────────────────────────────┐
│                 Application Core                                │
│                   sample-core                                   │
│   ├─ domain/: VO (record)                                       │
│   ├─ application/command: Service, UseCase, StorePort           │
│   ├─ application/query: Service, UseCase, StorePort             │
│   └─ exception: ApplicationException, ErrorCode, DomainException│
└──────────────────────┬──────────────────────────────────────────┘
                       │ StorePort (out-port) — VO 전달
┌──────────────────────▼──────────────────────────────────────────┐
│                  Outbound Adapters                              │
│    sample-out-opensearch    sample-out-clickhouse               │
│   ├─ Adapter                ├─ Adapter                          │
│   ├─ Store                  ├─ Store                            │
│   └─ VO→Entity Mapper       └─ VO→Entity Mapper                 │
└─────────────────────────────────────────────────────────────────┘

sample-config: @Conditional 어노테이션으로 어댑터 런타임 ON/OFF

sample-bootstrap (Common)
├─ GlobalExceptionHandler: Exception → ApiResponse
├─ ApiResponse<T>: 표준 REST 응답 래퍼
└─ OpenApiConfig: Swagger UI 제공
```

---

## CQRS 패턴 — 반드시 준수

쓰기(Command)와 읽기(Query)는 모든 레이어에서 완전 분리한다.

| 레이어 | Command | Query |
|--------|---------|-------|
| UseCase (in-port) | `{Domain}CommandUseCase` | `{Domain}QueryUseCase` |
| Service | `{Domain}CommandService` | `{Domain}QueryService` |
| StorePort (out-port) | `{Domain}CommandStorePort` | `{Domain}QueryStorePort` |
| Adapter | `{Tech}{Domain}CommandAdapter` | `{Tech}{Domain}QueryAdapter` |
| DTO | `dto/command/{Domain}Request` | `dto/query/{Domain}Response` |
| Mapper | `mapper/command/{Domain}CommandMapper` | `mapper/query/{Domain}QueryMapper` |
| Controller | `web/command/{Domain}CommandController` | `web/query/{Domain}QueryController` |

---

## 명명 규칙

```
Port (인터페이스)
  - Inbound : EventCommandUseCase, EventQueryUseCase
  - Outbound: EventCommandStorePort, EventQueryStorePort

구현체
  - Service : EventCommandService, EventQueryService
  - Adapter : OpenSearchEventCommandAdapter, ClickHouseEventQueryAdapter
  - Store   : OpenSearchEventStore, ClickHouseEventStore

예외
  - Base    : ApplicationException
  - 도메인별 : EventException, UserException
  - ErrorCode: EventErrorCode (enum, EVT-1XX/2XX/3XX), UserErrorCode (USR-XXX)
```

---

## 의존성 규칙 (필수)

```java
// ✅ Core는 Port(인터페이스)만 의존
@Service
public class EventCommandService implements EventCommandUseCase {
    private final EventCommandStorePort storePort; // ✅ 인터페이스
}

// ❌ 절대 금지 — Core가 기술 스택(HTTP, gRPC, DB)을 직접 import
import org.springframework.web.bind.annotation.RestController; // ❌
import com.ahnlab.edr.sample.out.opensearch.OpenSearchEventCommandAdapter; // ❌
```

| 모듈 | 의존 가능 | 의존 금지 |
|------|----------|----------|
| `sample-core` | VO, Entity, Port, Exception | HTTP/gRPC/DB 라이브러리, Adapter |
| `sample-in-*` | Core (Port, VO) | Outbound Adapter 직접 참조 |
| `sample-out-*` | Core (Port, **VO**, Entity) | Inbound Adapter, Controller |

> `sample-out-*`는 Core의 VO를 받아 자체적으로 Entity로 변환한다.  
> `sample-core`에 VO→Entity Mapper를 두지 않는다.

---

## 예외 처리 패턴

- **Port 인터페이스**: `throws` 절 없음 (깔끔한 계약), **VO를 파라미터로 받음**
- **Outbound Adapter**: VO → Entity 변환 후 저장, `IOException` 등 checked 예외 → `RuntimeException`으로 래핑
- **Service**: `RuntimeException` catch → 도메인 예외(`EventException` 등)로 변환

```java
// StorePort — VO를 파라미터로 받음 (Entity 변환은 Adapter 책임)
public interface EventCommandStorePort {
    void save(EventVO eventVO);  // ✅ VO, throws 절 없음
}

public interface EventQueryStorePort {
    Optional<EventVO> findById(String id);  // ✅ VO 반환
}

// Adapter — VO→Entity 변환 후 저장
@Component
@OpenSearchOutboundEnabled
public class OpenSearchEventCommandAdapter implements EventCommandStorePort {

    private final OpenSearchEventStore store;
    private final EventEntityMapper mapper; // VO→Entity 변환 담당 (Adapter 내부)

    @Override
    public void save(EventVO eventVO) {  // throws 절 없음
        try {
            EventEntity entity = mapper.toEntity(eventVO); // ✅ Adapter에서 변환
            store.put(entity.getId(), entity);
        } catch (RuntimeException e) {
            log.error("Failed to save event: {}", eventVO.id(), e);
            throw new RuntimeException("Failed to save event: " + eventVO.id(), e);
        }
    }
}

// Service — VO를 그대로 StorePort에 전달 (변환 책임 없음)
@Service
public class EventCommandService implements EventCommandUseCase {
    private final EventCommandStorePort storePort;

    @Override
    public void saveEvent(EventVO eventVO) {
        try {
            storePort.save(eventVO);  // ✅ VO 전달, Entity 변환 코드 없음
        } catch (RuntimeException e) {
            log.error("Failed to save event: {}", eventVO.id(), e);
            throw new EventException(EventErrorCode.EVENT_SAVE_FAILED, eventVO.id(), e);
        }
    }
}
```

---

## 어댑터 ON/OFF (Conditional 설정)

`sample-config`의 `@Conditional` 어노테이션으로 어댑터를 런타임에 선택한다.

```yaml
# application.yml
sample:
  inbound.http-enabled: true    # HTTP Inbound 활성화
  inbound.grpc-enabled: false   # gRPC Inbound 비활성화
  outbound: clickhouse          # opensearch | clickhouse
```

각 Adapter 빈에 `@HttpInboundEnabled`, `@GrpcInboundEnabled`, `@ClickHouseOutboundEnabled`, `@OpenSearchOutboundEnabled` 중 하나를 붙인다.

---

## Common 모듈 (sample-bootstrap)

`sample-bootstrap/common` 패키지는 전체 애플리케이션을 연결하는 공통 인프라를 제공한다.

### 핵심 컴포넌트

| 클래스 | 역할 |
|--------|------|
| `ApiResponse<T>` | 모든 REST API 응답을 통일하는 래퍼 (성공/실패 통합) |
| `GlobalExceptionHandler` | `ApplicationException` → HTTP 상태코드 + `ApiResponse` 변환 |
| `OpenApiConfig` | Swagger UI 제공, OpenAPI 3.0 문서 자동 생성 |

### 예외 처리 흐름

```
Service (RuntimeException 발생)
  ↓ throws EventException
Controller
  ↓ (예외 전파)
GlobalExceptionHandler
  ↓ @ExceptionHandler(ApplicationException.class)
ApiResponse.error(code, message)
  ↓
HTTP 상태코드 + JSON 응답
```
---

## Lombok 사용

`@Slf4j`, `@RequiredArgsConstructor`, `@Data`, `@Builder` 등 Lombok을 적극 활용한다.  
생성자 주입은 `@RequiredArgsConstructor` + `private final` 필드 조합을 사용한다.

---

## 데이터베이스

PostgreSQL 스키마는 `.database/postgres/` 에서 관리한다.  
주요 테이블: `tb_edr_custom_rule`, `tb_edr_custom_rule_apply`  
Enum 타입: `enum_platform_type`, `enum_severity`, `enum_rule_status`, `enum_apply_status`
