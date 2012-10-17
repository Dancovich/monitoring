/*
 * Demoiselle Framework
 * Copyright (C) 2011 SERPRO
 * ----------------------------------------------------------------------------
 * This file is part of Demoiselle Framework.
 * 
 * Demoiselle Framework is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this program; if not,  see <http://www.gnu.org/licenses/>
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA  02110-1301, USA.
 * ----------------------------------------------------------------------------
 * Este arquivo é parte do Framework Demoiselle.
 * 
 * O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
 * modificá-lo dentro dos termos da GNU LGPL versão 3 como publicada pela Fundação
 * do Software Livre (FSF).
 * 
 * Este programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA
 * GARANTIA; sem uma garantia implícita de ADEQUAÇÃO a qualquer MERCADO ou
 * APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/LGPL em português
 * para maiores detalhes.
 * 
 * Você deve ter recebido uma cópia da GNU LGPL versão 3, sob o título
 * "LICENCA.txt", junto com esse programa. Se não, acesse <http://www.gnu.org/licenses/>
 * ou escreva para a Fundação do Software Livre (FSF) Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
 */
package br.gov.frameworkdemoiselle.monitoring.internal.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.internal.bootstrap.AbstractLifecycleBootstrap;
import br.gov.frameworkdemoiselle.internal.configuration.ConfigurationLoader;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.lifecycle.Startup;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.MIB;
import br.gov.frameworkdemoiselle.monitoring.internal.configuration.snmp.SNMPAgentConfig;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.jmx.MBeanManager;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.SNMPAgent;
import br.gov.frameworkdemoiselle.monitoring.stereotype.MBean;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Bootstrap class intented to initialize and start the <b>SNMP agent</b> automatically
 * on application startup. Moreover, it stops the agent before application shutdown.
 * 
 * @author SERPRO
 */
public class SNMPAgentBootstrap extends AbstractLifecycleBootstrap<Startup> {

	private static Logger logger = LoggerProducer.create(SNMPAgentBootstrap.class);

	private static final Class<SNMPAgentConfig> SNMP_AGENT_CONFIG = SNMPAgentConfig.class;
	
	private SNMPAgent agent;
	private SNMPAgentConfig config;
	
	private boolean started = false;
	
	protected static List<AnnotatedType<?>> types = Collections.synchronizedList(new ArrayList<AnnotatedType<?>>());
	
	private ResourceBundle bundle;

	private void readConfiguration() {
		getLogger().debug(getBundle().getString(
				"bootstrap.configuration.processing", SNMP_AGENT_CONFIG.toString()));
		config = Beans.getReference(SNMP_AGENT_CONFIG);
		ConfigurationLoader loader = Beans.getReference(ConfigurationLoader.class);
		loader.load(config);
	}
	
	public <T> void detectAnnotation(@Observes final ProcessAnnotatedType<T> event, final BeanManager beanManager) {
		final AnnotatedType<T> type = event.getAnnotatedType();
		if (type.isAnnotationPresent(MIB.class) && type.isAnnotationPresent(MBean.class)) {
			types.add(event.getAnnotatedType());
		}
	}

	public void onStartup(@Observes final AfterDeploymentValidation event) {
		
		if (config == null) {
			readConfiguration();
		}
		
		if (config.isAgentEnabled()) {
			logger.info(getBundle(BootstrapConstants.BUNDLE_NAME).getString("agent-snmp-startup"));
			agent = Beans.getReference(SNMPAgent.class);

			if (types != null && !types.isEmpty()) {
				
				final MBeanManager manager = Beans.getReference(MBeanManager.class);				
				final List<Class<?>> mibs = new LinkedList<Class<?>>();
				
				for (AnnotatedType<?> type : types) {
					
					final Class<?> clazz = type.getJavaClass();
					logger.debug(getBundle(BootstrapConstants.BUNDLE_NAME).
							getString("agent-snmp-loading-mib", clazz.getName()));
					
					final Object mbean = Beans.getReference(clazz);
					manager.registerMBean(mbean);
					
					mibs.add(clazz);
				}
				agent.assignMIBsList(mibs);
			}
			
			agent.startup();
			started = true;
		}
	}
	
	public void onShutdown(@Observes final BeforeShutdown event) {
		if (started) {
			logger.info(getBundle(BootstrapConstants.BUNDLE_NAME).getString("agent-snmp-shutdown"));
			agent.shutdown();
			agent = null;
		}
	}
	
	protected ResourceBundle getBundle(String resource) {
		if (this.bundle == null) {
			this.bundle = ResourceBundleProducer.create(resource, Locale.getDefault());
		}

		return this.bundle;
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

}
