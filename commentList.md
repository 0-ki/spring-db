# Spring Boot - DB

- @Transactional : 트랜잭션 관리 애너테이션, 하지만!! @Test 테스트 코드의 영역에서 사용되면, 자동으로 테스트 후 Transaction Rollback하여 DB 테스트가 반복 가능하게끔 기능한다!
- DriverManagerDataSource 를 Bean으로 직접 DataSource를 등록, dataSource.setUrl("jdbc:h2:mem:db;DB_CLOSE_DELAY=-1"); JVM에 H2를 올려서 사용 가능.
  - 이 때, 추가기능(?)으로, "test/resources/schema.sql" 경로를 찾아 초기화 SQL을 자동으로 실행해준다!
  - 아니, 위의 DataSource부분과 application.properties에서 datasource 관련 설정을 다 지우면, 아예 자동으로 스프링 부트가 같은 동작 처리를 해준다!! 
