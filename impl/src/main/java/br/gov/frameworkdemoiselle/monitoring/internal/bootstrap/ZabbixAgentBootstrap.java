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

import java.util.Locale;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeforeShutdown;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.internal.bootstrap.AbstractLifecycleBootstrap;
import br.gov.frameworkdemoiselle.internal.configuration.ConfigurationLoader;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.lifecycle.Startup;
import br.gov.frameworkdemoiselle.monitoring.internal.configuration.zabbix.ZabbixAgentConfig;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.zabbix.ZabbixAgent;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Bootstrap class intented to initialize and start the <b>Zabbix agent</b> automatically
 * on application startup. Moreover, it stops the agent before application shutdown.
 * 
 * @author SERPRO
 */
public class ZabbixAgentBootstrap extends AbstractLifecycleBootstrap<Startup> {

	private static Logger logger = LoggerProducer.create(ZabbixAgentBootstrap.class);

	private static final Class<ZabbixAgentConfig> ZABBIX_AGENT_CONFIG = ZabbixAgentConfig.class;

	private ZabbixAgent agent;
	private ZabbixAgentConfig config;
	
	private boolean started = false;
	
	private ResourceBundle bundle;
	
	private void readConfiguration() {
		getLogger().debug(getBundle().getString(
				"bootstrap.configuration.processing", ZABBIX_AGENT_CONFIG.toString()));
		config = Beans.getReference(ZABBIX_AGENT_CONFIG);
		ConfigurationLoader loader = Beans.getReference(ConfigurationLoader.class);
		loader.load(config);
	}
	
	public void onStartup(@Observes final AfterDeploymentValidation event) {
		if (config == null) {
			readConfiguration();
		}
		if (config.isAgentEnabled()) {
			logger.info(getBundle().getString("agent-zabbix-startup"));
			agent = Beans.getReference(ZabbixAgent.class);
			agent.startup();
			started = true;
		}
	}
	
	public void onShutdown(@Observes final BeforeShutdown event) {
		if (started) {
			logger.info(getBundle().getString("agent-zabbix-shutdown"));
			agent.shutdown();
			agent = null;
		}
	}
	
	@Override
	protected ResourceBundle getBundle() {
		if (this.bundle == null) {
			this.bundle = ResourceBundleProducer.create(BootstrapConstants.BUNDLE_NAME, Locale.getDefault());
		}

		return this.bundle;
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

}