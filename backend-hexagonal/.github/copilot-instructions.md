# 🧭 GitHub Copilot Instructions for EDR Backend Project

## 📌 목적

이 문서는 GitHub Copilot을 활용한 EDR 백엔드 개발 프로젝트를 일관된 품질과 생산성을 유지하기 위한 지침서입니다. 모든 팀원은 이 문서를 기반으로 Copilot을 활용하며, AI 보조 개발의
guardrail(가이드라인)을 준수합니다.
 
---

## 🏗️ 프로젝트 개요

EDR(Endpoint Detection and Response) 솔루션은 다양한 보안 이벤트를 수집, 분석, 대응하는 기능을 제공하기 위한 프로젝트입니다.
Front End와 Back End가 분리된 구조이며, 본 프로젝트는 Back End 역할입니다.
 
---

## 🔄 개발 흐름

Copilot은 아래 순서에 따라 작업을 수행합니다. 각 단계에서는 AI Mode를 선택하여 최적의 프롬프트 지시가 이행될 수 있도록 합니다.

1. **요구사항 정의** → `.github/chat.mode/requirement.chatmode.md`
2. **검토** → `.github/chat.mode/mentoring.chatmode.md`
3. **설계** → `.github/chat.mode/plan.chatmode.md`
4. **구현** → `.github/chat.mode/implement.chatmode.md`
5. **정리** → `.github/chat.mode/cleanup.chatmode.md`
6. **테스트 검증** → `.github/chat.mode/test-verify-java.chatmode.md`

코딩 단계에서는 `.github/instructions/reference/`하위의 지침들을 참고 합니다.
 
---

## 🤖 Copilot 사용 지침

### 명확한 프롬프트 작성

- 명확한 프롬프트 요청이 아닐 경우는사용자 확인을 거쳐 명확하게 처리될 수 있도록 합니다.

### 코드 품질

- Java Code Convention 준수
- @Slf4j, @RequiredArgsConstructor 등 Lombok 적극 활용

### 테스트 코드 생성

- 단위 테스트: JUnit5 + Mockito
- 통합 테스트: @SpringBootTest 활용
- 테스트 커버리지 도구: JaCoCo

### 보안 및 품질

- 민감 정보 포함 금지 (API 키, 비밀번호 등)
- 생성된 코드 반드시 리뷰 후 병합
- PR에 Copilot 사용 여부 명시

---

## 📘문서와 코드의 주석

모든 문서는 **텍스트 기반(Markdown)**으로 작성