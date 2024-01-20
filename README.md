# mysql 관련 애플리케이션

## setup

- mysql 8.0.33 버전 docker container로 생성
- spring boot 3 version (3.1.2)
- springdoc-openapi 의존성 변경

```groovy
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0'
```
**2.1.0에서 java record에 대한 ParameterObject bug fix**  
2.1.0 이상 사용하려면 webmvc-ui 사용해야 함  
또한 spring boot v3 이상이어야 하는 듯

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

### 분포도에 따른 인덱스 성능 차이

인덱스 1. memberId, 2. createdDate, 3. (memberId, createdDate)

- **memberId** 인덱스로 타는 경우

```sql
-- memberId
--  - 3 : 100만건
--  - 4 : 200만건

SELECT createdDate, memberId, COUNT(id) AS count
FROM Post use index POST_index_member_id
WHERE memberId = ${memberId} AND createdDate between '1900-01-01' and '2023-01-01'
GROUP BY memberId, createdDate;
```
`FROM Post use index [memberId_index] where memberId = 4 ...`  
memberId 인덱스를 가지고 조회하도록 쿼리 실행하면 오히려 full scan 때보다 많이 걸림  
`memberId = 4`로 index 테이블에 해당 페이지로 이동해도 200만건이나 되고 거기서 찾아서 스토리지 엔진 접근하고 해야 함  
(오히려 바로 table full scan 하는 것보다 훨씬 더 오래 걸림, 6배 ~ 13배 차이)

> **인덱스는 해당 칼럼의 데이터 분포도에 영향을 받는다.(카디널리티 관련)**

- **createdDate** 인덱스로 타는 경우

```sql
-- createdDate
-- 랜덤 생성했기 때문에 골고루 분포 대략 2만개 정도의 고유 식별 개수를 가짐

SELECT createdDate, memberId, COUNT(id) AS count
FROM Post use index POST_index_created_date
WHERE memberId = ${memberId} AND createdDate between '1900-01-01' and '2023-01-01'
GROUP BY memberId, createdDate;
```
`memberId = 4`로 했을 때는 성능이 아주 좋다.  
`memberId = 1(없는 id)`로 했을 때는 오히려 성능이 아주 안좋아짐  
group by가 있고 없고가 성능차이가 크다. `memberId = 1` 일 때는 오히려 memberId 인덱스로 조회하는 것이 더 좋다.

> `memberId = 1`에서 왜 성능이 안좋아지는지 이유는 아직 모르겠다

- (memberId, createdDate) 인덱스로 타는 경우

```sql
SELECT createdDate, memberId, COUNT(id) AS count
FROM Post use index POST_index_member_id_created_date
WHERE memberId = ${memberId} AND createdDate between '1900-01-01' and '2023-01-01'
GROUP BY memberId, createdDate;
```
복합 인덱스는 
첫 번째 칼럼으로 정렬되고 만약 값이 동일하면 두 번째 칼럼으로 정렬되어 인덱스 테이블 저장됨  
그래서 group by 하면 인덱스만으로도 바로 조회 결과를 만들 수 있다.

### covering index

테이블에 접근하지 않고 인덱스 테이블만으로도 데이터를 응답할 수 있다는 개념  

```sql
SELECT age, id
FROM members
WHERE age < 30
```
index: age / pk: id  
이 상황에서 age 만 조회하거나 age, id만 조회하는 경우 인덱스 테이블 만으로도 조회 가능 (테이블 조회 필요 X)  
id도 클러스터링 index이기 때문에 index 테이블 마지막 리프 노드에 존재하게 된다.

## Pagination

### offset과 cursor 기반 방식의 차이점

- paging 조회하는 중에 최신 데이터가 업데이트 되면 offset 방식은 첫 번째 페이지에서 밀린 데이터들을 두 번째 페이지에서 중복으로 볼 수 있음
- cursor 조회는 cursor가 되는 식별자 id 값으로 조회하기 때문에 데이터는 고정

### 커버링 인덱스와 페이지네이션

```mysql
-- 나이가 30 이하인 회원의 이름을 2개만 조회
select name
from members
where age <= 30
limit 2
```
age가 30이하인 데이터가 1000개라고 한다면 age index table 접근 후 1000개의 데이터를 테이블에서 가져오게 된다.  
그 이후 limit 2로 잘라서 응답하는 것 같음  
이렇게 되면 random I/O가 많이 발생 <- 이걸 커버링 인덱스로 보완

```mysql
with 커버링 as (
  select id
  from members
  where age <= 30
  limit 2
)

select name
from members m
inner join 커버링 c on c.id = m.id;
```
이렇게 해서 가져오면 커버링 인덱스로 id 값 2개를 가져옴  
random IO를 줄일 수 있음  
order by, offset, limit 등 불필요한 데이터 블록 접근을 커버링 인덱스로 최소화할 수 있음
