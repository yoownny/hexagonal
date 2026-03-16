---
name: swagger
description: >
  Spring Boot 3.x 헥사고날 아키텍처 프로젝트에 Swagger(OpenAPI 3) 문서를 작성하는 가이드.
  "swagger 작성", "API 문서화", "OpenAPI 추가", "swagger 설정", "@Operation 추가",
  "@Schema 작성", "API 스펙 문서" 요청 시 사용.
---

# SKILL: Swagger(OpenAPI 3) 문서 작성 가이드

## 목적

Spring Boot 3.x + `springdoc-openapi` 를 사용하여 헥사고날 아키텍처 프로젝트에  
OpenAPI 3 문서를 작성한다.  
**Inbound Adapter(`sample-in-http`)** 레이어에만 Swagger 어노테이션을 추가한다.  
Core, Outbound Adapter에는 Swagger 어노테이션을 추가하지 않는다.

---

## 1. 의존성 추가 — `{domain}-in-http/pom.xml`

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.8</version>
</dependency>
```

> Spring Boot 3.x 기준 `springdoc-openapi-starter-webmvc-ui` 2.x 버전을 사용한다.  
> `springfox` (2.x 시절 라이브러리) 사용 금지 — Spring Boot 3.x 미지원.

---

## 2. `OpenApiConfig` — `{domain}-bootstrap`

Bootstrap 모듈에 OpenAPI 전역 설정 클래스를 배치한다.

```java
package com.ahnlab.edr.{domain}.bootstrap.common;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 전역 설정.
 * Swagger UI: /swagger-ui.html
 * API Docs:   /v3/api-docs
 */
@OpenAPIDefinition(
        info = @Info(
                title = "{Domain} API",
                version = "v1.0",
                description = "{Domain} 서비스 REST API 명세서",
                contact = @Contact(
                        name = "AhnLab EDR Team",
                        email = "edr@ahnlab.com"
                ),
                license = @License(
                        name = "AhnLab Proprietary",
                        url = "https://www.ahnlab.com"
                )
        ),
        servers = {
                @Server(url = "/", description = "기본 서버")
        }
)
@Configuration
public class OpenApiConfig {
}
```

---

## 3. `application.yml` 설정

```yaml
springdoc:
  swagger-ui:
    path: /swagger-ui.html          # Swagger UI 접근 경로
    tags-sorter: alpha               # 태그 알파벳순 정렬
    operations-sorter: alpha         # 오퍼레이션 알파벳순 정렬
    display-request-duration: true   # 요청 처리 시간 표시
  api-docs:
    path: /v3/api-docs              # OpenAPI JSON 경로
  default-produces-media-type: application/json
  default-consumes-media-type: application/json
```

---

## 4. Controller 어노테이션

### 4.1 `@Tag` — 컨트롤러 그룹핑

```java
@Tag(name = "Event Command", description = "이벤트 생성·수정·삭제 API")
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventCommandController { ... }

@Tag(name = "Event Query", description = "이벤트 조회 API")
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventQueryController { ... }
```

> CQRS 원칙에 따라 Command Controller와 Query Controller의 `@Tag`를 분리한다.

### 4.2 `@Operation` — 엔드포인트 설명

```java
@Operation(
    summary = "이벤트 저장",
    description = "새로운 이벤트를 저장합니다. 동일 ID가 존재하면 덮어씁니다."
)
@PostMapping
public ResponseEntity<ApiResponse<Void>> save(@RequestBody @Valid EventRequest request) {
    eventCommandUseCase.saveEvent(eventCommandMapper.toVO(request));
    return ResponseEntity.ok(ApiResponse.ok());
}
```

### 4.3 `@ApiResponses` / `@ApiResponse` — 응답 코드 정의

```java
@Operation(summary = "이벤트 단건 조회", description = "ID로 이벤트를 조회합니다.")
@ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "조회 성공",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = EventResponse.class)
        )
    ),
    @ApiResponse(
        responseCode = "404",
        description = "이벤트를 찾을 수 없음",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ApiResponse.class)
        )
    )
})
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<EventResponse>> get(@PathVariable String id) { ... }
```

---

## 5. DTO 어노테이션 — `@Schema`

### 5.1 Request DTO (`dto/command/`)

```java
package com.ahnlab.edr.{domain}.in.http.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 이벤트 저장 요청 DTO.
 */
@Schema(description = "이벤트 저장 요청")
@Data
public class EventRequest {

    @Schema(description = "이벤트 고유 식별자", example = "EVT-20240101-001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이벤트 ID는 필수입니다.")
    private String id;

    @Schema(description = "이벤트 메시지", example = "파일 변경 감지됨", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "메시지는 필수입니다.")
    private String message;
}
```

### 5.2 Response DTO (`dto/query/`)

```java
package com.ahnlab.edr.{domain}.in.http.dto.query;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 이벤트 조회 응답 DTO.
 */
@Schema(description = "이벤트 조회 응답")
@Data
public class EventResponse {

    @Schema(description = "이벤트 고유 식별자", example = "EVT-20240101-001")
    private String id;

    @Schema(description = "이벤트 메시지", example = "파일 변경 감지됨")
    private String message;
}
```

### 5.3 `ApiResponse` 래퍼에 `@Schema` 추가

```java
@Schema(description = "API 공통 응답 래퍼")
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    @Schema(description = "요청 성공 여부", example = "true")
    private final boolean success;

    @Schema(description = "응답 데이터 (성공 시에만 존재)")
    private final T data;

    @Schema(description = "에러 코드 (실패 시에만 존재)", example = "EVT-101")
    private final String errorCode;

    @Schema(description = "에러 메시지 (실패 시에만 존재)", example = "Event with id 'X' not found")
    private final String message;
}
```

---

## 6. 파라미터 어노테이션 — `@Parameter`

### 6.1 Path Variable

```java
@Operation(summary = "이벤트 단건 조회")
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<EventResponse>> get(
        @Parameter(description = "이벤트 ID", example = "EVT-20240101-001", required = true)
        @PathVariable String id) {
    ...
}
```

### 6.2 Query Parameter

```java
@Operation(summary = "이벤트 목록 조회")
@GetMapping
public ResponseEntity<ApiResponse<List<EventResponse>>> list(
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") int page,

        @Parameter(description = "페이지 크기", example = "20")
        @RequestParam(defaultValue = "20") int size) {
    ...
}
```

---

## 7. 전체 예시 — Command Controller

```java
package com.ahnlab.edr.{domain}.in.http.web.command;

import com.ahnlab.edr.{domain}.bootstrap.common.ApiResponse;
import com.ahnlab.edr.{domain}.core.application.command.port.in.EventCommandUseCase;
import com.ahnlab.edr.{domain}.in.http.dto.command.EventRequest;
import com.ahnlab.edr.{domain}.in.http.mapper.command.EventCommandMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Event Command", description = "이벤트 생성·수정·삭제 API")
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventCommandController {

    private final EventCommandUseCase eventCommandUseCase;
    private final EventCommandMapper eventCommandMapper;

    @Operation(
            summary = "이벤트 저장",
            description = "새로운 이벤트를 저장합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "저장 성공",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 파라미터 오류",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 오류",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> save(@RequestBody @Valid EventRequest request) {
        eventCommandUseCase.saveEvent(eventCommandMapper.toVO(request));
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
```

---

## 8. 전체 예시 — Query Controller

```java
package com.ahnlab.edr.{domain}.in.http.web.query;

import com.ahnlab.edr.{domain}.bootstrap.common.ApiResponse;
import com.ahnlab.edr.{domain}.core.application.query.port.in.EventQueryUseCase;
import com.ahnlab.edr.{domain}.in.http.dto.query.EventResponse;
import com.ahnlab.edr.{domain}.in.http.mapper.query.EventQueryMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Event Query", description = "이벤트 조회 API")
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventQueryController {

    private final EventQueryUseCase eventQueryUseCase;
    private final EventQueryMapper eventQueryMapper;

    @Operation(
            summary = "이벤트 단건 조회",
            description = "ID로 이벤트를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = EventResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "이벤트를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ApiResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> get(
            @Parameter(description = "이벤트 ID", example = "EVT-20240101-001", required = true)
            @PathVariable String id) {
        return eventQueryUseCase.getEvent(id)
                .map(eventQueryMapper::toResponse)
                .map(com.ahnlab.edr.{domain}.bootstrap.common.ApiResponse::ok)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
```

---

## 9. `@Hidden` — 특정 API 문서화 제외

내부 전용 또는 미완성 API를 Swagger 문서에서 숨길 때 사용한다.

```java
import io.swagger.v3.oas.annotations.Hidden;

// 특정 메서드 숨기기
@Hidden
@PostMapping("/internal/debug")
public ResponseEntity<Void> debug() { ... }

// 컨트롤러 전체 숨기기
@Hidden
@RestController
@RequestMapping("/internal")
public class InternalController { ... }
```

---

## 10. 어노테이션 적용 위치 규칙

| 어노테이션 | 적용 위치 | 적용 금지 위치 |
|-----------|-----------|----------------|
| `@Tag` | Controller 클래스 | Core, Outbound Adapter |
| `@Operation` | Controller 메서드 | Core, Outbound Adapter |
| `@ApiResponses` / `@ApiResponse` | Controller 메서드 | Core, Outbound Adapter |
| `@Parameter` | Controller 메서드 파라미터 | Core VO, Outbound Adapter |
| `@Schema` | Request/Response DTO 클래스 및 필드 | Core VO (record), Entity |
| `@Hidden` | 숨길 Controller 또는 메서드 | — |

> ❌ Core의 VO (`record`)에 `@Schema` 추가 금지 — Core는 외부 기술 의존 금지  
> ✅ Inbound Adapter의 DTO 클래스에만 `@Schema` 추가

---

## 11. 체크리스트

- [ ] `{domain}-in-http/pom.xml`에 `springdoc-openapi-starter-webmvc-ui` 의존성 추가
- [ ] `{domain}-bootstrap`에 `OpenApiConfig` 생성 (`@OpenAPIDefinition`, `@Info`)
- [ ] `application.yml`에 `springdoc.swagger-ui.path`, `springdoc.api-docs.path` 추가
- [ ] Command Controller에 `@Tag(name = "... Command")` 적용
- [ ] Query Controller에 `@Tag(name = "... Query")` 적용
- [ ] 각 메서드에 `@Operation(summary, description)` 작성
- [ ] 각 메서드에 `@ApiResponses` — 200/400/404/500 응답 코드 정의
- [ ] Request DTO 필드에 `@Schema(description, example, requiredMode)` 작성
- [ ] Response DTO 필드에 `@Schema(description, example)` 작성
- [ ] Path Variable / Query Param에 `@Parameter(description, example, required)` 작성
- [ ] Core VO에 `@Schema` 없음 확인 (Core는 외부 기술 의존 금지)
- [ ] `/swagger-ui.html` 접속하여 문서 정상 렌더링 확인
