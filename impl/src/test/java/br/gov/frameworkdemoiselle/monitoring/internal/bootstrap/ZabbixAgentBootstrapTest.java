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

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.internal.configuration.ConfigurationLoader;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.monitoring.internal.configuration.zabbix.ZabbixAgentConfig;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.zabbix.ZabbixAgent;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * @author SERPRO
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Beans.class })
@Ignore
public class ZabbixAgentBootstrapTest {

	private ZabbixAgentBootstrap zabbixAgentBootstrap;

	private ZabbixAgentConfig zabbixAgentConfig;

	private ConfigurationLoader configurationLoader;

	private ZabbixAgent zabbixAgent;

	@Before
	public void before() {
		this.zabbixAgentBootstrap = new ZabbixAgentBootstrap();
		this.zabbixAgentConfig = new ZabbixAgentConfig();
		this.configurationLoader = new ConfigurationLoader();
		Logger logger = LoggerProducer.create(ZabbixAgentBootstrapTest.class);
		ResourceBundle bundle = ResourceBundleProducer.create("demoiselle-core-bundle");
		configurationLoader = new ConfigurationLoader();
		Whitebox.setInternalState(this.configurationLoader, "bundle", bundle);
		Whitebox.setInternalState(this.configurationLoader, "logger", logger);
	}

	@Test
	public void testOnStartupWithConfigNullAndAgentDisabled() {
		mockStatic(Beans.class);
		expect(Beans.getReference(ZabbixAgentConfig.class)).andReturn(this.zabbixAgentConfig);
		expect(Beans.getReference(ConfigurationLoader.class)).andReturn(this.configurationLoader);
		replayAll();
		this.zabbixAgentBootstrap.onStartup(null);
		assertFalse(this.zabbixAgentConfig.isAgentEnabled());
	}

	@Test
	public void testOnStartupWithConfigNotNullAndAgentEnabled() {
		Whitebox.setInternalState(this.zabbixAgentBootstrap, "config", this.zabbixAgentConfig);
		Whitebox.setInternalState(this.zabbixAgentConfig, "agentEnabled", true);
		mockStatic(Beans.class);
		this.zabbixAgent = EasyMock.createMock(ZabbixAgent.class);
		expect(Beans.getReference(ZabbixAgent.class)).andReturn(this.zabbixAgent);
		this.zabbixAgent.startup();
		replay(this.zabbixAgent);
		replayAll();
		this.zabbixAgentBootstrap.onStartup(null);
		verify(this.zabbixAgent);
	}

	@Test
	public void testOnShutdown() {
		Whitebox.setInternalState(this.zabbixAgentBootstrap, "started", true);
		this.zabbixAgent = EasyMock.createMock(ZabbixAgent.class);
		this.zabbixAgent.shutdown();
		replay(this.zabbixAgent);
		Whitebox.setInternalState(this.zabbixAgentBootstrap, "agent", this.zabbixAgent);
		this.zabbixAgentBootstrap.onShutdown(null);
		verify(this.zabbixAgent);
	}

}