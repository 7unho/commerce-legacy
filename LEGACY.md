# 레거시 코드 개선 항목

본 문서는 현재 프로젝트(`commerce-legacy`)에서 발견된 일관성 없는 패턴, 규칙이 명확하지 않은 부분, 그리고 개선이 필요한 레거시 코드를 정리합니다.

---

## 1. 아키텍처 및 계층 분리 이슈

### 1.1 도메인 서비스의 인프라 계층 직접 의존
**현상:**
- `CartService`, `OrderService`, `PointService` 등 도메인 서비스가 `storage.db.core`의 엔티티와 리포지토리를 직접 의존
- 예: `CartService`가 `CartItemEntity`, `CartItemRepository`, `ProductRepository`를 직접 주입받아 사용

**문제점:**
- 도메인 계층이 인프라 세부사항(JPA)에 강하게 결합됨
- 테스트 시 실제 JPA 리포지토리가 필요하여 단위 테스트 작성이 어려움
- 영속성 기술 변경 시 도메인 서비스 전체 수정 필요

**개선 방향:**
- 리포지토리 인터페이스를 도메인 계층에 정의 (Port)
- 인프라 계층에서 해당 인터페이스를 구현 (Adapter)
- 엔티티 ↔ 도메인 모델 변환을 인프라 계층에서 처리

**관련 파일:**
- `core/core-api/src/main/java/io/april2nd/commerce/core/domain/CartService.java`
- `core/core-api/src/main/java/io/april2nd/commerce/core/domain/OrderService.java`
- `core/core-api/src/main/java/io/april2nd/commerce/core/domain/PointService.java`

### 1.2 컨트롤러의 과도한 책임
**현상:**
- 컨트롤러가 여러 도메인 서비스를 조합하여 복합 응답을 생성
- 예: `ProductController.findProduct()`가 `productService`, `productSectionService`, `reviewService`, `couponService` 4개의 서비스를 호출
- 예: `OrderController.findOrderForCheckout()`가 `orderService`, `ownedCouponService`, `pointService`를 조합

**문제점:**
- 컨트롤러가 서비스 조율(orchestration) 로직을 포함
- 프레젠테이션 계층이 비즈니스 로직 흐름을 알아야 함
- 동일한 조합 로직의 재사용이 어려움

**개선 방향:**
- Facade 패턴 또는 Use Case 계층 도입
- 복합 조회 로직을 별도 서비스로 분리
- 컨트롤러는 단일 서비스 호출과 DTO 변환에만 집중

**관련 파일:**
- `core/core-api/src/main/java/io/april2nd/commerce/core/api/controller/v1/ProductController.java` (라인 35-42)
- `core/core-api/src/main/java/io/april2nd/commerce/core/api/controller/v1/OrderController.java` (라인 49-61)

---

## 2. 서비스 계층 네이밍 불일치

### 2.1 서비스 클래스 네이밍 규칙 혼재
**현상:**
- `Service`, `Handler`, `Finder`, `Manager`, `Validator` 등 다양한 네이밍 접미사 혼용
- 명확한 네이밍 규칙 없이 사용되고 있음

**예시:**
- `CartService`, `OrderService`, `PaymentService` (전통적 Service)
- `PointHandler` (Handler)
- `ReviewFinder` (Finder)
- `ReviewManager` (Manager)
- `ReviewPolicyValidator` (Validator)
- `SettlementCalculator` (Calculator)

**문제점:**
- 각 접미사가 어떤 책임을 가지는지 명확하지 않음
- 새로운 컴포넌트 추가 시 네이밍 선택 기준이 모호함
- `ReviewService`는 내부적으로 Finder, Manager, Validator를 조합하지만, 다른 Service는 직접 구현

**개선 방향:**
- 각 접미사의 역할과 책임을 명확히 정의
  - `Service`: 유스케이스 조율 (Facade 역할)
  - `Handler`: 특정 도메인 이벤트/작업 처리
  - `Finder`: 조회 전담
  - `Manager`: 생명주기 관리 (CUD 전담)
  - `Validator`: 정책 검증
- 가이드라인 문서에 네이밍 규칙 추가 및 일관된 패턴으로 리팩토링

**관련 파일:**
- `core/core-api/src/main/java/io/april2nd/commerce/core/domain/` (전체)

---

## 3. 트랜잭션 관리 불일치

### 3.1 @Transactional 적용 기준 불명확
**현상:**
- 읽기 전용 메서드에 `@Transactional` 적용: `OrderService.getOrders()` (라인 94), `OrderService.getOrder()` (라인 62)
- 쓰기 작업인데 `@Transactional` 누락: `PaymentService.fail()` (라인 95)
- `ReviewService`의 모든 쓰기 메서드(`addReview`, `updateReview`, `removeReview`)에 `@Transactional` 누락

**문제점:**
- 트랜잭션 경계가 명확하지 않아 데이터 일관성 문제 발생 가능
- 쓰기 작업에서 트랜잭션 누락 시 롤백이 되지 않거나 의도치 않은 동작 발생 가능

**개선 방향:**
- 명확한 트랜잭션 정책 수립
  - 읽기 전용: `@Transactional(readOnly = true)` 사용 (Spring) 또는 조회 메서드에서는 생략 고려
  - 쓰기 작업: 반드시 `@Transactional` 적용
- `ReviewService` 등 누락된 곳에 트랜잭션 추가

**관련 파일:**
- `core/core-api/src/main/java/io/april2nd/commerce/core/domain/OrderService.java`
- `core/core-api/src/main/java/io/april2nd/commerce/core/domain/PaymentService.java` (라인 95)
- `core/core-api/src/main/java/io/april2nd/commerce/core/domain/ReviewService.java` (라인 25-41)

---

## 4. 엔티티 ↔ 도메인 모델 변환 일관성 부족

### 4.1 변환 로직 위치 불일치
**현상:**
- `CartService.getCart()`: 서비스 내부에서 직접 엔티티를 도메인 객체로 변환 (라인 24-60)
- `PointService.balance()`, `histories()`: 서비스 내부에서 직접 생성자 호출하여 변환
- `OrderService.getOrder()`, `getOrders()`: 서비스 내부에서 변환 로직 포함

**문제점:**
- 엔티티 구조 변경 시 여러 서비스 수정 필요
- 도메인 서비스가 영속성 계층의 세부사항(엔티티 필드)을 너무 많이 알아야 함

**개선 방향:**
- 변환 책임을 인프라 계층(Adapter)으로 이동하거나, 도메인 모델/엔티티에 변환 메서드 추가
- 또는 별도의 Mapper 클래스(예: MapStruct 등) 도입 고려

**관련 파일:**
- `core/core-api/src/main/java/io/april2nd/commerce/core/domain/CartService.java`
- `core/core-api/src/main/java/io/april2nd/commerce/core/domain/PointService.java`
- `core/core-api/src/main/java/io/april2nd/commerce/core/domain/OrderService.java`

---

## 5. DTO 변환 패턴 불일치

### 5.1 요청/응답 DTO의 변환 로직
**현상:**
- `AddCartItemRequest.toAddCartItem()` 등 `to` 접두사 사용
- `OrderCheckoutResponse.of(...)` 등 정적 팩토리 메서드 `of` 사용
- 일부 DTO는 생성자를 직접 호출하여 생성(예: `PaymentController.create`의 `CreatePaymentResponse`)

**문제점:**
- 객체 생성 및 변환 방식이 혼재되어 가독성 저하
- `of` 메서드가 단일 객체를 받을 때와 복수 파라미터를 받을 때가 섞여 있음

**개선 방향:**
- DTO 변환 패턴 통일 (예: Request는 `toDomain()`, Response는 `of(domain)` 또는 `from(domain)`)
- 복합적인 데이터 조합이 필요한 응답은 별도의 컴포저(Composer)나 서비스 레이어에서 처리

**관련 파일:**
- `core/core-api/src/main/java/io/april2nd/commerce/core/api/controller/v1/request/`
- `core/core-api/src/main/java/io/april2nd/commerce/core/api/controller/v1/response/`

---

## 6. API 엔드포인트 설계 불일치

### 6.1 REST 리소스 네이밍 및 패키지 구조
**현상:**
- `/v1/cart` (단수형) vs `/v1/orders` (복수형)
- `/v1/cart-orders` (행위 기반 네이밍)
- 컨트롤러 패키지가 `v1`, `batch` 등으로 나뉘어 있으나 공통 API(Health 등) 위치가 애매함

**문제점:**
- API 일관성 부족으로 인한 클라이언트 사용 혼선
- RESTful 원칙(리소스 중심)에서 벗어난 엔드포인트 존재

**개선 방향:**
- 모든 리소스를 복수형으로 통일 (예: `/v1/carts`)
- `/v1/cart-orders`는 `/v1/orders`의 쿼리 파라미터나 요청 본문으로 구분 고려

**관련 파일:**
- `core/core-api/src/main/java/io/april2nd/commerce/core/api/controller/v1/CartController.java`
- `core/core-api/src/main/java/io/april2nd/commerce/core/api/controller/v1/OrderController.java`

---

## 7. 하드코딩 및 매직 넘버/문자열

### 7.1 하드코딩된 로직 및 NOTE 주석
**현상:**
- `PaymentService.success()` 내부에 PG 연동 관련 `NOTE` 주석과 하드코딩된 값 존재 (라인 60-69)
- `OrderService.createOrderName()`에서 `" 외 n개"` 문자열 하드코딩 (라인 133)
- `NOTE` 주석이 구현 필요, 비즈니스 설명, 설계 고민 등 다양한 목적으로 혼용됨

**문제점:**
- 실제 구현이 누락된 부분이 `NOTE`로 방치되어 추적이 어려움
- 하드코딩된 문자열로 인한 다국어 대응 및 유지보수 어려움

**개선 방향:**
- 구현이 필요한 부분은 `TODO`로 변경하여 추적 가능하게 함
- 비즈니스 규칙 설명은 KDoc이나 별도 문서화
- 하드코딩된 문자열은 상수로 분리하거나 메시지 소스 활용

**관련 파일:**
- `core/core-api/src/main/java/io/april2nd/commerce/core/domain/PaymentService.java`
- `core/core-api/src/main/java/io/april2nd/commerce/core/domain/OrderService.java`

---

## 8. 도메인 모델 설계 이슈

### 8.1 빈약한 도메인 모델 (Anemic Domain Model)
**현상:**
- `User` 클래스가 식별자만 포함: `public record User(Long id)`
- 많은 비즈니스 로직이 엔티티나 서비스에 흩어져 있고, 도메인 모델은 데이터 전달 역할에 치중

**문제점:**
- 객체지향적인 설계의 이점을 살리지 못하고 절차지향적으로 흐르기 쉬움
- 도메인 지식이 서비스 레이어에 매몰됨

**개선 방향:**
- 도메인 모델에 의미 있는 속성과 행위 추가
- 값 객체(Value Object) 적극 활용

---

## 개선 우선순위 제안

### High Priority (P0)
1. **트랜잭션 관리 불일치** - 데이터 정합성 문제 직결
2. **하드코딩된 PG 연동 로직** - 실제 결제 프로세스 미완성

### Medium Priority (P1)
3. **도메인 서비스의 인프라 직접 의존** - 테스트 및 유지보수성 저하
4. **컨트롤러의 과도한 책임** - 비즈니스 로직 응집도 저하
5. **서비스 계층 네이밍 불일치** - 코드 일관성 및 가독성

### Low Priority (P2)
6. **변환 로직 위치 및 DTO 패턴 불일치**
7. **API 리소스 네이밍 통일**

---

**Note:** 본 문서는 현재 시점의 코드 분석 결과이며, 실제 리팩토링 시 비즈니스 요구사항과 팀의 우선순위를 고려하여 진행해야 합니다.
