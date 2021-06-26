package com.huangxy.stone.framwork;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author huangxy
 * @date 2021/06/18 13:31:13
 */
public class StoneServiceNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("service", new ProviderFactoryBeanDefinitionParser());
    }
}
