# 📁 Project Structure

---

## 📐 지침 문서
Copilot은 아래 지침서를 기반으로 코드를 생성합니다. 모든 지침서는 .github 폴더 하위에 위치합니다.

---

### .github 하위 구조
.github/
└── copilot-instructions.md
└── chat.mode/
└── instructions/
       └── references/
       └── works/
       └── improvements/

#### copilot-instructions.md
- 프로젝트 전체에 영향을 미치는 공통된 지침서

#### chat.mode
- AI 프로프트 명령을 사용자가

#### instructions
- 바이브 코딩을 하기 위해 참고할 수 있는 지침서입니다.

---

## 📘.document 하위 구조
Copilot은 개발 과정에서 나오는 피드백을 단계별로 정리해서, JAVA 프로젝트의 각 모듈에 맞춰 **텍스트 기반(Markdown)**으로 기록합니다.

.document/
└── historys/ 
└── works/
       └── edr-common-core/
       └── edr-common-config/
       └── edr-common-constant/
       └── edr-common-util/
       └── edr-resource-lettuce/
       └── edr-resource-opensearch/
       └── edr-resource-postgres/
       └── edr-resource-producer/
       └── edr-interface-router/
       └── edr-module-api/
       └── edr-module-authentication/
       └── edr-module-cache/
       └── edr-module-dsl/
       └── edr-module-file-manager/
       └── edr-module-mdp/
       └── edr-module-rakeen/
       └── edr-module-terminal-access/
       └── edr-service-common/
       └── edr-service-agent/
       └── edr-service-console/
       └── edr-service-analyzer/
       └── edr-service-consumer/
       └── edr-service-batch/
       └── edr-service-scheduler/
       └── edr-service-router/
       └── edr-service-manager/
       └── edr-tools/
       └── edr-tw-file-convert/
       └── edr-provisioner/

### historys
- JAVA 프로젝트를 빌드할 때, 각 모듈에서 어떤 주요 변경이 있었는지 요약해서 기록합니다.
- 빌드 버전은 네 자리 숫자(예: 1.0.3.15)로 관리되며, 마지막 숫자(build 번호)가 바뀔 때마다 변경 내용을 기록합니다.
- patch 버전(예: 1.0.3)이 바뀔 때마다 새로운 파일을 만들어서, 그 안에 변경 이력을 정리해 둡니다.

### works
- JAVA 프로젝트는 여러 모듈로 나뉘어 있는데, 각 모듈에서 어떤 변경이 있었는지 간단하게 정리해서 기록합니다.
- 기능을 개발할 때는 도메인(Domain) 단위로 설계부터 구현까지의 과정을 순서대로 기록합니다. (예: 설계 → 작업 분해 → 구현 → 정리)
- 변경 내용은 다음과 같은 항목으로 간결하게 정리합니다:
 ├-- 날짜: 언제 작업했는지
 ├-- 개발자: 누가 작업했는지
 ├-- 변경 내용: 어떤 내용이 바뀌었는지
 └-- 주의 사항: 개발하거나 테스트할 때 주의해야 할 점 및 구현 내용에서 주의 깊게 확인 되어야 할 내용

---

## 🧱 Database 스키마 문서
Copilot은 아래 Database 스키마 문서를 참조하여 코드를 생성합니다. 모든 지침서는 . database 폴더 하위에 위치합니다.

### .database 하위 구조
.database/
└── postgres/
         └── initialize/   
         └── enums/  
         └── tables/  
         └── procedures/  
└── opensearch/
       └── mappers/

#### .database/postgres
- Prostgres DB의 DDL(Data Definition Language) 데이터베이스 객체(테이블, 인덱스, enum 등)를 생성, 변경하는 명령문 파일들 포함하고 있습니다.
- 하위 initialize 퐅더에는 최초 EDR 서비스 구축 시 초기화에 필요한 명령문 또는 초기값 데이터 저장 명령문을 포함하고 있습니다.
- 하위 enums 폴더에는 하나의 파일에 모든 enum정의 하고 있습니다.(enum은 여러 테이블에서 공통으로 사용할 수 있기 때문입니다.)
- 하위 tables 폴더에는 테이블과 인덱스 생성, 변경 명령문은 각각의 테이블 단위로 존재합니다.
- 하위 procedures 폴더에는 여러 store procudure 생성, 변경 명령문을 포함하고 있습니다.

#### .database/opensearch
- OpenSearch의 INDEX를 생성할 때, mapper정보를 template파일로 정의하고 있습니다.

---

## 🗃️ JAVA 프로젝트 모듈 구조
- edr-common-core: 핵심 기능
- edr-common-config: 설정 관련 기능
- edr-common-constant: 상수정의 기능(문자, 숫자, enum형 상수로 함수를 포함하지 않습니다.)
- edr-common-util: 유틸리티 클래스 모음(final 클래스로 static함수만 제공합니다.)
- edr-resource-lettuce: Redis클라이언트(Lettuce) 연결 기능
- edr-resource-opensearch: OpenSearch 연결 기능
- edr-resource-postgres: Postgres 연결 기능
- edr-resource-producer: Kafka Producer 연결 기능
- edr-interface-router: API Gateway 클라이언트 기능(내부 서비스 라이팅 처리)
- edr-module-api: 외부 API 제공 기능(EDR 로그는 Kafka producer를 통해 Kafka에 전달됩니다.)
- edr-module-authentication: 인증/인가 처리 기능
- edr-module-cache: 로컬 메모리에 캐시된 정보를 제공하는 기능
- edr-module-dsl: 조회 DSL 질의를 OpenSearch QueryDSL로 변황하여 제공하는 기능
- edr-module-file-manager: 파일 업/다운로드 기능
- edr-module-mdp: 탐지 DSL을 End Point의 Agent의 엔진에서 인식할 수 있도록 변환하는 기능
- edr-module-rakeen: INTEL, INSPECT와 같이 Rakeen에 종속적인 기능
- edr-module-terminal-access: End Point의 Agent와 WebSocket으로 연결하여 제어 명령을 처리
- edr-service-common: HTTP 서비스를 제공하는 모듈들이 공통으로 사용되는 기능
- edr-service-agent: End Poing Agent에서의 요청을 처리
- edr-service-console: EDR 정보를 제공하는 관리자 화면 기능
- edr-service-analyzer: EDR 로그를 분석하여 위협 다이어그램을 분석하는 기능
- edr-service-consumer: Kafka에서 로그룰 수집하여 OpenSearch에 저장하는 기능
- edr-service-batch: 배치 작업 처리
- edr-service-scheduler: 작업 스캐줄링
- edr-service-router: 서비스간 라우팅
- edr-service-manager: 서비스 관리 및 제어
- edr-tools: 유지보수 지원을 위해 유용한 도구를 제공
- edr-tw-file-convert: TW 다국어 처리
- edr-provisioner: Open Source 및 Application 서비스 설치 및 업그레이드