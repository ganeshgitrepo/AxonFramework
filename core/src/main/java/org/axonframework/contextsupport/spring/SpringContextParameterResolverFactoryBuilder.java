/*
 * Copyright (c) 2010-2013. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.axonframework.contextsupport.spring;

import org.axonframework.common.annotation.ClasspathParameterResolverFactory;
import org.axonframework.common.annotation.MultiParameterResolverFactory;
import org.axonframework.common.annotation.SpringBeanParameterResolverFactory;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;

/**
 * Creates and registers a bean definition for a Spring Context aware ParameterResolverFactory. It ensures that only
 * one such instance exists for each ApplicationContext.
 *
 * @author Allard Buijze
 * @since 2.1
 */
public class SpringContextParameterResolverFactoryBuilder {

    private static final String PARAMETER_RESOLVER_FACTORY_BEAN_NAME = "__axon-parameter-resolver-factory";

    /**
     * Create, if necessary, a bean definition for a ParameterResolverFactory and returns the reference to bean for use
     * in other Bean Definitions.
     *
     * @param registry The registry in which to look for an already existing instance
     * @return a BeanReference to the BeanDefinition for the ParameterResolverFactory
     */
    public static RuntimeBeanReference getBeanReference(BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(PARAMETER_RESOLVER_FACTORY_BEAN_NAME)) {
            final ManagedList<BeanDefinition> factories = new ManagedList<BeanDefinition>();
            factories.add(BeanDefinitionBuilder.genericBeanDefinition(ClasspathParameterResolverFactoryBean.class)
                                               .getBeanDefinition());
            factories.add(BeanDefinitionBuilder.genericBeanDefinition(SpringBeanParameterResolverFactory.class)
                                               .getBeanDefinition());
            AbstractBeanDefinition def =
                    BeanDefinitionBuilder.genericBeanDefinition(MultiParameterResolverFactory.class)
                                         .addConstructorArgValue(factories)
                                         .getBeanDefinition();
            registry.registerBeanDefinition(PARAMETER_RESOLVER_FACTORY_BEAN_NAME, def);
        }
        return new RuntimeBeanReference(PARAMETER_RESOLVER_FACTORY_BEAN_NAME);
    }

    private static class ClasspathParameterResolverFactoryBean implements BeanClassLoaderAware, InitializingBean,
            FactoryBean<ClasspathParameterResolverFactory> {

        private ClassLoader classLoader;
        private ClasspathParameterResolverFactory factory;

        @Override
        public ClasspathParameterResolverFactory getObject() throws Exception {
            return factory;
        }

        @Override
        public Class<?> getObjectType() {
            ApplicationContext c;
            return ClasspathParameterResolverFactory.class;
        }

        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            this.factory = ClasspathParameterResolverFactory.forClassLoader(classLoader);
        }

        @Override
        public void setBeanClassLoader(ClassLoader classLoader) {
            this.classLoader = classLoader == null ? ClassUtils.getDefaultClassLoader() : classLoader;
        }
    }
}
