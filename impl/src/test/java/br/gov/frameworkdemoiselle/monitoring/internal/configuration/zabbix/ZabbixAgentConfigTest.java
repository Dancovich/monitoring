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
package br.gov.frameworkdemoiselle.monitoring.internal.configuration.zabbix;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.mockStatic;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.internal.bootstrap.CoreBootstrap;
import br.gov.frameworkdemoiselle.internal.configuration.ConfigurationLoader;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.zabbix.ProtocolVersion;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * @author SERPRO
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(CoreBootstrap.class)
public class ZabbixAgentConfigTest {

	private ZabbixAgentConfig config;
	
	@Before
	public void setUp() throws Exception {
		config = new ZabbixAgentConfig();
		
		ConfigurationLoader loader = new ConfigurationLoader();
		Logger logger = PowerMock.createMock(Logger.class);
		ResourceBundle bundle = ResourceBundleProducer.create("demoiselle-core-bundle");
		
		Whitebox.setInternalState(loader, "bundle", bundle);
		Whitebox.setInternalState(loader, "logger", logger);
		
		CoreBootstrap bootstrap = EasyMock.createMock(CoreBootstrap.class);
		expect(bootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		EasyMock.replay(bootstrap);
		mockStatic(Beans.class);
		expect(Beans.getReference(CoreBootstrap.class)).andReturn(bootstrap);
		PowerMock.replay(Beans.class);

		loader.load(config);
	}

	@After
	public void tearDown() throws Exception {
		config = null;
	}

	@Test
	public void testIsAgentEnabled() {
		assertTrue(config.isAgentEnabled());
	}

	@Test
	public void testGetAgentPort() {
		assertEquals(10052, config.getAgentPort());
	}

	@Test
	public void testGetListenAddress() {
		assertEquals("*", config.getListenAddress());
	}

	@Test
	public void testGetProtocol() {
		assertEquals(ProtocolVersion.V1_4, config.getProtocol());
	}

}
