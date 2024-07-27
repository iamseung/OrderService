### 프로젝트 개요

온라인으로 상품을 등록하고 판매하는 e-commerce 시스템, 다량의 데이터와 높은 동시접속자 수 환경에서 안정적인 요청을 처리할 수 있어야 한다.

<aside>
💡 실무에서 결제 프로세스를 다루며 많은 문제점들이 발견됐다. 레거시한 코드와 프로세스를 통해 처리를 하다 보니 성능이 최악이였으며, 약간의 과부하만 발생해도 지연 문제가 빈번하게 발생했다. 또한 컴포넌트 간의 결합이 너무 나도 강했으며 모든 관리를 주문 프로세스에서 처리했다.

이를 Java, Spring Boot 로 재구현하며 비동기적이며 MSA 를 적용한 결제 서비스를 구현한다.
</aside>

### Stack

*JDK11, SpringBoot 2.7, Docker, Apache Kafka 3.6.0, MySQL, Redis, Cassandra, Apache JMeter*

- MSA 로 서비스 구현
- 이벤트 브로커 + Kafka 를 활용한 서비스
- Cassandra + NoSQL

## 수직 확장(Vertical Scaling)

- 개별 서버의 업그레이드로 성능 향상 (CPU, RAM, DISK 등)
- 확장시 일시적 중단 필요
- 하드웨어 비용이 높고, 금방 한계에 도달

## 수평 확장(Horizontal Scaling)

- 서버를 추가하여 처리 능력을 확장
- 일시적 중단이 필요없고 리소스 추가/제거에 유연
- 이론상 무제한 추가
- 하드웨어 비용 저렴
- 복잡성 증가
- 데이터 일관성 문제

주로 수평 확장을 고려하지만 관계형 데이터 베이스 서버(RDB Server)의 경우엔 수직 확장을 더 많이 고려, 하지만 NoSQL DataBase Server 는 수평 확장을 선택

# 분산으로 해결되지 않는 문제

1. 분산 처리에 따라오는 복잡도와 동시성 문제
    1. 데이터 일관성의 문제
    2. Routing 의 처리
    3. 노드 개수 변화에 따른 재조정, rebalancing
    4. 부분 장애
2. 요청량의 급변할 때의 확장 문제, spike traffic
3. 처리량을 넘었을 때 발생할 수 있는 일시적인 오류

이를 해결하기 위한 방안으로 ***Event-Driven 아키텍처*** 를 선택.

[Docker Compose 로 Kafka Cluster 구현] (https://seung-seok.tistory.com/entry/Docker-Docker-Compose-%EB%A1%9C-Kafka-%EB%A5%BC)
