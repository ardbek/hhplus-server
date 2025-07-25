# 콘서트 예약 시스템 MSA 전환

- 도메인 분리: 현재 시스템의 기능을 분석하여 마이크로 서비스 단위를 설계.
- 분산 트랜잭션 해결: MSA 환경에서 발생하는 데이터 일관성 문제를 해결.

---

## 시스템 아키텍처
현 시스템은 Spring Boot 기반의 모놀리식 애플리케이션으로, 모든 비즈니스 로직이 단일 프로젝트 내에 구현되어 있음.
모든 데이터는 단일 데이터베이스에서 관리됨.

- 기술 스택: Java, Spring Boot, JPA, Gradle
- 데이터베이스: 단일 RDBMS (e.g., MySQL, PostgreSQL)
- 구조: 모든 도메인(사용자, 예약, 결제 등)이 하나의 배포 단위에 포함

## 도메인 모델 분석
현재 시스템은 다음과 같은 핵심 도메인들로 구성.

- 사용자 (User): 인증 및 사용자 정보 관리
- 콘서트 (Concert): 콘서트, 공연일, 좌석 등 기본 정보 관리
- 대기열 (Queue): 동시 요청 제어를 위한 대기열 토큰 관리
- 예약 (Reservation): 좌석의 임시 배정 및 예약 확정
- 결제 (Payment): 사용자 잔액 관리 및 결제 처리

---

## 도메인 서비스 분리
각 도메인의 책임과 역할을 기준으로 다음과 같이 5개의 마이크로서비스를 설계.

### 사용자 서비스 (User Service)

- 주요 역할: 사용자 인증 및 정보 관리
- 책임:
    - 사용자 계정 생성 및 조회
    - 사용자 인증 (로그인)
    - API 요청 검증을 위한 JWT 토큰 발급 및 검증
- 소유 데이터: 사용자 정보 테이블

### 콘서트 서비스 (Concert Service)

- 주요 역할: 콘서트 및 좌석 정보 제공
- 책임:
    - 콘서트, 공연 일정 정보 조회
    - 특정 공연의 예약 가능한 좌석 목록 조회
    - 인기 콘서트 목록 조회
- 소유 데이터: 콘서트, 공연일정, 좌석 정보 테이블

### 대기열 서비스 (Queue Service)

- 주요 역할: 트래픽 제어를 위한 대기열 관리
- 책임:
    - 사용자 진입 시 대기열 토큰 발급
    - 대기열 순번 및 예상 시간 관리
    - 주기적으로 활성 상태 사용자 전환 (토큰 상태 변경)
- 소유 데이터: 대기열 정보

### 예약 서비스 (Reservation Service)

- 주요 역할: 콘서트 좌석 예약 처리
- 책임:
    - 좌석 임시 배정
    - 결제 완료 후 예약 상태 확정
    - 임시 배정 시간 만료 시 자동 해제 (스케줄러)
- 소유 데이터: 예약 정보 테이블

### 결제 서비스 (Payment Service)

- 주요 역할: 사용자 잔액 관리 및 결제
- 책임:
    - 사용자 잔액 충전 및 조회
    - 예약 확정을 위한 결제 요청 처리
    - 결제 및 잔액 변경 내역 기록
- 소유 데이터: 잔액, 결제 내역 테이블

---

## 분산 트랜잭션 처리 방안

### 문제: 트랜잭션 처리의 한계
기존 모놀리식 아키텍처에서는 단일 데이터베이스의 ACID 트랜잭션을 통해 데이터 일관성을 쉽게 보장할 수 있었음. 예를 들어 '좌석 예약 및 결제'는 @Transactional 어노테이션 하나로 원자적(Atomic)으로 처리.

하지만 MSA 환경에서는 예약 서비스와 결제 서비스가 각자 다른 데이터베이스를 사용하므로, 분산 시스템 전반에 걸친 데이터 일관성을 유지하는 것이 매우 어려움. 만약 예약 서비스에서 좌석을 임시 배정한 후, 결제 서비스에서 결제를 처리하다 실패하면 좌석은 점유된 상태로 남고 돈은 지불되지 않는 데이터 불일치가 발생.

### 해결 방안: Saga 패턴
이러한 문제를 해결하기 위해 Saga 패턴을 도입.

Saga는 여러 서비스에 걸쳐있는 일련의 로컬 트랜잭션을 순차적으로 실행하는 패턴입니다. 각 로컬 트랜잭션이 완료되면 다음 트랜잭션을 호출하며, 만약 어느 단계에서든 실패할 경우 **보상 트랜잭션**을 통해 이전 단계에서 수행했던 작업들을 모두 취소하여 데이터의 최종 일관성을 보장.

### 코레오그래피 vs 오케스트레이션

- 코레오그래피 기반 사가 : 중앙 조정자 없이 각 서비스가 이벤트를 발행하면, 관련된 다른 서비스가 해당 이벤트를 구독하여 다음 동작을
  수행하는 방식.
  서비스 간 결합도가 낮아 유연하지만, 전체 트랜잭션 흐름을 추적하고 관리하기 어려움.
    - 장점
        - 참여자가 적고 중앙 제어가 필요없는 경우 적합
        - 추가 서비스 구현이 필요 없음(구성 간편)
        - 역할이 분산되어 단일 실패 지점이 존재하지 않음
        - 참여자는 서로 직접 알지 못하기 때문에 느슨한 결합을 유지
    - 단점
        - 명령 추적이 어렵기 때문에 흐름 파악이 어려움
        - Saga 참가자 간에 순환 종속성 발생 가능
        - 통합 테스트가 어려움
- 오케스트레이션 : 오케스트레이터라는 중앙 조정자가 전체 트랜잭션의 흐름을 관리하고 각 서비스에 수행할 작업을 명령하는 방식.
  흐름이 중앙 집중화되어 관리 및 모니터링이 용이하지만, 오케스트레이터에 대한 의존성이 발생.
    - 장점
        - 참여자가 많거나 추가되는 상황 같이 복잡한 워크플로우에 적합
        - 활동 흐름 제어 가능
        - 오케스트레이터가 존재하여 순환 종속성이 발생되지 않음.
        - 각 참여자는 다른 참여자의 명령어를 알지 않아도 됨.
    - 단점
        - 중앙에서 관리를 위한 복잡한 로직 구현 필요
        - 모든 워크플로우를 관리하기 때문에 실패 지점이 될 수 있음

### 패턴 선택 및 사유

‘예약-결제’ 프로세스는 여러 단계에 걸쳐 명확한 순서와 흐름을 가지는 비즈니스 트랜잭션으로, 
하나의 과정이라도 실패하면 이전 단계의 작업을 모두 취소(보상 트랜잭션)해아 하고, 
전체 흐름을 명시적으로 제어할 수 있도록 오케스트레이션 기반 사가 패턴을 이용해 트랜잭선을 처리.