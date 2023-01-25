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
