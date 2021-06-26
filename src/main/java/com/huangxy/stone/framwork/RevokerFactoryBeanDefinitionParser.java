package com.huangxy.stone.framwork;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * @author huangxy
 * @date 2021/06/19 01:26:23
 */
public class RevokerFactoryBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected Class<?> getBeanClass(Element element) {
        return RevokerFactoryBean.class;
    }

    @Override
    protected void doParse(Element element, BeanDefinitionBuilder builder) {
        String targetInterface = element.getAttribute("interface");
        String loadbalance = element.getAttribute("loadbalance");

        builder.addPropertyValue("targetInterface", targetInterface);
        builder.addPropertyValue("loadbalance", loadbalance);
    }
}
