---
applyTo: "**/*.java"
---

# Java Code Style Rules

모든 Java 파일에 적용되는 코드 스타일 규칙이다.

---

## Lombok

- 로깅은 `@Slf4j` 사용 (`Logger logger = LoggerFactory.getLogger(...)` 직접 선언 금지)
- 생성자 주입은 `@RequiredArgsConstructor` + `private final` 조합 사용
- `@Autowired` 필드 주입 / 수정자 주입 금지
- getter/setter가 모두 필요한 클래스(Entity, DTO)는 `@Data` 사용
- 불변 객체가 필요하면 Java `record` 또는 `@Value` 사용
- 빌더 패턴이 필요한 경우 `@Builder` 사용

```java
// ✅
@Service
@Slf4j
@RequiredArgsConstructor
public class EventCommandService implements EventCommandUseCase {
    private final EventCommandStorePort storePort;
}

// ❌
@Autowired
private EventCommandStorePort storePort;
```

---

## Value Object (VO)

- VO는 `record`로 선언하여 불변성 보장
- compact constructor 안에서 비즈니스 검증 수행

```java
public record EventVO(String id, String message) {
    public EventVO {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Event ID cannot be null or blank");
        }
    }
}
```

---

## Entity

- Entity는 `@Data` 사용
- setter가 불필요하면 `@Getter` + `@NoArgsConstructor` + `@AllArgsConstructor` 조합 사용

---

## 예외 처리

Port 인터페이스, Outbound Adapter, Service 각각의 역할이 다르다.

| 레이어 | 규칙 |
|--------|------|
| Port 인터페이스 | `throws` 절 선언 금지 |
| Outbound Adapter | checked 예외 → `RuntimeException` 래핑 |
| Service | `RuntimeException` catch → 도메인 예외(`XxxException`)로 변환 |

```java
// Port — throws 절 없음
public interface EventCommandStorePort {
    void save(EventVO eventVO);
}

// Adapter — checked 예외 래핑
@Override
public void save(EventVO eventVO) {
    try {
        EventEntity entity = mapper.toEntity(eventVO);
        store.put(entity.getId(), entity);
    } catch (IOException e) {
        throw new RuntimeException("Failed to save event: " + eventVO.id(), e);
    }
}

// Service — 도메인 예외로 변환
@Override
public void saveEvent(EventVO eventVO) {
    try {
        storePort.save(eventVO);
    } catch (RuntimeException e) {
        log.error("Failed to save event: {}", eventVO.id(), e);
        throw new EventException(EventErrorCode.EVENT_SAVE_FAILED, eventVO.id(), e);
    }
}
```

도메인 예외와 ErrorCode는 반드시 쌍으로 정의한다.

```java
// ErrorCode enum — 도메인별 코드 체계 유지
public enum EventErrorCode implements ErrorCode {
    // Query (EVT-1XX)
    EVENT_NOT_FOUND("EVT-101", "Event with id '%s' not found", 404),
    // Command (EVT-2XX)
    EVENT_SAVE_FAILED("EVT-201", "Failed to save event", 500),
    // Validation (EVT-3XX)
    EVENT_ID_REQUIRED("EVT-301", "Event ID is required", 400);
}
```

---

## Optional 및 컬렉션

- 메서드에서 `null` 반환 금지 → `Optional<T>` 또는 빈 컬렉션(`List.of()`) 반환
- `Optional.get()` 직접 호출 금지 → `orElse`, `orElseThrow`, `map` 활용

```java
// ✅
return store.findById(id)
    .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND, id));

// ❌
return store.findById(id).get();
```

---

## Spring 어노테이션

- 목적에 맞는 스테레오타입 사용: `@Service`, `@Repository`, `@RestController`, `@Component`
- `@Transactional`은 Service 계층에서만, 경계를 최소화하여 선언
- Controller 메서드 파라미터에 `@Validated` / `@Valid` 적용

---

## 임포트

- 와일드카드 임포트(`import com.example.*`) 금지
- 사용하지 않는 임포트 제거
- static 임포트는 테스트 코드의 `Assertions.*`, `Mockito.*`에 한해 허용

---

## 테스트

- 테스트 프레임워크: **JUnit 5 + AssertJ + Mockito**
- 통합 테스트: `@SpringBootTest`
- 단위 테스트에서 `@ExtendWith(MockitoExtension.class)` 사용, Mock 주입은 생성자 방식 선호
- 테스트 메서드명: `메서드명_상황_기대결과` (예: `saveEvent_whenIdIsNull_throwsException`)
- `@DisplayName`으로 한국어 설명 작성 가능

```java
@ExtendWith(MockitoExtension.class)
class EventCommandServiceTest {

    @Mock
    private EventCommandStorePort storePort;

    private EventCommandService service;

    @BeforeEach
    void setUp() {
        service = new EventCommandService(storePort);
    }

    @Test
    @DisplayName("이벤트 저장 성공")
    void saveEvent_whenValid_savesToStore() {
        EventVO vo = new EventVO("id-1", "message");
        service.saveEvent(vo);
        verify(storePort).save(vo);
    }
}
```

---

## 기타

- 의미 없는 주석, 광고성 주석, `// TODO` 방치 금지
- 상수는 `private static final`로 선언, `UPPER_SNAKE_CASE` 명명
- Javadoc은 `public` 메서드에 작성 (파라미터, 반환값, 예외 포함)
