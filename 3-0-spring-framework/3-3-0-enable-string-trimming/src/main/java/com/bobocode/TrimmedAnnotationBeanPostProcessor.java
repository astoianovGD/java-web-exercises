package com.bobocode;

import com.bobocode.annotation.Trimmed;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

public class TrimmedAnnotationBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();

        boolean hasTrimmedParam = Arrays.stream(beanClass.getMethods())
                .flatMap(m -> Arrays.stream(m.getParameters()))
                .anyMatch(p -> p.isAnnotationPresent(Trimmed.class));

        if (!hasTrimmedParam) {
            return bean;
        }

        ProxyFactory proxyFactory = new ProxyFactory(bean);
        proxyFactory.addAdvice((org.aopalliance.intercept.MethodInterceptor) invocation -> {
            Method method = invocation.getMethod();
            Object[] args = invocation.getArguments();

            if (args != null) {
                Method targetMethod = beanClass.getMethod(method.getName(), method.getParameterTypes());
                Parameter[] parameters = targetMethod.getParameters();

                for (int i = 0; i < parameters.length; i++) {
                    if (parameters[i].isAnnotationPresent(Trimmed.class) && args[i] instanceof String) {
                        if (args[i] != null) {
                            args[i] = ((String) args[i]).trim();
                        }
                    }
                }
            }
            return invocation.proceed();
        });

        return proxyFactory.getProxy();
    }
}