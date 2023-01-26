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
* 서버 객체를 프록시 객체로 대체해도 동작해야 함. -> 프록시는 대체가능해야 함 
* 클라이언트는 Interface에만 의존 -> DI를 사용하여 대체 가능

### 예시 코드


