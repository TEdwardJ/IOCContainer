package edu.eteslenko;

import edu.eteslenko.ioc.ApplicationContext;
import edu.eteslenko.ioc.ClassPathApplicationContext;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class ClassPathApplicationContextTest {
  private ApplicationContext context;
    @Before
    public void init(){
        context = new ClassPathApplicationContext("configuration.xml");
        ((ClassPathApplicationContext) context).init();
    }

    @Test
    public void getBeanByClass() {
        JdbcUserDao o = context.getBean(JdbcUserDao.class);
        assertEquals(3000,o.getPort());
        assertEquals("localhost",o.getUrl());
    }

    @Test
    public void getBeanById() {
        JdbcUserDao o = (JdbcUserDao)context.getBean("userDao");
        assertEquals(3000,o.getPort());
        assertEquals("localhost",o.getUrl());

        DefaultUserService defaultService = (DefaultUserService) context.getBean("userService");
        assertEquals("value_suffix",defaultService.getSuffix());
        assertEquals(o,defaultService.getUserDao());
    }

    @Test
    public void getBeanByIdAndClass() {
        JdbcUserDao o = (JdbcUserDao)context.getBean("userDao",JdbcUserDao.class);
        assertEquals(3000,o.getPort());
        assertEquals("localhost",o.getUrl());
    }

    @Test
    public void getBeanByIdAndClassNegative() {
        JdbcUserDao o = (JdbcUserDao)context.getBean("userDa1o",JdbcUserDao.class);
        assertNull(o);
    }

    @Test
    public void getBeanNames() {
        assertEquals(Arrays.asList("userDao","userService"),context.getBeanNames());
    }
}