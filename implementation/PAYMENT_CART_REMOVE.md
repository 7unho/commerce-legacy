# 결제 완료 후 장바구니 상품 삭제 구현 전략

## 목적

결제가 완료되면 결제된 상품을 사용자의 장바구니에서 삭제한다.

현재 결제 성공 처리는 `PaymentService.success` -> `PaymentManager.success` 흐름으로 진행된다. 실제 주문 상태 변경, 결제 상태 변경, 쿠폰 사용, 포인트 차감/적립은 `PaymentManager.success`의 트랜잭션 안에서 처리된다.

## 현재 구조 요약

### 관련 흐름

- 장바구니 주문 생성
  - `OrderAssembler.createFromCart`
  - `CartService.getCart`
  - `Cart.toNewOrder(request.cartItemIds())`
  - `OrderService.create`
  - `OrderManager.create`
- 직접 주문 생성
  - `OrderAssembler.create`
  - `CreateOrderRequest.toNewOrder(user)`
  - `OrderService.create`
  - `OrderManager.create`
- 결제 성공
  - `PaymentAssembler.success`
  - `PaymentService.success`
  - `PaymentManager.success`

### 현재 데이터로 알 수 있는 것

- `OrderItemEntity`에는 `productId`, `productOptionId`, `quantity`가 저장된다.
- `CartItemEntity`에도 `productId`, `productOptionId`, `quantity`가 저장된다.
- `CartItemManager.deleteItemsByProductOptions(userId, productOptionIds)`가 이미 존재한다.

### 현재 데이터로 알 수 없는 것

- 주문이 장바구니에서 생성되었는지, 직접 주문으로 생성되었는지 알 수 없다.
- 주문 생성 시 선택된 `cartItemIds`가 주문이나 주문 상품에 저장되지 않는다.
- 결제 성공 시점에는 주문 상품의 `productOptionId`만 보고 사용자의 장바구니 항목을 찾아야 한다.

이 점 때문에 구현 전략은 크게 "옵션 기준 삭제"와 "장바구니 항목 추적 후 삭제"로 나뉜다.

## 전략 1. 결제 성공 시 주문 상품 옵션 기준으로 장바구니 항목 삭제

### 개요

결제 성공 트랜잭션 안에서 주문 상품의 `productOptionId` 목록을 조회하고, 동일 사용자의 활성 장바구니 항목 중 같은 `productOptionId`를 가진 항목을 soft delete 한다.

현재 코드에 이미 있는 `CartItemManager.deleteItemsByProductOptions(userId, productOptionIds)`를 활용할 수 있다.

### 예상 변경

- `OrderItemRepository`에 주문 ID로 주문 상품을 조회하는 기존 `findByOrderId(orderId)` 사용
- `PaymentManager.success`에서 주문 상품 옵션 ID 목록 조회
- `CartItemManager` 또는 별도 Logic 컴포넌트를 통해 장바구니 항목 삭제

예상 흐름:

```text
PaymentService.success
  -> PaymentManager.success
       1. 주문/결제 검증
       2. 결제 성공 처리
       3. 주문 PAID 처리
       4. 쿠폰/포인트 처리
       5. 주문 상품의 productOptionId 조회
       6. userId + productOptionIds 기준 장바구니 항목 삭제
```

### 장점

- 변경 범위가 가장 작다.
- 별도 DB 컬럼이나 테이블 추가가 필요 없다.
- `CartItemManager.deleteItemsByProductOptions`를 재사용할 수 있다.
- 결제 성공과 장바구니 삭제를 같은 트랜잭션으로 묶기 쉽다.

### 단점

- 직접 주문으로 결제한 상품도 같은 옵션이 장바구니에 있으면 삭제된다.
- 사용자가 장바구니에 같은 옵션을 담아두고 직접 구매한 경우, 의도하지 않은 삭제로 느낄 수 있다.
- 주문 생성 시 선택한 `cartItemIds` 단위로 정확하게 삭제할 수 없다.

### 레이어 관점

`PaymentManager`가 `CartItemManager`를 직접 참조하면 Logic Layer 간 참조이므로 현재 프로젝트 규칙상 허용된다. 다만 결제 개념의 Logic이 장바구니 개념의 Logic을 호출하게 되므로, 책임이 조금 넓어진다.

### 적합한 경우

- "결제된 상품과 같은 옵션은 장바구니에서 제거한다"가 정책적으로 허용되는 경우
- 빠르게 기능을 반영해야 하는 경우
- 직접 주문 후 장바구니 동일 옵션 삭제가 큰 문제가 아닌 경우

## 전략 2. 주문 생성 시 cartItemIds를 주문 상품에 저장하고 결제 성공 시 해당 항목만 삭제

### 개요

장바구니에서 주문을 만들 때 선택한 `cartItemIds`를 주문 상품에 함께 저장한다. 결제 성공 시 주문 상품에 저장된 `cartItemId`만 삭제한다.

직접 주문은 `cartItemId`가 없으므로 장바구니 삭제 대상이 되지 않는다.

### 예상 변경

- `NewOrderItem`에 nullable `cartItemId` 추가
- `Cart.toNewOrder(targetItemIds)`에서 `CartItem.id`를 `NewOrderItem.cartItemId`로 전달
- `CreateOrderRequest.toNewOrder(user)`는 `cartItemId` 없이 생성
- `OrderItemEntity`에 nullable `cartItemId` 컬럼 추가
- `OrderManager.create`에서 `OrderItemEntity.cartItemId` 저장
- `CartItemRepository`에 `findByUserIdAndIdInAndStatus` 또는 soft delete용 신규 메서드 추가
- `CartItemManager.deleteItems(userId, cartItemIds)` 신규 추가
- `PaymentManager.success`에서 주문 상품의 `cartItemId` 목록만 삭제

### 장점

- 장바구니에서 주문한 항목만 정확히 삭제할 수 있다.
- 직접 주문은 장바구니를 건드리지 않는다.
- 선택한 장바구니 항목 단위로 삭제되므로 사용자의 기대와 가장 잘 맞는다.
- 향후 주문 출처 분석이나 장바구니 전환율 분석에도 활용 가능하다.

### 단점

- DB 컬럼 추가가 필요하다.
- 주문 생성 관련 개념 객체와 Entity 변경 범위가 전략 1보다 크다.
- 기존 주문 데이터에는 `cartItemId`가 없으므로 nullable 처리와 과거 데이터 정책이 필요하다.

### 레이어 관점

주문 생성 시 장바구니 선택 정보를 주문 상품에 스냅샷으로 저장한다. 결제 성공 시에는 `PaymentManager`가 `OrderItemRepository`로 주문 상품을 읽고, `CartItemManager`로 삭제를 요청할 수 있다.

기존 공개 `OrderService.create` 시그니처는 유지할 수 있으나 `NewOrderItem` 내부 필드가 늘어난다.

### 적합한 경우

- 직접 주문과 장바구니 주문을 명확히 구분해야 하는 경우
- 사용자 경험상 "선택해서 결제한 장바구니 항목만 제거"가 중요한 경우
- DB 마이그레이션을 수용할 수 있는 경우

## 전략 3. 주문 출처를 Order에 저장하고 옵션 기준 삭제를 장바구니 주문에만 적용

### 개요

`OrderEntity`에 주문 출처를 나타내는 필드를 추가한다. 예를 들어 `OrderSource`를 `DIRECT`, `CART`로 둔다. 결제 성공 시 주문 출처가 `CART`인 경우에만 주문 상품 옵션 기준으로 장바구니를 삭제한다.

### 예상 변경

- `OrderSource` enum 추가
- `NewOrder` 또는 주문 생성 메서드에 주문 출처 추가
- `OrderEntity`에 `source` 컬럼 추가
- `OrderAssembler.create`는 `DIRECT`
- `OrderAssembler.createFromCart`는 `CART`
- 결제 성공 시 `order.source == CART`인 경우에만 옵션 기준 장바구니 삭제

### 장점

- 직접 주문으로 인한 장바구니 삭제는 막을 수 있다.
- 전략 2보다 저장해야 하는 정보가 적다.
- 주문 출처라는 비즈니스 정보가 명확해진다.

### 단점

- 장바구니 주문 안에서는 여전히 `productOptionId` 기준 삭제다.
- 같은 옵션이 장바구니에 중복될 수 있는 구조로 바뀌거나, 공유 장바구니 정책이 복잡해지면 정확도가 떨어질 수 있다.
- 선택된 `cartItemIds`를 추적하지 않으므로 전략 2만큼 정밀하지 않다.

### 적합한 경우

- 직접 주문의 장바구니 삭제만 막으면 충분한 경우
- `cartItemId`까지 저장하는 것은 과하다고 판단되는 경우
- 주문 출처 정보가 다른 기능에도 필요할 가능성이 있는 경우

## 전략 4. 결제 성공 이벤트 발행 후 장바구니 삭제를 별도 후처리로 수행

### 개요

결제 성공 트랜잭션 안에서 장바구니를 직접 삭제하지 않고, 결제 성공 이벤트를 발행한다. 이벤트 리스너가 주문 상품을 조회해 장바구니 항목을 삭제한다.

Spring event, 메시지 큐, outbox 패턴 등으로 확장할 수 있다.

### 장점

- 결제 성공 로직과 장바구니 후처리를 느슨하게 분리할 수 있다.
- 장바구니 삭제 실패가 결제 성공을 막지 않게 설계할 수 있다.
- 향후 결제 성공 후처리 기능이 늘어날 때 확장하기 좋다.

### 단점

- 현재 프로젝트 규모에는 복잡도가 높을 수 있다.
- 비동기 처리 시 일시적으로 결제 완료 상품이 장바구니에 남아 있을 수 있다.
- 실패 재시도, 중복 처리, 모니터링 정책이 필요하다.
- 이 프로젝트는 아직 작은 규모이며 헥사고날/이벤트 중심 구조를 지양하는 규칙이 있으므로 과할 수 있다.

### 적합한 경우

- 결제 성공 후처리가 여러 개로 늘어날 예정인 경우
- 장바구니 삭제가 결제 트랜잭션과 강하게 묶이지 않아도 되는 경우
- 재시도/보상 처리를 별도 체계로 관리할 준비가 된 경우

## 전략 5. 결제 생성 또는 주문 생성 시점에 장바구니에서 먼저 제거

### 개요

장바구니 주문을 만들거나 결제 요청을 만들 때 장바구니 항목을 먼저 삭제한다.

### 장점

- 결제 성공 시점의 추가 작업이 줄어든다.
- 결제 진행 중 장바구니 중복 구매 시도를 줄일 수 있다.

### 단점

- 결제 실패 또는 결제 이탈 시 장바구니 복구가 필요하다.
- 복구 로직이 없으면 사용자의 장바구니 데이터가 사라진다.
- 현재 요구사항의 "결제가 완료되면"과 맞지 않는다.

### 판단

현재 요구사항에는 적합하지 않다. 결제 완료 후 삭제하는 방향이 더 안전하다.

## 추천 전략

### 1순위: 전략 2

정확성을 우선하면 `cartItemId`를 주문 상품에 저장하고, 결제 성공 시 해당 장바구니 항목만 삭제하는 전략이 가장 좋다.

이유:

- 장바구니 주문과 직접 주문의 부작용이 섞이지 않는다.
- 사용자가 선택한 장바구니 항목만 삭제할 수 있다.
- 결제 성공 후 삭제라는 요구사항을 가장 정확히 만족한다.
- 향후 장바구니/주문 분석에도 활용 가능하다.

### 2순위: 전략 1

빠르게 구현해야 한다면 옵션 기준 삭제가 현실적인 선택이다.

다만 이 경우 정책을 명확히 해야 한다.

> 직접 주문으로 결제한 상품과 같은 옵션이 장바구니에 있으면 삭제해도 되는가?

이 질문에 "예"라고 답할 수 있을 때만 전략 1을 선택하는 것이 좋다.

## 최종 구현 시 권장 세부 정책

- 장바구니 항목 삭제는 hard delete가 아니라 기존 정책에 맞춰 `BaseEntity.delete()`를 통한 soft delete로 처리한다.
- 결제 성공 트랜잭션 안에서 삭제한다.
  - 결제 성공, 주문 PAID 처리, 쿠폰 사용, 포인트 처리, 장바구니 삭제의 정합성을 맞추기 쉽다.
- 삭제 대상이 없어도 성공으로 처리한다.
  - 이미 사용자가 장바구니에서 삭제했거나, 직접 주문이거나, 과거 데이터일 수 있다.
- 주문 상품 조회는 반복문 안에서 하지 않고 주문 ID 기준으로 한 번에 조회한다.
- 기존 Repository 메서드 시그니처는 변경하지 않고 신규 메서드를 추가한다.

## 전략 2 기준 예상 작업 목록

1. `NewOrderItem`에 `cartItemId` 추가
2. `Cart.toNewOrder`에서 장바구니 항목 ID 전달
3. 직접 주문 생성 요청은 `cartItemId = null`로 처리
4. `OrderItemEntity`에 nullable `cartItemId` 추가
5. `OrderManager.create`에서 주문 상품 생성 시 `cartItemId` 저장
6. `CartItemRepository`에 `findByUserIdAndIdInAndStatus` 신규 추가
7. `CartItemManager.deleteItems(userId, cartItemIds)` 신규 추가
8. `PaymentManager.success`에서 주문 상품 조회 후 `cartItemId`가 있는 항목만 삭제
9. 테스트 추가
   - 장바구니 주문 결제 성공 시 선택한 cartItem만 삭제
   - 직접 주문 결제 성공 시 장바구니 유지
   - 삭제 대상 cartItem이 없어도 결제 성공 유지
   - 결제 금액 불일치 등 실패 시 장바구니 유지

## 전략 1 기준 예상 작업 목록

1. `PaymentManager`에 `OrderItemRepository`, `CartItemManager` 의존성 추가
2. 결제 성공 후 주문 상품의 `productOptionId` 목록 조회
3. `CartItemManager.deleteItemsByProductOptions(payment.userId, productOptionIds)` 호출
4. 테스트 추가
   - 결제 성공 시 주문 상품 옵션과 같은 장바구니 항목 삭제
   - 삭제 대상이 없어도 결제 성공 유지
   - 결제 실패 시 장바구니 유지

## 결정 필요 사항

- 직접 주문으로 결제한 상품이 장바구니에 있을 때도 삭제할 것인가?
- 장바구니 주문에서 선택한 `cartItemIds`만 정확히 삭제해야 하는가?
- DB 컬럼 추가가 가능한가?
- 결제 성공과 장바구니 삭제가 반드시 같은 트랜잭션이어야 하는가?

위 질문에서 정확성이 중요하고 DB 변경이 가능하다면 전략 2를 추천한다. 빠른 반영이 중요하고 동일 옵션 삭제 정책을 받아들일 수 있다면 전략 1을 추천한다.
