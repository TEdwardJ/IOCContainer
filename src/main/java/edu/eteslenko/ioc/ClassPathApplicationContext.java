package edu.eteslenko.ioc;

import edu.eteslenko.entity.Bean;
import edu.eteslenko.entity.BeanDefinition;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ClassPathApplicationContext implements ApplicationContext {


    BeanDefinitionReader reader;
    List<Bean> beanList = new ArrayList<>();
    List<BeanDefinition> beanDefinitionList;
    Map<BeanDefinition,Bean> beanMapping = new HashMap<>();

    Function<Class, Predicate<Bean>> byClass = c -> bb -> bb.getValue().getClass() == c;
    Function<String, Predicate<Bean>> byId = s -> bb -> bb.getId().equals(s);
    BiFunction<String, Class, Predicate<Bean>> byIdAndClass = (s, c) -> bb -> bb.getId().equals(s) && bb.getValue().getClass() == c;

    public ClassPathApplicationContext(String path) {
        reader = new XMLBeanDefinitionReader(path);
        init();
    }

    public void init() {
        beanDefinitionList = reader.getBeanDefinitions();
        for (BeanDefinition beanDefinition : beanDefinitionList) {
            Bean bean = constructBean(beanDefinition);
            beanList.add(bean);
            beanMapping.put(beanDefinition,bean);
        }
        for (BeanDefinition beanDef : beanDefinitionList) {
            try {
                Bean bean = beanMapping.get(beanDef);
                injectValueDependency(bean.getValue(),beanDef);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        for (BeanDefinition beanDefinition : beanDefinitionList) {
            try {
                injectRefDependency(beanDefinition);
            }catch (Exception e) {
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

    private void injectRefDependency(BeanDefinition beanDefinition) throws IllegalAccessException, InvocationTargetException {
        Object o = getBean(beanDefinition.getId());
        for (Map.Entry<String, String> beanEntry : beanDefinition.getRefDependencyList().entrySet()) {
            setFieldRefBySetter(o, beanEntry);
            //setFieldRef(o, beanEntry);
        }
    }

    private void setFieldRef(Object o, Map.Entry<String, String> propertyEntry) throws IllegalAccessException, NoSuchFieldException {
        Field declaredField = getField(o, propertyEntry.getKey());//o.getClass().getDeclaredField(beanEntry.getKey());
        declaredField.setAccessible(true);
        declaredField.set(o, getBean(propertyEntry.getValue()));
        declaredField.setAccessible(false);
    }

    private void setFieldRefBySetter(Object o, Map.Entry<String, String> propertyEntry) throws InvocationTargetException, IllegalAccessException {
        Method declaredMethod = getSetter(o, propertyEntry.getKey());
        declaredMethod.invoke(o,getBean(propertyEntry.getValue()));
    }

    private Bean constructBean(BeanDefinition beanDefinition) {
        Bean bean = new Bean();
        bean.setId(beanDefinition.getId());
        try {
            Class clazz = Class.forName(beanDefinition.getClassName());
            Object instance = clazz.newInstance();
            bean.setValue(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bean;
    }


    private Field getField(Object o, String fieldName) {
        Class superClazz = o.getClass();
        Field declaredField = null;
        while (declaredField == null) {
            declaredField = Arrays
                    .stream(superClazz.getDeclaredFields())
                    .filter(t -> t.getName().equals(fieldName))
                    .findFirst()
                    .orElse(null);
            superClazz = superClazz.getSuperclass();
            if(superClazz == Object.class){
                return declaredField;
            }
        }
        return declaredField;
    }

    private Method getSetter(Object o, String fieldName) {
        Class superClazz = o.getClass();
        Method declaredMethod = null;
        String setterPattern = "set"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1);
        while (declaredMethod == null) {
            declaredMethod = Arrays
                    .stream(superClazz.getDeclaredMethods())
                    .filter(t->t.getReturnType().equals(void.class))
                    .filter(t -> t.getName().matches(setterPattern))
                    .findFirst()
                    .orElse(null);
            superClazz = superClazz.getSuperclass();
            if(superClazz == Object.class){
                return declaredMethod;
            }
        }
        return declaredMethod;
    }

    private void setFieldValue(Object o, Map.Entry<String, String> propertyEntry) throws NoSuchFieldException {

        Field declaredField = getField(o, propertyEntry.getKey());
        String value = propertyEntry.getValue();
        declaredField.setAccessible(true);
        Class fieldClass = declaredField.getType();
        try {
            if (fieldClass == int.class) {
                declaredField.set(o, Integer.valueOf(value));
            } else if (fieldClass == double.class) {
                declaredField.setDouble(o, Double.parseDouble(value));
            } else if (fieldClass == long.class) {
                declaredField.setLong(o, Long.parseLong(value));
            } else if (fieldClass  == float.class) {
                declaredField.setFloat(o, Float.parseFloat(value));
            } else if (fieldClass == boolean.class) {
                declaredField.setBoolean(o, false);
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
