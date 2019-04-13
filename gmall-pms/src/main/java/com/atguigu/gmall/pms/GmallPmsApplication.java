package com.atguigu.gmall.pms;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

//开启事务
//合理使用Required,Require_New
/**解决service自己掉方法没有事务控制的问题
 *(原因：xxxService.XXX()，yyy（），拿不到自己的代理对象，解决：拿到自己的代理对象)
 * 1.让别的service调；
 * 2.自己调自己
 *  （1）、引入spring-aop（高级aop场景，aspectj）切面
 *  （2）、EnableTransactionManagement(proxyTargetClass = true) //开启事务
 *  （3）、@EnableAspectJAutoProxy(exposeProxy = true) //暴露出这个类的代理对象
 */
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableTransactionManagement(proxyTargetClass = true)
@EnableDubbo
@MapperScan("com.atguigu.gmall.pms.mapper")
@SpringBootApplication
public class GmallPmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallPmsApplication.class, args);
    }

}
