# 샘플 API Instructions

## 배경
- 2개의 팀이 각각 2개의 프로젝트를 진행 합니다. 
- 각 프로젝트에는 일부 같은 기능을 제공해야 합니다.
- 같은 기능을 제공하지만 요청 인터페이스는 각각 http와 gRPC를 제공합니다.
- 같은 기능에 대해서 저장소는 각각 OpenSearch와 ClickHouse를 사용합니다.

## 목표
- JAVA 프로젝트 모듈을 핵사고날 아키텍처로 설계합니다.
- 핵사고날 아키텍처에서 비즈니스 캡슐화는 Core 모듈에 정의합니다.
- 핵사고날 아키텍처에서 port in 을 처리하는 Adaptor는 In 모듈에 정의합니다.
- 핵사고날 아키텍처에서 port out 을 처리하는 Adaptor는 Out 모듈에 정의합니다.

## 구현
### JAVA 프로젝트 모듈 구성

#### sample-core 모듈
- 도메인에 대한 비지니스 로직을 캡슐화한 모듈입니다.
- Adaptor Out 모듈의 Interface를 선언합니다.
- Out Interface의 패키지 내에는 CQRS 구분을 위해 commnad, query 패키지로 구성합니다.
- 비즈니스 로직의 파라메터는 VO 형태로 정의합니다.
- Core 에서 비지니스 결과에 대한 DB처리는 관련 port out에 정의된 Interface를 호출합니다.

##### JAVA 패키지 구조 예시
sample.core/
  ├─ domain/
  │     ├─ entity/
  │     └─ port/
  │           └─ out
  │               ├─ command
  │               │    └─ CommandInterface.java
  │               └─ query
  │                    └─ QueryInterface.java
  └─ application/
        ├─ service/
        │     ├─ model/
        │     │    └─ SampleVO.java
        │     ├─ command
        │     │    └─ CommandImpl.java
        │     └─ query
        │          └─ QueryImpl.java
        ├─ mapper/
        │     └─ VoToEntityMapper.java
        └─ exception
              └─ SampleException.java

#### sample-in-http 모듈
- sample-core 모듈을 참조합니다.
- 핵사고날 아키텍처의 port in 을 처리하는 Adaptor 를 구현합니다. 
- HTTP Request 요청을 처리하는 RestController 를 구현합니다.
- Request 파라메터는 DTO로 정의합니다.
- DTO는 Mapper에 의해 VO로 변환되어 Core모듈을 호출합니다.

##### JAVA 패키지 구조 예시
sample.in.http/
  ├─ controller/
  │      ├─ command
  │      │     └─ CommandHttpController.java
  │      └─ query
  │            └─ QueryHttpController.java
  ├─ dto/
  │     ├─ command
  │     └─ query
  └─ mapper/
        ├─ command
        │      ├─ CommandHttpRquestMapper.java
        │      └─ CommandHttpResponseMapper.java
        └─ query
               └─ QueryHttpMapper.java 

#### sample-out-opensearch 모듈
- sample-core 모듈을 참조합니다.
- 핵사고날 아키텍처의 port out 을 처리하는 Adaptor 를 구현합니다. 
- Core모듈의 인터페이스를 구현 하여 Entity를 OpenSearch 에 요청하여 저장 또는 조회 기능을 제공합니다.

##### JAVA 패키지 구조 예시
sample.out/
  └─ opensearch/
        ├─ command
        │      └─ OpenSearchCommandRepository.java
        └─ query
               └─ OpenSearchQueryRepository.java

#### sample-bootstrap 모듈
- 모든 모듈을 참조하여 spring boot 서비스를 제공합니다.
- system 설정 값에 따라 in 과 out Adaptor를 선택적으로 사용합니다.

##### JAVA 패키지 구조 예시
sample.bootstrap/