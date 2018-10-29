package edu.eteslenko.ioc;

import edu.eteslenko.config.BeanFactoryPostProcessor;
import edu.eteslenko.config.BeanPostProcessor;
import edu.eteslenko.entity.Bean;
import edu.eteslenko.entity.BeanDefinition;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ClassPathApplicationContext implements ApplicationContext {

    private BeanDefinitionReader reader;
    private List<BeanDefinition> beanPostProcessorFactoryList = new ArrayList<>();
    private List<BeanDefinition> beanPostProcessorList = new ArrayList<>();
    private List<BeanDefinition> ordinaryBeanList = new ArrayList<>();
    private Map<BeanDefinition, Bean> totalBeanMapping = new HashMap<>();



    public ClassPathApplicationContext(String path) {
        reader = new XMLBeanDefinitionReader(path);
        init();
    }

    private boolean isInterfaceImplemented(Class implementation, Class interfaceClass) {
        Class superClass = implementation;
        while (superClass != Object.class) {
            for (Class aClass : implementation.getInterfaces()) {
                if (aClass == interfaceClass) {
                    return true;
                }
            }
            superClass = superClass.getSuperclass();
            ;
        }
        return false;
    }

    private List<BeanDefinition> findSystemFactoryBeans() {
        return getBeanDefinitionsByInterface(BeanFactoryPostProcessor.class);
    }

    private List<BeanDefinition> findPostProcessorBeans() {
        return getBeansByInterface(BeanPostProcessor.class);
    }

    private List<BeanDefinition> getBeansByInterface(Class interfaceClass) {
        List<BeanDefinition> beanList = new ArrayList();
        for (BeanDefinition beanDefinition : totalBeanMapping.keySet()) {
            Bean bean = totalBeanMapping.get(beanDefinition);
            if (bean != null) {
                Class beanClass = bean.getValue().getClass();
                if (isInterfaceImplemented(beanClass, interfaceClass)) {
                    beanList.add(beanDefinition);
                }
            }
        }
        return beanList;
    }

    private List<BeanDefinition> getBeanDefinitionsByInterface(Class interfaceClass) {
        List<BeanDefinition> beanDefinition = new ArrayList();
        for (BeanDefinition beanDef : totalBeanMapping.keySet()) {
            try {
                Class beanClass = Class.forName(beanDef.getClassName());
                if (isInterfaceImplemented(beanClass, interfaceClass)) {
                    beanDefinition.add(beanDef);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return beanDefinition;
    }


    public void init() {
        List<BeanDefinition> beanDefinitionList = reader.getBeanDefinitions();
        for (BeanDefinition beanDefinition : beanDefinitionList) {
            totalBeanMapping.put(beanDefinition, null);
        }

        beanPostProcessorFactoryList.addAll(findSystemFactoryBeans());

        constructBeans(beanPostProcessorFactoryList);
        configureWithBeanPostProcessorFactory();
        beanDefinitionList.removeAll(beanPostProcessorFactoryList);
        ordinaryBeanList.addAll(beanDefinitionList);

        constructBeans(ordinaryBeanList);

        beanPostProcessorList.addAll(findPostProcessorBeans());

        configureWithBeanPostProcessorBefore();
        postConstructConfiguration();
        configureWithBeanPostProcessorAfter();
    }

    private void postConstructConfiguration() {
        for (BeanDefinition beanDefinition : ordinaryBeanList) {
            Object instance = getBean(beanDefinition.getId());
            for (Method method : getPostConstructMethods(instance)) {
                try {
                    method.invoke(instance);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }


        }
    }


    private void injectRefDependencies(List<BeanDefinition> beanList) {
        for (BeanDefinition beanDefinition : beanList) {
            try {
                injectRefDependency(beanDefinition);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void injectValueDependencies(List<BeanDefinition> beanList) {
        for (BeanDefinition beanDef : beanList) {
            try {
                Bean bean = totalBeanMapping.get(beanDef);
                injectValueDependency(bean.getValue(), beanDef);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
    }

    private void constructBeans(List<BeanDefinition> beanList) {
        for (BeanDefinition beanDefinition : beanList) {
            Bean bean = constructBean(beanDefinition);
            totalBeanMapping.put(beanDefinition, bean);
        }
        injectValueDependencies(beanList);
        injectRefDependencies(beanList);
    }

    private void configureWithBeanPostProcessorFactory() {
        for (BeanDefinition beanPostProcessorFactory : beanPostProcessorFactoryList) {
            BeanFactoryPostProcessor postProcessor = (BeanFactoryPostProcessor) totalBeanMapping.get(beanPostProcessorFactory).getValue();
            for (BeanDefinition beanDefinition : totalBeanMapping.keySet()) {
                postProcessor.postProcessBeanFactory(beanDefinition);
            }
        }
    }

    private void configureWithBeanPostProcessorBefore() {
        for (BeanDefinition beanPostProcessor : beanPostProcessorList) {
            BeanPostProcessor postProcessor = (BeanPostProcessor) totalBeanMapping.get(beanPostProcessor).getValue();
            for (BeanDefinition beanDefinition : ordinaryBeanList) {
                Bean bean = totalBeanMapping.get(beanDefinition);
                bean.setValue( postProcessor.postProcessBeforeInitialization(bean.getValue(), bean.getId()));
                totalBeanMapping.put(beanDefinition, bean);
            }
        }
    }


    private void configureWithBeanPostProcessorAfter() {
        for (BeanDefinition beanPostProcessor : beanPostProcessorList) {
            BeanPostProcessor postProcessor = (BeanPostProcessor) totalBeanMapping.get(beanPostProcessor).getValue();
            for (BeanDefinition beanDefinition : ordinaryBeanList) {
                Bean bean = totalBeanMapping.get(beanDefinition);
                bean.setValue(postProcessor.postProcessAfterInitialization(bean.getValue(), bean.getId()));
                totalBeanMapping.put(beanDefinition, bean);
            }
        }
    }


    private Object getBean(Predicate<Bean> condition) {
        Optional bean = totalBeanMapping.values()
                .stream()
                .filter(t -> t != null)
                .filter(condition)
                .map(e -> e.getValue())
                .findFirst();
        return bean.orElse(null);
    }

    @Override
    public <T> T getBean(Class<T> t) {
        return (T) getBean(b -> b.getValue().getClass() == t);
    }

    @Override
    public Object getBean(String id) {
        return getBean(b -> b.getId().equals(id));
    }

    @Override
    public <T> T getBean(String id, Class<T> clazz) {
        return (T) getBean(bb -> bb.getId().equals(id) && bb.getValue().getClass() == clazz);
    }

    @Override
    public List<String> getBeanNames() {
        return totalBeanMapping.values().stream()
                .map(e -> e.getId())
                .collect(Collectors.toList());
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
        }
    }


    private void setFieldRefBySetter(Object o, Map.Entry<String, String> propertyEntry) throws InvocationTargetException, IllegalAccessException {
        Method declaredMethod = getSetter(o, propertyEntry.getKey());
        declaredMethod.invoke(o, getBean(propertyEntry.getValue()));
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
            if (superClazz == Object.class) {
                return declaredField;
            }
        }
        return declaredField;
    }

    private Method getSetter(Object o, String fieldName) {
        Class superClazz = o.getClass();
        Method declaredMethod = null;
        String setterPattern = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        while (declaredMethod == null) {
            declaredMethod = Arrays
                    .stream(superClazz.getDeclaredMethods())
                    .filter(t -> t.getReturnType().equals(void.class))
                    .filter(t -> t.getName().matches(setterPattern))
                    .findFirst()
                    .orElse(null);
            superClazz = superClazz.getSuperclass();
            if (superClazz == Object.class) {
                return declaredMethod;
            }
        }
        return declaredMethod;
    }

    private List<Method> getPostConstructMethods(Object o) {
        Class superClazz = o.getClass();
        List<Method> declaredMethod = null;
        while (declaredMethod == null) {
            declaredMethod = Arrays
                    .stream(superClazz.getDeclaredMethods())
                    .filter(t -> t.isAnnotationPresent(PostConstruct.class))
                    .collect(Collectors.toList());
            superClazz = superClazz.getSuperclass();
            if (superClazz == Object.class) {
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
            } else if (fieldClass == float.class) {
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
