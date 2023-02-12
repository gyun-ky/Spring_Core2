package hello.proxy.cglib;

import hello.proxy.cglib.code.TimeMethodInterceptor;
import hello.proxy.common.service.ConcreteService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.cglib.proxy.Enhancer;

@Slf4j
public class CglibTest {

    @Test
    void cglib() {
        ConcreteService target = new ConcreteService();

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(ConcreteService.class); // 어떤 구체 클래스를 상속 받아서 프록시를 생성할지 지정
        enhancer.setCallback(new TimeMethodInterceptor(target)); //위에서 지정해준 class 객체를 상속 받아서 프록시 객체를 동적으로 생성
        ConcreteService proxy = (ConcreteService) enhancer.create();

        log.info("targetClass = {}", target.getClass());
        log.info("targetClass = {}", proxy.getClass());

    }
}
