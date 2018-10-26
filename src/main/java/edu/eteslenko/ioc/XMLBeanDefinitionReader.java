package edu.eteslenko.ioc;

import edu.eteslenko.entity.BeanDefinition;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class XMLBeanDefinitionReader implements BeanDefinitionReader {
    private String xmlSource;


    public XMLBeanDefinitionReader(String xmlSource) {
        this.xmlSource = xmlSource;
    }

    public XMLBeanDefinitionReader() {
    }

    public List<BeanDefinition> getBeanDefinitions() {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();

            BeanDefaultHandler beanHandler = new BeanDefaultHandler();
            parser.parse(getClass().getClassLoader().getResourceAsStream(xmlSource),beanHandler);
            //parser.parse(new File(xmlSource), beanHandler);

            return beanHandler.getBeanDefinitionList();

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);

        } catch (SAXException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

}
