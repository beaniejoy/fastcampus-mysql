# mysql 관련 애플리케이션

## 필기내용

`MemberNicknameHistory` 과거 이력 저장 관리하는 Entity  
과거 이력 데이터는 정규화의 대상이 아니다.  
nickname 필드가 있다고 중복이 아니다.  
또한 과거 이력 저장할 때 주의할 점이 시점에 대한 고민이 있어야 한다.  

ex) e-commerce에서 주문내역에 제조사 식별자를 남겨야할 때  
제조사 이름이 바뀔 때 바뀐 제조사의 이름으로 이전에 주문내역도 갱신을 해야할지  
아니면 그대로 내버려 두어 과거 데이터로 남겨둘지 기획 단계에서 고려 필요    
-> **데이터가 최신성을 고려해야할지 과거로 남겨야할지**

## 정규화 내용

- 정규화는 생각보다 좋지 않을 수 있다.
- 정규화 > read, write의 trade-off
  - 테이블의 중복을 줄이려 정규화를 한다면 읽기의 성능을 줄이고 쓰기의 성능을 높이게 됨
  - 정규화가 많아지면 table 간 depth가 깊어져 join 등이 많이 발생하게 됨, 혹은 여러번 조회를 해야할 수 있음
- 이러한 특징을 기억해서 적절히 정규화하는 것이 중요
  - ex. 히스토리 데이터 같은 경우 정규화를 하지 않는 것이 좋다.
- 정규화 고려사항
  - 얼마나 빠르게 데이터 최신성 보장해야 하는가 
  - 히스토리성 데이터는 오히려 정규화 하지 않아야 함
  - 데이터 변경 주기와 조회 주기는 어떻게 되는지 > 변경주기가 잦다면 정규화를, 조회주기가 더 짧다면 역정규화를
  - 객체(테이블) 탐색 깊이가 얼마나 깊은가
- join 관련
  - join 쿼리를 사용하는 것보다 조회 쿼리를 여러 번 하는 것이 더 나을 수 있다.
  - join은 테이블간 결합도를 엄청나게 높이게 됨
    - 하나의 테이블 변경이 일어날 때 여러 테이블에 영향을 줄 수 있고
    - fk로 인해 데드락이슈도 발생 가능(?) > 이건 좀 찾아봐야할 듯
  - 조회시 성능이 좋은 별도 DB나 캐싱등 다양한 최적화 기법을 이용할 수 있음

## Index

DB 성능 핵심은 Disk I/O를 줄이는 것
- 메모리에 올라온 데이터로 최대한 요청 처리(메모리 cache hit ratio를 높이는 것이 중요)
- DB 쓰기 작업도 곧바로 디스크에 쓰지 않고 먼저 메모리에 쓴다.
- 메모리 데이터 유실 고려해 WAL(Write Ahead Log) 사용
  - Random I/O, Sequential I/O > 순차 I/O로 작동되는 것이 성능에 좋음
  - 대부분의 트랜잭션은 무작위 write 발생
  - 이를 지연시켜 랜덤 I/O 줄이고 순차 I/O 발생시켜 정합성 유지(log file에 write 작업을 end 부분부터 순차적으로 메모리에 쌓음)
  - 설령 메모리에 log file이 적재된 상태로 서버가 죽어버려도 서버가 다시 up 될 때 자동으로 log file을 재실행(WAL)
- **Disk Random I/O를 최소화하는 것**
