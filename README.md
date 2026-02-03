# 1. EAI

**프로젝트 목표**:
주문자가 결제 완료한 주문 정보를 주문 테이블(DB) 에 적재하고
동시에 회계 담당자에게는 영수증(정해진 파일 포맷) 형태로 SFTP 전송되도록 하며
ERP에 적재된 주문 데이터는 5분 주기 배치로 운송사 DB로 전달되어 배송 접수가 자동화하는 것입니다.

## 2. 주요 기술 스택
<img width="500" height="301" alt="image" src="https://github.com/user-attachments/assets/d4b77775-0969-4c0e-8cdb-f6e07375540c" />


## 3. 사용한 라이브러리
**1. 프록시**
- cglib, asm, objenesis: 인터페이스가 없는 클래스에 대해 런타임 상속을 통해 프록시 객체를 생성하여 트랜잭션과 로깅을 주입하기 위해 사용했습니다.
  
**2. 데이터베이스 및 커넥션 관리**
- ojdbc11: 오라클 DB와의 통신을 위한 핵심 드라이버입니다.
- h2: 로컬 테스트 환경을 위해 사용했습니다.
- HikariC: JDBC 커넥션 풀을 사용하여, 매 요청마다 커넥션을 생성하는 비용을 줄이고 시스템 안정성을 높였습니다.
  
**3. 스케줄링**
- quartz: 5분 주기 배치 작업 등 복잡한 스케줄링 로직을 안정적으로 실행하기 위한 스케줄러입니다.

**4. 파일전송**
- jsch: SSH/SFTP 프로토콜을 구현하여 인스피언 서버로 결과 파일을 안전하게 전송하기 위해 사용했습니다.
  
**4. 데이터 직렬화 및 유틸리티**
- ackson-databind/core/annotations: JSON 데이터 파싱 및 객체 바인딩을 위해 사용했습니다.
- jaxb-runtime, jakarta.xml.bind-api: XML 기반 데이터 처리 및 설정을 위해 도입했습니다.
- lombok: 반복적인 Getter/Setter/Contructor 코드를 줄여 객체의 응집도를 높였습니다.
  
**5. 로깅 및 테스트**
- logback-classic/core, slf4j-api: 실행 로그를 파일 및 콘솔 및 파일에 남기기 위한 로깅 프레임워크입니다.
- junit-jupiter, mockito: 단위 테스트 및 Mock 객체 활용을 통해 비즈니스 로직의 무결성을 검증하기 위해 사용하였습니다. 테스트코드가 많이 부족한데 추후 추가하겠습니다.

## 4. 시퀀스 다이어그램
**1. REST API Communication Process**
<img width="8192" height="5630" alt="image" src="https://github.com/user-attachments/assets/b8e70f67-60fe-4a1f-9c3a-c44d50611e10" />

외부 시스템과의 효율적인 데이터 연동을 위해 REST 인터페이스를 제공하며, FrontController 패턴을 통해 모든 요청을 중앙 집중식으로 처리합니다.
- Endpoint: POST /order 요청이 유입됩니다.

- FrontController: 모든 요청의 단일 진입점으로서 요청을 수신합니다.

- HandlerMapping: 요청 URL과 HTTP 메서드를 분석하여 해당 요청을 처리할 핸들러(OrderController)를 찾아 반환합니다.
- FrontInterceptor: 요청 응답의 유효성 검증과, 요청 응답에 대한 로깅을 실행합니다.

- HandlerAdaptor: 핸들러(OrderController)를 실행시킵니다.

- 비즈니스 로직:
  - Service 단계: OrderService에서 비즈니스 로직을 수행합니다.
  - Persistence 단계: OrderDao를 통해 Oracle DB에 데이터를 적재하며, Batch Insert를 통해 성능을 최적화합니다.
  - SFTP 단계: 성공적으로 저장된 데이터는 FTP 서버로 전송됩니다.
  - 이 로직은 하나의 트랜잭션에서 실행됩니다.

**2. Scheduler Process**
<img width="8192" height="5524" alt="image" src="https://github.com/user-attachments/assets/37e7b0af-b10d-42a1-ae19-9ceab5c97da1" />

Quartz Scheduler를 사용하여 5분마다 미전송 데이터를 자동으로 추출하고 운송 회사 DB로 적재하는 배치를 진행합니다.
- Job & Trigger: OrderJob이라는 작업 단위와 5분 주기를 가진 Trigger를 생성합니다.

- Scheduler Execution: Quartz 엔진이 시스템 구동과 함께 시작되어, 메모리 상에서 다음 실행 시간을 계산하고 대기합니다.

- 비즈니스 로직:
  - Data Mapping 단계: ORDER_TB의 상태'N'을 기준으로 미전송 주문을 조회 운송 회사용 데이터 객체로 변환합니다.
  - Status Update 단계: 적재 및 전송이 성공한 데이터에 한해 ORDER_TB의 상태를 'N'에서 'Y'로 업데이트하여 중복 처리를 방지합니다.

## 5. 프록시 클래스 다이어그램

<img width="751" height="741" alt="제목 없는 다이어그램 drawio (3)" src="https://github.com/user-attachments/assets/7c88d7e0-3719-475b-b3f8-c3e3514ece59" />

CGLib 프록시를 구현하여 어노테이션만으로 로깅과 트랜잭션이 가능하게합니다. 다이어그램엔 없지만 메서드에 @LogExecution이 있으면 로깅, @Transactional이 있으면 트랜잭션을 진행하도록 구현했습니다.
1. 동작원리
- Inheritance 프록시: 인터페이스가 없는 클래스를 대상으로, 런타임에 해당 클래스를 상속받는 프록시 객체($$EnhancerByCGLIB)를 생성합니다.

- Method Interception: 프록시 객체는 모든 메서드 호출을 가로채어 MethodInterceptor로 전달하며, 여기서 어노테이션 유무에 따라 부가 기능을 실행합니다.

- 어노테이션:
  - @LogExecution: 메서드 실행 전후의 시간을 나노초 단위로 측정하여 성능 데이터를 수집합니다.
  - @Transactional: TransactionManager와 연동하여 메서드 진입 시 autoCommit(false)를 설정하고, 종료 시 commit 또는 예외 발생 시 rollback을 수행합니다.

- <참고> 로그 파일은 logback.xml을 설정해서 저장하였습니다.
 

## 6. 결과
### 1. Rest API Process**
- 주문 XML 및 CURL 예
```xml
<HEADER>
    <USER_ID>USER1</USER_ID>
    <NAME>홍길동</NAME>
    <ADDRESS>서울특별시 금천구</ADDRESS>
    <STATUS>
        N
    </STATUS>
</HEADER>
<HEADER>
    <USER_ID>USER2</USER_ID>
    <NAME>유관순</NAME>
    <ADDRESS>서울특별시 구로구</ADDRESS>
    <STATUS>
        N
    </STATUS>
</HEADER>
<ITEM>
    <USER_ID>USER1</USER_ID>
    <ITEM_ID>ITEM1</ITEM_ID>
    <ITEM_NAME>청바지</ITEM_NAME>
    <PRICE>21000</PRICE>
</ITEM>
<ITEM>
    <USER_ID>USER2</USER_ID>
    <ITEM_ID>ITEM2</ITEM_ID>
    <ITEM_NAME>티셔츠</ITEM_NAME>
    <PRICE>15800</PRICE>
</ITEM>
> 
```
```Shell
 curl -X POST http://localhost:8080/order \  
     -H "Content-Type: application/xml" \
     -H "Connection: keep-alive" \
     --data-binary "@[XML 파일 경로]" \
     -v
```
- DB 저장

<img width="500" height="300" alt="image" src="https://github.com/user-attachments/assets/d6287439-675e-4262-8fa7-424a955a1b3b" />

- SFTP 전송

<img width="500" height="600" alt="image" src="https://github.com/user-attachments/assets/d828b21c-e958-472b-b3ce-6e54b812a5a2" />

### 2.Scheduler Process**

- SHIPMENT_TB
  
<img width="500" height="300" alt="image" src="https://github.com/user-attachments/assets/0fafd1a8-5a9d-4877-abf0-af24033f496d" />

  
- ORDER_TB

<img width="500" height="300" alt="image" src="https://github.com/user-attachments/assets/dade11fd-6902-4a62-bdbb-d89b13b0cab3" />

### 3. Logging

<img width="500" height="300" alt="image" src="https://github.com/user-attachments/assets/f7fa39b0-244d-46ed-a494-e57a0ee68409" />

## 7. 참고 문헌
- 토비스프링 3.0
- GoF, Design Patterns
- Spring Framework Documentation
- ETC

