package edu.eteslenko;

import edu.eteslenko.entity.BeanDefinition;
import edu.eteslenko.ioc.BeanDefinitionReader;
import edu.eteslenko.ioc.XMLBeanDefinitionReader;
import org.junit.Assert;

import java.util.List;

public class XMLBeanDefinitionReaderTest {

    @org.junit.Test
    public void getBeanDefinitions() {
        BeanDefinitionReader reader = new XMLBeanDefinitionReader("configuration2.xml");
        List<BeanDefinition> list = reader.getBeanDefinitions();
        BeanDefinition bean1 = list.get(0);
        BeanDefinition bean2 = list.get(1);
        Assert.assertEquals("userDao",bean1.getId());
        Assert.assertEquals("edu.eteslenko.entity.JdbcUserDao",bean1.getClassName());
        Assert.assertEquals("userService",bean2.getId());
        Assert.assertEquals("edu.eteslenko.entity.DefaultUserService",bean2.getClassName());
        Assert.assertEquals(2,bean1.getValueDependencyList().size());
        Assert.assertEquals(0,bean1.getRefDependencyList().size());
        Assert.assertEquals(1,bean2.getValueDependencyList().size());
        Assert.assertEquals(1,bean2.getRefDependencyList().size());
    }
}