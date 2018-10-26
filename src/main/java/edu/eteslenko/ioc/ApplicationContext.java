package edu.eteslenko.ioc;

import java.util.List;

public interface ApplicationContext {

    <T> T getBean(Class<T> t);
    Object getBean(String id);
    <T> T getBean(String id, Class<T> clazz);
    List<String> getBeanNames();

}
