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
package br.gov.frameworkdemoiselle.monitoring.internal.interceptor;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;

import javax.interceptor.InvocationContext;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.ZabbixTrapperHandler;
import br.gov.frameworkdemoiselle.util.Beans;

/**
 * @author SERPRO
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Beans.class })
@Ignore
public class ZabbixTrapperInterceptorTest {

	private InvocationContext ctx;
	private ZabbixTrapperInterceptor zabbixTrapperInterceptor;
	
	@Before
	public void before() {
		this.ctx = EasyMock.createMock(InvocationContext.class);
		this.zabbixTrapperInterceptor = new ZabbixTrapperInterceptor();
		Whitebox.setInternalState(this.zabbixTrapperInterceptor,"logger", LoggerProducer.create(ZabbixTrapperInterceptor.class));
	}
	
	@Test
	public void testManage() throws Exception {
		mockStatic(Beans.class);
		ZabbixTrapperHandler handler = EasyMock.createMock(ZabbixTrapperHandler.class);
		
		expect(this.ctx.getMethod()).andReturn(ZabbixTrapperInterceptorTest.class.getMethod("testManage"));
		expect(this.ctx.getTarget()).andReturn(this);
		handler.initialize(this.getClass().getSuperclass());
		handler.sendTrap(this.ctx);
		expect(this.ctx.proceed()).andReturn(null);
		
		expect(Beans.getReference(ZabbixTrapperHandler.class)).andReturn(handler);
		
		replay(this.ctx);
		replay(handler);
		replayAll();
		
		this.zabbixTrapperInterceptor.manage(this.ctx);
		verify(this.ctx);
		verify(handler);
	}
	
	@Test
	public void testManageWithHandlerAlreadyExistentForThisClass() throws Exception {
		mockStatic(Beans.class);
		ZabbixTrapperHandler handler = EasyMock.createMock(ZabbixTrapperHandler.class);
		
		expect(this.ctx.getMethod()).andReturn(ZabbixTrapperInterceptorTest.class.getMethod("testManageWithHandlerAlreadyExistentForThisClass")).times(2);
		expect(this.ctx.getTarget()).andReturn(this).times(2);
		handler.initialize(this.getClass().getSuperclass());
		handler.sendTrap(this.ctx);
		handler.sendTrap(this.ctx);
		expect(this.ctx.proceed()).andReturn(null).times(2);
		
		expect(Beans.getReference(ZabbixTrapperHandler.class)).andReturn(handler);
		
		replay(this.ctx);
		replay(handler);
		replayAll();
		
		this.zabbixTrapperInterceptor.manage(this.ctx);
		this.zabbixTrapperInterceptor.manage(this.ctx);
		verify(this.ctx);
		verify(handler);
	}
	
	@Test
	public void testManageThatThrowException() throws Exception {
		mockStatic(Beans.class);
		ZabbixTrapperHandler handler = EasyMock.createMock(ZabbixTrapperHandler.class);
		
		expect(this.ctx.getMethod()).andReturn(ZabbixTrapperInterceptorTest.class.getMethod("testManageThatThrowException"));
		expect(this.ctx.getTarget()).andReturn(this);
		handler.initialize(this.getClass().getSuperclass());
		handler.sendTrap(this.ctx);
		expect(this.ctx.proceed()).andThrow(new Exception());
		
		expect(Beans.getReference(ZabbixTrapperHandler.class)).andReturn(handler);
		
		replay(this.ctx);
		replay(handler);
		replayAll();
		try {
			this.zabbixTrapperInterceptor.manage(this.ctx);
			assertFalse(false);
		}catch (Exception cause){
			assertTrue(true);
		}
		verify(this.ctx);
		verify(handler);
	}
	
}
