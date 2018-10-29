package edu.eteslenko.config;

import edu.eteslenko.entity.BeanDefinition;

public interface BeanFactoryPostProcessor {

    void postProcessBeanFactory(BeanDefinition beanDefinition);
}
