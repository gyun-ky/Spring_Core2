# Spring Core - 2

## V1 
인터페이스와 구현 클래스 생성 후 수동으로 스프링 빈으로 등록

* `@Import` 수동으로 설정한 스프링 빈 파일을 참조하여 스프링 빈으로 등록 - 버전에 따라 다른 클래스들을 스프링 빈으로 등록할수 있다.
* `@SpringBootApplication(scanBasePackages = "hello.proxy.app")` 컴포넌트 스캔 대상을 설정 
  * `scanBasePackages = "hello.proxy.app"` 설정 파일이 아닌 소스 파일들만 컴포넌트 스캔 대상으로 들어갈 수 있도록 세부 설정
```java
@Import({AppV1Config.class, AppV2Config.class})
@SpringBootApplication(scanBasePackages = "hello.proxy.app") //주의
public class ProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProxyApplication.class, args);
	}

}
```

## V2
인터페이스 없이 클래스 생성 후 수동으로 스프링 빈으로 등록

### @Controller 와 @RequestMapping의 차이
둘다 Controller임을 스프링에게 알리는 역할을 함
* `@Controller` 자동으로 컴포넌트 스캔의 대상이 된다
* `@RequestMapping` 컴포넌트 스캔의 대상이 되지 않기 때문에 수동으로 스프링 빈에 등록해주어야 한다. 


## V3
자동으로 스프링 빈으로 등록

* `@RestController` @Component, @Controller 어노테이션을 포함하고 있어서 자동 스캔 대상 
* `@Service` `@Repository` 자동 컴포넌트 스캔 대상이 되게함.


## 프록시 패턴
접근 제어가 목적인 패턴

원본 코드를 수정하지 않고 코드 전반을 건드리는 기능을 도입할 수 있음
* 클라이언트가 서버에 직접 요청하는 것이 아니라 대리자를 통해서 간접적으로 요청
* `PROXY` : 대리자
* cf) 데코레이터 패턴 : 프록시 패턴과 모양은 비슷하나 새로운 기능 추가가 목적

### 대리자(proxy)의 기능
* `접근 제어` 
  * 권한에 따라서 접근을 차단 가능
  * 자주 사용하는 데이터 캐싱 가능
  * 지연 로딩 가능
* `부가 기능 추가` - 서버의 원래 기능보다 부가된 기능이 추가됨
* `프록시 체인` - 대리자가 다른 대리자를 불러서 작업을 처리, 클라이언트는 처리과정을 알지 못함.

### 대리자(Proxy)의 필수 조건 
* 서버와 프록시는 같은 interface를 사용해야 함 -> 클라이언트가 서버에 요청을 한 것인지, 프록시에 요청을 한 것인지 몰라야 함.
* 서버 객체를 프록시 객체로 대체해도 동작해야 함. -> 프록시는 대체가능해야 함, 모양이 실객체와 같고 실객체를 주입받아서 주요 로직을 실ㅅ
* 클라이언트는 Interface에만 의존 -> DI를 사용하여 대체 가능

## 데코레이터 패턴
* decorator들을 항상 꾸며줄 대상이 필요함. 
  * Component를 내부에 가지고 있고 component의 method를 항상 호출해주어야 함.
  * Component를 추상클래스로 생성하여서 중복으로 호출되는 부분을 제거

```java
 Client -- <operation()> --> Decorator -- <operation()> --> RealComponent
```

### decoreator 예시
```java
@Slf4j
public class MessageDecorator implements Component {

    private Component component;

    public MessageDecorator(Component component) {
        this.component = component;
    }

    @Override
    public String operation() {
        log.info("MessageDecorator 실행");

        String result = component.operation();
        String decoResult = "*****" + result + "*****";
        log.info("MessageDecorator 꾸미기 적용 전 = {}, 적용 후 = {}", result, decoResult);
        return decoResult;
    }
}
```

### 프록시 체인 구현

```java
  client --> timeDecorator --> messageDecorator --> realComponent
```
```java
    @Test
    void decorator2() {
        Component realComponent = new RealComponent();
        MessageDecorator messageDecorator = new MessageDecorator(realComponent);
        TimeDecorator timeDecorator = new TimeDecorator(messageDecorator);
        DecoratorPatternClient client = new DecoratorPatternClient(timeDecorator);

        client.execute();
    }
```

### 프록시 패턴과 데코레이터 패턴 차이
중요한 것은 의도
* 프록시 패턴 : for 접근 제어
* 데코레이터 패턴 : 객체에 대한 기능을 동적으로 추가 for 기능확장


## V1에 데코레이터 패턴 적용

```java
  client --> orderControllerProxy --> orderControllerV1Impl --> orderServiceProxy --> orderServiceV1Impl
```

* 실제 객체 대신에 Interface를 구현한 Proxy 구현체를 client의 request의 전방에 세운다
* 스프링 빈을 등록할 떄, proxy 객체를 등록한다. 
```java
@Configuration
public class InterfaceProxyConfig {

    @Bean
    public OrderControllerV1 OrderController(LogTrace logTrace) {
        OrderControllerV1Impl controllerImpl = new OrderControllerV1Impl(orderService(logTrace));
        return new OrderControllerInterfaceProxy(controllerImpl, logTrace);
    }

    @Bean
    public OrderServiceV1 orderService(LogTrace logTrace) {
        OrderServiceV1Impl serviceImpl = new OrderServiceV1Impl(orderRepository(logTrace));
        return new OrderServiceInterfaceProxy(serviceImpl, logTrace);
    }

    @Bean
    public OrderRepositoryV1 orderRepository(LogTrace logTrace) {
        OrderRepositoryV1Impl repositoryImpl = new OrderRepositoryV1Impl();
        return new OrderRepositoryInterfaceProxy(repositoryImpl, logTrace);
    }

}
```
프록시 객체는 `스프링 컨테이너가 관리 + Java 힙 메모리에 올라감` But 실제 객체는 Java 힙 메모리에는 올라가지만 스프링 컨테이너가 관리하지는 않는다.


## 인테페이스 기반 프록시 / 클래스 기반 프록시
* 인터페이스 기반의 프록시 : 인터페이스를 implements 하여서 구현
```java
@RequiredArgsConstructor
public class OrderRepositoryInterfaceProxy implements OrderRepositoryV1 {
    private final OrderRepositoryV1 target;
    private final LogTrace logTrace;
    @Override
    public void save(String itemId) {
        TraceStatus status = null;
        try{
            status = logTrace.begin("OrderRepository.request()");
            // target 호출
            target.save(itemId);
            logTrace.end(status);
        } catch (Exception e) {
            logTrace.exception(status, e);
            throw e;
        }
    }
}
```
* 클래스 기반 프록시 : 클래스를 상속 받아서 구₩
```java
public class OrderRepositoryConcreteProxy extends OrderRepositoryV2 {

    private final OrderRepositoryV2 target;
    private final LogTrace logTrace;

    public OrderRepositoryConcreteProxy(OrderRepositoryV2 target, LogTrace trace) {
        this.target = target;
        this.logTrace = trace;
    }

    @Override
    public void save(String itemId) {
        TraceStatus status = null;
        try{
            status = logTrace.begin("OrderRepository.request()");
            // target 호출
            target.save(itemId);
            logTrace.end(status);
        } catch (Exception e) {
            logTrace.exception(status, e);
            throw e;
        }
    }
}
```

### 클래스 기반 프록시의 유의사항
상속이라는 특성 때문에 상속 받을때, 부모 생성자를 호출해야한다 `super()`
프록시는 말그대로 실제 객체가 아닌 대리자일뿐 부모 객체의 기능을 사용하지 않는 경우, 위의 상속의 특성을 고려하여 자바 문법 처리를 해주어야 한다.
```java
public class OrderServiceConcreteProxy extends OrderServiceV2 {

    private final OrderServiceV2 target;
    private final LogTrace logTrace;

    public OrderServiceConcreteProxy(OrderRepositoryV2 orderRepository, OrderServiceV2 target, LogTrace logTrace) {
        super(null); //프록시 역할만 할것이기 때문에, 하지만 자바 문법상 필요!!
        this.target = target;
        this.logTrace = logTrace;
    }
```
* OrderRepository를 부모 클래스에서 주입받는데 자식 프록시 클래스에서는 사용하지 않기 때문에 (`target.orderItem()`을 할것이기 때문에) `super()`에 파라미터를 null로 전달하여서 자바의 문법적 오류를 해결해준다.


## 동적 프록시
* 이전의 리플렉션은 부가기능 대상 코드 만큼 로그 추적을 위한 클래스를 만들어야 한다는 단점이 있음
* JDK 동적 프록시 기술이나 CDGLIB 프록시 생성 오픈소스를 활용 -> 프록시 객체를 동적으로 생성가능
* 프록시를 적용할 객체 하나만 생성 후, 프록시 기술을 사용해서 프록시 객체를 생성

### 리플렉션
클래스나 메서드의 `메타정보`를 사용해서 동적으로 호출하는 메서드
```java
// 클래스 메타정보 획득
Class<?> classHello = Class.forName("hello.proxy.jdkdynamic.ReflectionTest$Hello");

Hello target = new Hello();
// callA 메서드 정보
Method methodCallA = classHello.getMethod("callA");  // 해당 클래스의 메서드 메타정보
Object result1 = methodCallA.invoke(target); // 메서드 메타정보로 실제 인스턴스를 호출 가능
log.info("result1={}", result1);
```
* 공통 메서드를 `Method` 클래스로 추상화 -> 언제든 내용을 갈아끼울 수 있음 -> 공통화 시키는 것이 가능해짐

```java
    @Test
    void reflection2() throws Exception {
        // 클래스 메타정보 획득
        Class<?> classHello = Class.forName("hello.proxy.jdkdynamic.ReflectionTest$Hello");

        Hello target = new Hello();
        // callA 메서드 정보
        Method methodCallA = classHello.getMethod("callA");  // 해당 클래스의 메서드 메타정보
        dynamicCall(methodCallA, target);

        // callB 메서드 정보
        Method methodCallB = classHello.getMethod("callB");
        dynamicCall(methodCallB, target);
    }

    private void dynamicCall(Method method, Object target) throws Exception {
        log.info("start");
        Object result = method.invoke(target);
        log.info("result={}", result);
    }
```
* `dynamicCall()` 
  * method : 메타정보를 통해서 호출할 메서드의 정보가 동적으로 넘어옴
  * target : 실제 실행할 인스턴스 정보가 넘어옴 method가 target 클래스에 있는 메서드가 아니면 오류 발

##### 리플렉션은 가급적으로 사용하지 않아야한다
```java
classHello.getMethod("callAaaaaaaa잘못씀");
```
* 컴파일 시점에 오류가 나는 것이 아니라, 런타임 시점에 오류가 발생한다. 

### JDK 동적 프록시
런타임에 JDK가 개발자 대신에 프록시 객체를 생성
* 프록시에 적용할 로직은 `InvocateHandler` 인터페이스에 구현해서 작성
  * `Object proxy` 프록시 자신
  * `Method method` 호출한 메서드
  * `Object[] args` 메서드를 호출할 때 전달한 인수

* 단점
  * 인터페이스가 필수적임

### CGLIB
바이트 코드를 조작해서 동적으로 동적프록시를 생성해내는 라이브러리

CGLIB는 `대상클래스$$EnhancerByCGLIB$$임의코드` 이름의 프록시 객체를 생성
* 프록시에 적용할 로직은 `MethodInterceptor` 인터페이스에 구현해서 작성
  * `obj` CGLIB가 적용된 객체
  * `method` 호출된 메서드
  * `args` 메서드를 호출하면서 전달된 인수
  * `proxy` 메서드 호출에 사정 

* 제약
  * 부모 클래스의 생성자를 체크해야 함
    * CGLIB가 상속을 통해서 동적으로 객체를 생성하기 때문에 부모 클래스에는 기본 생성자가 필요함
  * 클래스에 `final` 키워드가 붙으면 상속이 불가능
  * 메서드에 `final` 키워드가 붙으면 해당 메서드 오버라이딩 불가

### JDK 동적 프록시 & CGLIB 한계?
* 인터페이스가 있으면 JDK 동적 프록시, 인터페이스가 없으면 CGLIB! 언제나 인터페이스 유무에 따라 관련 객체들을 생성해내기에 어렵다
* 특정 조건에 맞을 때, 해당 프록시 기술을 적용하여 제공하는 그런 라이브러리는 없을까?

## 프록시 팩토리
스프링이 지원하는 프록시 기술

동적 프록시를 통합해서 편리하게 만들어주는 `ProxyFactory` 기능 제공

* 인터페이스가 있으면 JDK 동적 프록시 사용
* 구체 클래스인 경우에는 CGLIB 사용

```java
package org.appliance.intercept;

public interface MethodInterceptor extends Interceptor {
    Object invoke(MEhodInvocation invocation) throws Throwable;
}
```
* invocation
  * 메서드 호출 방법
  * 현재 프록시 객체 인스턴스
  * 메서드 파라미터
  * 메서드 정보
* Inerceptor 상속 / Interceptor는 Advice 상속

#### Advice 도입
* `InvocationHandler`와 `MethodInterceptor`는 `Advice`를 호출
* 개발자는 로직은 `Advice`에, 프록시 생성은 `proxyFactory`를 통해
* Spring은 handler들을 모두 세팅을 해놓음 

```java
client -> jdk proxy -> adviceInvocationHandler -> Advice -> target
```
```java
client -> cglib proxy -> adviceMethodInterceptor -> Advice -> target
```

#### PointCut
특정 조건에 맞을 때, 프록시 로직을 적용하는 방법



