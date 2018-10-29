package edu.eteslenko.config;

public interface BeanPostProcessor {

    Object postProcessAfterInitialization(java.lang.Object bean, java.lang.String beanName);
    Object postProcessBeforeInitialization(java.lang.Object bean, java.lang.String beanName);
}
