package edu.eteslenko.entity;

import javax.annotation.PostConstruct;

public class Person {
    private int age;


    public int getAge() {
        return age;
    }

    public void setAge(int age) {

        this.age = age;
    }

    @PostConstruct
    public void init(){
        //Lets suppose now we get datasource bean that is already configured by BeanPostProcessor
        // and can get actual age of a person
        setAge(45);
    }
}
