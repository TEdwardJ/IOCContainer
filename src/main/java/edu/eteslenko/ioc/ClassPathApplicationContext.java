package edu.eteslenko.ioc;

import edu.eteslenko.entity.Bean;
import edu.eteslenko.entity.BeanDefinition;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ClassPathApplicationContext implements ApplicationContext {


    BeanDefinitionReader reader;
    List<Bean> beanList = new ArrayList<>();
    List<BeanDefinition> beanDefList = new ArrayList<>();

    Function<Class, Predicate<Bean>> byClass = c -> bb -> bb.getValue().getClass() == c;
    Function<String, Predicate<Bean>> byId = s -> bb -> bb.getId().equals(s);
    BiFunction<String, Class, Predicate<Bean>> byIdAndClass = (s, c) -> bb -> bb.getId().equals(s) && bb.getValue().getClass() == c;

    public ClassPathApplicationContext(String path) {
        reader =  new XMLBeanDefinitionReader(path);
    }

    public void init() {
        beanDefList = reader.getBeanDefinitions();
        for (BeanDefinition beanDefinition : beanDefList) {
            beanList.add(constructBean(beanDefinition));
        }

        for (BeanDefinition beanDefinition : beanDefList) {
            try {
                injectRefDependency(beanDefinition);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private Object getBean(Predicate<Bean> condition) {
        Optional bean = beanList.stream()
                .filter(condition)
                .map(e -> e.getValue())
                .findFirst();
        return bean.orElse(null);
    }

    @Override
    public <T> T getBean(Class<T> t) {
        return (T) getBean(byClass.apply(t));
    }

    @Override
    public Object getBean(String id) {
        return getBean(byId.apply(id));
    }

    @Override
    public <T> T getBean(String id, Class<T> clazz) {
        return (T) getBean(byIdAndClass.apply(id, clazz));
    }

    @Override
    public List<String> getBeanNames() {
        return beanList.stream().map(e -> e.getId()).collect(Collectors.toList());
    }

    public void injectValueDependency(Object o, BeanDefinition beanDefinition) throws NoSuchFieldException {
        for (Map.Entry<String, String> propertyEntry : beanDefinition.getValueDependencyList().entrySet()) {
            setFieldValue(o, propertyEntry);
        }
    }

    private void injectRefDependency(BeanDefinition beanDefinition) throws NoSuchFieldException, IllegalAccessException {
        Object o = getBean(beanDefinition.getId());
        for (Map.Entry<String, String> beanEntry : beanDefinition.getRefDependencyList().entrySet()) {
            setFieldRef(o, beanEntry);
        }
    }

    private void setFieldRef(Object o, Map.Entry<String, String> beanEntry) throws IllegalAccessException, NoSuchFieldException {
        Field declaredField = o.getClass().getDeclaredField(beanEntry.getKey());
        declaredField.setAccessible(true);
        declaredField.set(o, getBean(beanEntry.getValue()));
        declaredField.setAccessible(false);
    }

    private Bean constructBean(BeanDefinition beanDefinition) {
        Bean bean = new Bean();
        bean.setId(beanDefinition.getId());
        try {
            Class cx = Class.forName(beanDefinition.getClassName());
            Object constructedObject = cx.newInstance();
            injectValueDependency(constructedObject, beanDefinition);
            bean.setValue(constructedObject);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return bean;
    }


    private void setFieldValue(Object o, Map.Entry<String, String> propertyEntry/*Field declaredField, String value*/) throws NoSuchFieldException {

        Class superClazz = o.getClass();
        Field declaredField=null;
        while(declaredField == null){
            try {
                declaredField = superClazz.getDeclaredField(propertyEntry.getKey());
            }catch(NoSuchFieldException e){
                superClazz = superClazz.getSuperclass();
                if (superClazz == Object.class)
                    throw new NoSuchFieldException();
            }
        }
        String value = propertyEntry.getValue();
        declaredField.setAccessible(true);
        Class fieldClass = declaredField.getType();
        try {
            if (fieldClass.equals(int.class)) {
                declaredField.set(o, Integer.valueOf(value));
            } else if (fieldClass.equals(double.class)) {
                declaredField.set(o, Double.valueOf(value));
            } else if (fieldClass.equals(long.class)) {
                declaredField.set(o, Long.valueOf(value));
            } else if (fieldClass.equals(float.class)) {
                declaredField.set(o, Float.valueOf(value));
            } else if (fieldClass.equals(boolean.class)) {
                declaredField.set(o, false);
            } else if (isCharSequence(fieldClass)) {
                declaredField.set(o, value);
            } else {
                declaredField.set(o, null);
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        declaredField.setAccessible(false);
    }

    private static boolean isCharSequence(Class fieldClass) {
        return fieldClass.equals(Character.class) ||
                fieldClass.equals(char.class) ||
                Arrays.stream(fieldClass.getInterfaces()).anyMatch(i -> i.getName().contains("CharSequence"));
    }
}
