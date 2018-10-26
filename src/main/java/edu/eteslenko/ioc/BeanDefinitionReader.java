package edu.eteslenko.ioc;

import edu.eteslenko.entity.BeanDefinition;

import java.util.List;

public interface BeanDefinitionReader {
    List<BeanDefinition> getBeanDefinitions();
}
