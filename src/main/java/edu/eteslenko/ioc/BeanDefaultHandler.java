package edu.eteslenko.ioc;

import edu.eteslenko.entity.BeanDefinition;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanDefaultHandler extends DefaultHandler {

    private BeanDefinition beanDefinition;
    private List<BeanDefinition> beanDefinitionList = new ArrayList<>();
    private Map<String, String> attributesMap = new HashMap<>();
    private Map<String, String> valDependencyList = new HashMap<>();
    private Map<String, String> refDependencyList = new HashMap<>();


    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {

        if (qName.equalsIgnoreCase("Bean")) {
            beanDefinition = new BeanDefinition();
            getTagAttributes(attributes);
            beanDefinition.setId(attributesMap.get("id"));
            beanDefinition.setClassName(attributesMap.get("class"));
        }

        if (qName.equalsIgnoreCase("Property")) {
            getTagAttributes(attributes);
        }
    }

    private void getTagAttributes(Attributes attributes) {
        attributesMap.clear();
        for (int i = 0; i < attributes.getLength(); i++) {
            attributesMap.put(attributes.getLocalName(i), attributes.getValue(attributes.getLocalName(i)));
        }
    }

    public void endElement(String uri, String localName,
                           String qName) throws SAXException {

        if (qName.equalsIgnoreCase("Property")) {
            String key = (attributesMap.containsKey("value")) ? "value" : "ref";
            Map<String, String> beanDefList = (attributesMap.containsKey("value")) ? valDependencyList : refDependencyList;
            beanDefList.put(attributesMap.get("name"), attributesMap.get(key));
        }

        if (qName.equalsIgnoreCase("Bean")) {
            beanDefinition.setValueDependencyList(valDependencyList);
            beanDefinition.setRefDependencyList(refDependencyList);
            beanDefinitionList.add(beanDefinition);
            valDependencyList = new HashMap<>();
            refDependencyList = new HashMap<>();
        }

        if (qName.equalsIgnoreCase("Beans")) {
            attributesMap.clear();
        }
    }

    public List<BeanDefinition> getBeanDefinitionList() {
        return beanDefinitionList;
    }
}
