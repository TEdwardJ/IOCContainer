package edu.eteslenko.entity;

import java.util.HashMap;
import java.util.Map;

public class BeanDefinition {
    private String id;
    private String className;
    private Map<String, String> valueDependencyList = new HashMap<>();
    private Map<String, String> refDependencyList = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Map<String, String> getValueDependencyList() {
        return valueDependencyList;
    }

    public void setValueDependencyList(Map<String, String> valueDependencyList) {
        this.valueDependencyList = valueDependencyList;
    }

    public Map<String, String> getRefDependencyList() {
        return refDependencyList;
    }

    public void setRefDependencyList(Map<String, String> refDependencyList) {
        this.refDependencyList = refDependencyList;
    }

    @Override
    public String toString() {
        return "BeanDefinition{" +
                "id='" + id + '\'' +
                ", className='" + className + '\'' +
                ", valueDependencyList=" + valueDependencyList +
                ", refDependencyList=" + refDependencyList +
                '}';
    }
}
