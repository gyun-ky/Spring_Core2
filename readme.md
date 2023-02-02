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


