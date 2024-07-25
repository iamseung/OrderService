### 개요

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
