package edu.eteslenko;

import edu.eteslenko.config.BeanFactoryPostProcessor;
import edu.eteslenko.entity.BeanDefinition;

public class BeanFactoryPostProcessorTest implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(BeanDefinition beanDefinition) {
        if (beanDefinition.getId().equals("userDao")){
            beanDefinition.getValueDependencyList().put("port","3005");
        }
    }
}
