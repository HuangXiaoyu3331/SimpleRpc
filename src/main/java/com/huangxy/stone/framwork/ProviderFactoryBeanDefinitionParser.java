package com.huangxy.stone.framwork;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * @author huangxy
 * @date 2021/06/18 13:33:25
 */
public class ProviderFactoryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return ProviderFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        String ref = element.getAttribute("ref");
        String serviceItf = element.getAttribute("interface");

        builder.addPropertyReference("serviceObject", ref);
        builder.addPropertyValue("serviceItf", serviceItf);
    }

}
