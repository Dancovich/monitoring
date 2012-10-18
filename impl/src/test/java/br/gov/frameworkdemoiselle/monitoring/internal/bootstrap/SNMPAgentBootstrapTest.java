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
import static org.junit.Assert.assertEquals;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

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
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.MIB;
import br.gov.frameworkdemoiselle.monitoring.internal.configuration.snmp.SNMPAgentConfig;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.jmx.MBeanManager;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.SNMPAgent;
import br.gov.frameworkdemoiselle.monitoring.stereotype.MBean;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * @author SERPRO
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Beans.class })
@Ignore
public class SNMPAgentBootstrapTest {

	private SNMPAgentBootstrap snmpAgentBootstrap;

	private SNMPAgentConfig snmpAgentConfig;

	private ConfigurationLoader configurationLoader;

	private SNMPAgent snmpAgent;

	private MBeanManager manager;

	private List<AnnotatedType<?>> types = Collections.synchronizedList(new ArrayList<AnnotatedType<?>>());

	@SuppressWarnings("rawtypes")
	private AnnotatedType annotatedType;

	@SuppressWarnings("rawtypes")
	private ProcessAnnotatedType processAnnotatedType;

	@Before
	public void before() {
		this.snmpAgentBootstrap = new SNMPAgentBootstrap();
		this.snmpAgentConfig = new SNMPAgentConfig();
		this.configurationLoader = new ConfigurationLoader();
		Logger logger = LoggerProducer.create(SNMPAgentBootstrapTest.class);
		ResourceBundle bundle = ResourceBundleProducer.create("demoiselle-core-bundle");
		configurationLoader = new ConfigurationLoader();
		Whitebox.setInternalState(this.configurationLoader, "bundle", bundle);
		Whitebox.setInternalState(this.configurationLoader, "logger", logger);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDetectAnnotation() {
		this.annotatedType = EasyMock.createMock(AnnotatedType.class);
		expect(this.annotatedType.isAnnotationPresent(MIB.class)).andReturn(true);
		expect(this.annotatedType.isAnnotationPresent(MBean.class)).andReturn(true);
		replay(this.annotatedType);

		this.processAnnotatedType = EasyMock.createMock(ProcessAnnotatedType.class);
		expect(this.processAnnotatedType.getAnnotatedType()).andReturn(this.annotatedType).times(2);
		replay(this.processAnnotatedType);

		this.snmpAgentBootstrap.detectAnnotation(this.processAnnotatedType, null);

		assertEquals(1, SNMPAgentBootstrap.types.size());
	}

	@Test
	public void testOnStartupWithConfigNullAndAgentEnabled() {
		
		Whitebox.setInternalState(this.snmpAgentConfig, "agentEnabled", true);
		
		mockStatic(Beans.class);
		expect(Beans.getReference(ConfigurationLoader.class)).andReturn(this.configurationLoader);
		expect(Beans.getReference(SNMPAgentBootstrapTest.class)).andReturn(null);
		expect(Beans.getReference(SNMPAgentConfig.class)).andReturn(this.snmpAgentConfig).anyTimes();

		this.manager = EasyMock.createMock(MBeanManager.class);
		expect(this.manager.registerMBean(null)).andReturn(null);
		replay(this.manager);

		expect(Beans.getReference(MBeanManager.class)).andReturn(this.manager);

		this.annotatedType = EasyMock.createMock(AnnotatedType.class);
		expect(this.annotatedType.getJavaClass()).andReturn(this.getClass());
		replay(this.annotatedType);

		this.snmpAgent = EasyMock.createMock(SNMPAgent.class);
		expect(Beans.getReference(SNMPAgent.class)).andReturn(this.snmpAgent);
		this.snmpAgent.assignMIBsList(null);
		this.snmpAgent.startup();

		replayAll();
		this.types.add(this.annotatedType);
		SNMPAgentBootstrap.types = this.types;
		this.snmpAgentBootstrap.onStartup(null);
		verifyAll();
	}

	@Test
	public void testOnShutdown() {
		Whitebox.setInternalState(this.snmpAgentBootstrap, "started", true);
		this.snmpAgent = EasyMock.createMock(SNMPAgent.class);
		this.snmpAgent.shutdown();
		replay(this.snmpAgent);
		Whitebox.setInternalState(this.snmpAgentBootstrap, "agent", this.snmpAgent);
		this.snmpAgentBootstrap.onShutdown(null);
		verify(this.snmpAgent);
	}

}