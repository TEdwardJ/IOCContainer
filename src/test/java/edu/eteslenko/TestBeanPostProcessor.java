package edu.eteslenko;

import edu.eteslenko.config.BeanPostProcessor;
import edu.eteslenko.entity.Person;

public class TestBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (beanName.equalsIgnoreCase("personTest")){
            ((Person)bean).setAge(33);
        }
        return bean;
    }
}
