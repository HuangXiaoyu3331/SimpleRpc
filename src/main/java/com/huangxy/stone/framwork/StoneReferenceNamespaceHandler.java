package com.huangxy.stone.framwork;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author huangxy
 * @date 2021/06/19 01:24:37
 */
public class StoneReferenceNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("reference", new RevokerFactoryBeanDefinitionParser());
    }
}
