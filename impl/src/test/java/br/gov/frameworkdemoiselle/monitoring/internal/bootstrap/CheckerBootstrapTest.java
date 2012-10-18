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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import br.gov.frameworkdemoiselle.monitoring.internal.implementation.CheckerHandler;
import br.gov.frameworkdemoiselle.monitoring.stereotype.Checker;
import br.gov.frameworkdemoiselle.util.Beans;

/**
 * @author SERPRO
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ Beans.class })
@Ignore
public class CheckerBootstrapTest {

	@SuppressWarnings("rawtypes")
	private ProcessAnnotatedType event;

	private BeanManager beanManager;

	@SuppressWarnings("rawtypes")
	private AnnotatedType annotatedType;

	private List<AnnotatedType<?>> types;

	private CheckerBootstrap checkerBootstrap;

	@Before
	public void before() {
		this.event = EasyMock.createMock(ProcessAnnotatedType.class);
		this.annotatedType = EasyMock.createMock(AnnotatedType.class);
		this.beanManager = null;
		this.types = Collections.synchronizedList(new ArrayList<AnnotatedType<?>>());
		this.checkerBootstrap = new CheckerBootstrap();
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testDetectAnnotation() throws IllegalArgumentException, IllegalAccessException {
		expect(this.annotatedType.isAnnotationPresent(Checker.class)).andReturn(true);
		expect(this.event.getAnnotatedType()).andReturn(this.annotatedType).times(2);
		replay(this.annotatedType, this.event);
		this.checkerBootstrap.detectAnnotation(this.event, this.beanManager);
		verify(this.annotatedType, this.event);
		assertEquals(1, getActions(this.checkerBootstrap).size());
	}

	@Test
	public void testStartCheckers() {
		mockStatic(Beans.class);
		CheckerHandler handler = EasyMock.createMock(CheckerHandler.class);
		expect(Beans.getReference(CheckerHandler.class)).andReturn(handler);
		expect(this.annotatedType.getJavaClass()).andReturn(this.getClass()).times(2);
		expect(Beans.getReference(CheckerBootstrapTest.class)).andReturn(this);
		this.types.add(this.annotatedType);
		replay(this.annotatedType);
		replayAll();
		CheckerBootstrap.types = this.types;
		this.checkerBootstrap.startCheckers(null);
		verify(this.annotatedType);
		assertEquals(1, CheckerBootstrap.handlers.size());
	}

	@Test
	public void testShuttingDown() {
		CheckerHandler handler = EasyMock.createMock(CheckerHandler.class);
		handler.stop();
		replay(handler);
		if (CheckerBootstrap.handlers.size() > 0)
			CheckerBootstrap.handlers.remove(0);
		CheckerBootstrap.handlers.add(handler);
		this.checkerBootstrap.shuttingDown(null);
		verify(handler);
	}

	@SuppressWarnings("unchecked")
	private List<AnnotatedType<?>> getActions(CheckerBootstrap checkerBootstrap) throws IllegalArgumentException, IllegalAccessException {
		Set<Field> fields = Whitebox.getAllStaticFields(CheckerBootstrap.class);
		List<AnnotatedType<?>> list = null;
		for (Field field : fields) {
			if (field.getName().equals("types")) {
				list = (List<AnnotatedType<?>>) field.get(checkerBootstrap);
			}
		}
		return list;
	}

}
