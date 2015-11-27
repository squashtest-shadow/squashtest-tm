/*
 *      This file is part of the Squashtest platform.
 *      Copyright (C) 2010 - 2015 Henix, henix.fr
 *
 *      See the NOTICE file distributed with this work for additional
 *      information regarding copyright ownership.
 *
 *      This is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU Lesser General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      (at your option) any later version.
 *
 *      this software is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU Lesser General Public License for more details.
 *
 *      You should have received a copy of the GNU Lesser General Public License
 *      along with this software.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.squashtest.tm.web.config;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.squashtest.tm.api.config.SquashPathProperties;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Gregory Fouquet
 * @since 1.13.0
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Configuration
public class PluginEnabledEmbeddedServletContainerAutoConfig {

	@Order(Ordered.HIGHEST_PRECEDENCE)
	@Configuration
	@ConditionalOnClass(ServerProperties.Tomcat.class)
	public static class EmbeddedTomcat {
//		@Inject
//		private static SquashPathProperties squashPathProperties;

		public EmbeddedTomcat() {
			super();
		}

		@Bean
		public static TomcatEmbeddedServletContainerFactory tomcatEmbeddedServletContainerFactory(SquashPathProperties squashPathProperties) {
			TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
			factory.addContextCustomizers(pluginsAwareContextCustomizer(squashPathProperties));
			return factory;
		}

		/**
		 * This customizes tomcat context by adding the plugins dir to the context classpath
		 *
		 * @return
		 */
		public static TomcatContextCustomizer pluginsAwareContextCustomizer(SquashPathProperties squashPathProperties) {
			final DirResourceSet pluginsResources = new DirResourceSet();
			pluginsResources.setBase(squashPathProperties.getPluginsPath());
			pluginsResources.setWebAppMount("/WEB-INF/lib");

			return new TomcatContextCustomizer() {
				@Override
				public void customize(Context context) {
					WebResourceRoot resource = context.getResources();

					if (resource == null) {
						resource = new StandardRoot();
						context.setResources(resource);
					}

					resource.addPostResources(pluginsResources);
				}
			};
		}

//		@Bean
		public static PluginsBeanFactoryPostProcessor pluginsBeanFactoryPostProcessor(Environment environment) {
			PluginsBeanFactoryPostProcessor processor = new PluginsBeanFactoryPostProcessor();
			processor.setEnv(environment);
			return processor;
		}
	}

	public static class PluginsBeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {
		private static final Logger LOGGER = LoggerFactory.getLogger(PluginsBeanFactoryPostProcessor.class);
		private Environment env;

		@Override
		public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
			if (beanFactory instanceof ConfigurableBeanFactory) {
				ConfigurableBeanFactory applicationContext = (ConfigurableBeanFactory) beanFactory;

				File pluginsPath = new File(env.getProperty("squash.path.plugins-path"));
				if (pluginsPath.exists() && pluginsPath.isDirectory()) {
					LOGGER.info("Enumerating plugins in folder '{}'", pluginsPath.getAbsolutePath());

					File[] plugins = pluginsPath.listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							return name.endsWith(".jar");
						}
					});

					URL[] pluginsUrls = new URL[plugins.length];

					for (int i = 0; i < plugins.length; i++) {
						try {
							pluginsUrls[i] = plugins[i].toURI().toURL();
						} catch (MalformedURLException e) {
							// I guess this should not happen
							LOGGER.warn("Plugin file '{}' could not be converted into a URL", plugins[i], e);
						}
					}

					ClassLoader cl = new URLClassLoader(pluginsUrls, applicationContext.getBeanClassLoader());
					applicationContext.setBeanClassLoader(cl);

				} else {
					LOGGER.warn("Plugins folder '{}' does not exist or is not a folder. Squash won't register any plugin.", pluginsPath.getAbsolutePath());
				}
			} else {
				LOGGER.warn("I cannot set class loader for bean factory of type{}. Squash won't register any plugin.", beanFactory.getClass());
			}

		}

		@Override
		public int getOrder() {
			return Ordered.HIGHEST_PRECEDENCE + 100;
		}

		public void setEnv(Environment env) {
			this.env = env;
		}
	}
}
