# 🧾 Product Overview: EDR Analyzer System

 
---

## 🎯 목적

엔드포인트에서 발생하는 보안 이벤트를 수집, 분석, 대응하는 백엔드 시스템을 구축합니다.
 
---

## 🔍 주요 기능

### edr-agent 서비스

- 행위로그 수집
- 운영체제 로그 수집
- Artifact 로그 수집
- Antivirus 탐지 정보 수집
- 대응 결과 수집

### edr-consumer 서비스

- 일반 행위 로그 저장
- 주요 행위 저장
- 위협 탐지 저장
- 운영체제 로그 저장
- Artifact 로그 저장
- Antivirus 탐지 정보 저장
- 대응 결과 저장

### edr-anlayzer 서비스

- 일반 행위 다이어그램 분석
- 주요 행위 다이어그램 분석
- 위협 탐지 다이어그램 분석
- Antivirus 다이어그램 분석

### edr-batch 서비스

- 배치 작업

### edr-scheduler 서비스

- 작업 스캐줄링

### edr-router 서비스

- 서비스간 라우팅

### edr-manager 서비스

- 서비스 관리 및 제어

### EDR 정보 제공

- RESTful API 제공
- Syslog Server 연동

---

## 📈 기대 효과

- 위협 탐지 자동화
- 대응 시간 단축
- 로그 기반 인사이트 확보