# 헥사고날 아키텍처 프로젝트 플로우 단계별 가이드

## 📁 전체 폴더 구조

```
backend-hexagonal/
├── 📄 pom.xml                    # 루트 Maven 설정 파일
├── 📄 README.md                  # 프로젝트 설명서
├── 📄 edr-base.iml              # IntelliJ 프로젝트 설정
├── 📁 .github/                   # GitHub 설정 및 워크플로우
├── 📁 .database/                 # 데이터베이스 관련 스크립트
├── 📁 sample-bootstrap/          # 🚀 애플리케이션 부트스트랩 모듈
│   ├── 📄 pom.xml
│   └── 📁 src/main/java/com/ahnlab/edr/sample/bootstrap/
│       ├── SampleBootstrapApplication.java
│       └── 📁 common/            # 공통 설정 및 유틸리티
├── 📁 sample-config/             # ⚙️ 설정 모듈
│   ├── 📄 pom.xml
│   └── 📁 src/main/java/com/ahnlab/edr/sample/config/
│       ├── ClickHouseOutboundEnabled.java
│       ├── GrpcInboundEnabled.java
│       ├── HttpInboundEnabled.java
│       └── OpenSearchOutboundEnabled.java
├── 📁 sample-core/               # 🎯 핵심 비즈니스 로직 (도메인 계층)
│   ├── 📄 pom.xml
│   └── 📁 src/main/java/com/ahnlab/edr/sample/core/
│       ├── 📁 application/       # 애플리케이션 서비스 계층
│       │   ├── 📁 command/       # 명령(쓰기) 작업
│       │   │   ├── 📁 port/
│       │   │   │   ├── 📁 in/    # 인바운드 포트 (UseCase 인터페이스)
│       │   │   │   └── 📁 out/   # 아웃바운드 포트 (Repository 인터페이스)
│       │   │   └── 📁 service/   # 비즈니스 로직 구현
│       │   ├── 📁 query/         # 조회(읽기) 작업
│       │   │   ├── 📁 port/
│       │   │   │   ├── 📁 in/
│       │   │   │   └── 📁 out/
│       │   │   └── 📁 service/
│       │   └── 📁 exception/     # 애플리케이션 예외 처리
│       └── 📁 domain/            # 도메인 모델
│           └── 📁 vo/            # Value Objects
├── 📁 sample-in-grpc/            # 🌐 gRPC 인바운드 어댑터
│   ├── 📄 pom.xml
│   └── 📁 src/main/java/com/ahnlab/edr/sample/in/grpc/
│       ├── 📁 command/           # gRPC 명령 처리
│       ├── 📁 query/             # gRPC 조회 처리
│       └── 📁 mapper/            # 데이터 변환 매퍼
│           ├── 📁 command/
│           └── 📁 query/
├── 📁 sample-in-http/            # 🌐 HTTP REST API 인바운드 어댑터
│   ├── 📄 pom.xml
│   └── 📁 src/main/java/com/ahnlab/edr/sample/in/http/
│       ├── 📁 web/               # 컨트롤러
│       │   ├── 📁 command/       # 명령 컨트롤러
│       │   └── 📁 query/         # 조회 컨트롤러
│       ├── 📁 dto/               # 데이터 전송 객체
│       │   ├── 📁 command/
│       │   └── 📁 query/
│       └── 📁 mapper/            # 데이터 변환 매퍼
│           ├── 📁 command/
│           └── 📁 query/
├── 📁 sample-out-clickhouse/     # 🗄️ ClickHouse 아웃바운드 어댑터
│   ├── 📄 pom.xml
│   └── 📁 src/main/java/com/ahnlab/edr/sample/out/clickhouse/
│       ├── 📁 command/           # 명령 어댑터
│       ├── 📁 query/             # 조회 어댑터
│       ├── 📁 store/             # 저장소 구현 및 엔티티
│       └── 📁 mapper/            # 데이터 매핑
└── 📁 sample-out-opensearch/     # 🔍 OpenSearch 아웃바운드 어댑터
    ├── 📄 pom.xml
    └── 📁 src/main/java/com/ahnlab/edr/sample/out/opensearch/
        ├── 📁 command/           # 명령 어댑터
        ├── 📁 query/             # 조회 어댑터
        ├── 📁 store/             # 저장소 구현 및 엔티티
        └── 📁 mapper/            # 데이터 매핑
```

## 🔄 헥사고날 아키텍처 플로우 단계별 가이드

### 1단계: 요청 수신 (Inbound Adapters)
```
외부 클라이언트 → HTTP/gRPC → Inbound Adapters
```

#### HTTP 요청 플로우:
1. **HTTP 요청 수신**: `sample-in-http/web/command|query/Controller`
2. **DTO 변환**: `sample-in-http/mapper/` 를 통해 HTTP DTO → 도메인 객체 변환
3. **유효성 검증**: 입력 데이터 검증 및 예외 처리

#### gRPC 요청 플로우:
1. **gRPC 요청 수신**: `sample-in-grpc/command|query/Facade`
2. **프로토콜 변환**: gRPC 메시지 → 도메인 객체 변환
3. **비즈니스 로직 호출**: 코어 모듈의 UseCase 호출

### 2단계: 비즈니스 로직 처리 (Core Domain)
```
Inbound Adapters → Core Application Services → Domain
```

#### Command (명령) 처리:
1. **UseCase 인터페이스**: `sample-core/application/command/port/in/EventCommandUseCase`
2. **서비스 구현**: `sample-core/application/command/service/EventCommandService`
3. **도메인 로직**: `sample-core/domain/vo/EventVO` 를 통한 비즈니스 규칙 적용
4. **아웃바운드 포트 호출**: `sample-core/application/command/port/out/EventCommandStorePort`

#### Query (조회) 처리:
1. **UseCase 인터페이스**: `sample-core/application/query/port/in/EventQueryUseCase`
2. **서비스 구현**: `sample-core/application/query/service/EventQueryService`
3. **아웃바운드 포트 호출**: `sample-core/application/query/port/out/EventQueryStorePort`

### 3단계: 데이터 저장/조회 (Outbound Adapters)
```
Core Domain → Outbound Ports → External Systems (DB/Search)
```

#### ClickHouse 저장소:
1. **어댑터 구현**: `sample-out-clickhouse/command|query/ClickHouseEventAdapter`
2. **엔티티 매핑**: `sample-out-clickhouse/mapper/EventEntityMapper` 를 통한 도메인 → 엔티티 변환
3. **저장소 작업**: `sample-out-clickhouse/store/ClickHouseEventStore` 에서 실제 DB 작업

#### OpenSearch 저장소:
1. **어댑터 구현**: `sample-out-opensearch/command|query/OpenSearchEventAdapter`
2. **엔티티 매핑**: `sample-out-opensearch/mapper/EventEntityMapper` 를 통한 도메인 → 문서 변환
3. **인덱싱/검색**: `sample-out-opensearch/store/OpenSearchEventStore` 에서 검색 엔진 작업

### 4단계: 응답 반환 (Response Flow)
```
External Systems → Outbound Adapters → Core → Inbound Adapters → Client
```

1. **데이터 변환**: 엔티티 → 도메인 객체 → DTO
2. **응답 생성**: HTTP Response/gRPC Response 생성
3. **클라이언트 전송**: 최종 결과를 클라이언트에게 반환

## 🏗️ 아키텍처 원칙

### 의존성 방향
- **인바운드**: 외부 → 코어 (의존성이 안쪽으로)
- **아웃바운드**: 코어 → 외부 (포트/인터페이스를 통한 의존성 역전)

### CQRS 패턴 적용
- **Command**: 상태 변경 작업 (Create, Update, Delete)
- **Query**: 데이터 조회 작업 (Read)

### 설정 기반 활성화
- `sample-config` 모듈에서 각 어댑터의 활성화/비활성화 제어
- `@ConditionalOn` 어노테이션을 통한 조건부 빈 등록

## 🚀 애플리케이션 시작 프로세스

1. **Bootstrap 시작**: `sample-bootstrap/SampleBootstrapApplication`
2. **설정 로드**: `sample-config` 모듈의 조건부 설정 활성화
3. **컴포넌트 스캔**: 활성화된 어댑터들의 빈 등록
4. **포트 바인딩**: HTTP/gRPC 서버 시작
5. **서비스 준비**: 외부 요청 수신 대기

이 구조는 각 계층이 독립적으로 테스트 가능하며, 외부 시스템 변경에 유연하게 대응할 수 있는 헥사고날 아키텍처의 장점을 극대화합니다.