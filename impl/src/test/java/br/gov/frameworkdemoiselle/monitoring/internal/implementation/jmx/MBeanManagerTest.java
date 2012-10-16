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
package br.gov.frameworkdemoiselle.monitoring.internal.implementation.jmx;

import static org.junit.Assert.*;

import javax.inject.Inject;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.frameworkdemoiselle.junit.DemoiselleRunner;
import br.gov.frameworkdemoiselle.monitoring.exception.MBeanException;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.jmx.mbean.Dummy;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.jmx.mbean.Named;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.jmx.mbean.NotMBean;

/**
 * @author SERPRO
 */
@RunWith(DemoiselleRunner.class)
public class MBeanManagerTest {

	@Inject
	private MBeanManager manager;
	
	private final String DUMMY_REGISTERED_NAME = Dummy.class.getPackage().getName() +
													":name=" + Dummy.class.getSimpleName();
	
	private final String NAMED_REGISTERED_NAME = "br.gov.frameworkdemoiselle.monitoring:name=Named";
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRegisterMBean() throws MalformedObjectNameException, NullPointerException {
		ObjectInstance object = manager.registerMBean(new Dummy());
		assertNotNull(object);
		assertEquals(Dummy.class.getName(), object.getClassName());
		assertEquals(new ObjectName(DUMMY_REGISTERED_NAME), object.getObjectName());
		manager.unregisterMBean(DUMMY_REGISTERED_NAME);
	}

	@Test(expected = MBeanException.class)
	public void testRegisterNotMBean() {
		manager.registerMBean(new NotMBean());
	}

	@Test
	public void testRegisterNamedMBean() throws MalformedObjectNameException, NullPointerException {
		ObjectInstance object = manager.registerMBean(new Named());
		assertNotNull(object);
		assertEquals(Named.class.getName(), object.getClassName());
		assertEquals(new ObjectName(NAMED_REGISTERED_NAME), object.getObjectName());
		manager.unregisterMBean(NAMED_REGISTERED_NAME);
	}

	@Test
	public void testUnregisterMBean() {
		manager.registerMBean(new Dummy());
		manager.unregisterMBean(DUMMY_REGISTERED_NAME);
	}

	@Test
	public void testUnregisterMBeans() {
		assertNotNull(manager.mbeans);
		int count = manager.mbeans.size();
		manager.registerMBean(new Dummy());
		manager.registerMBean(new Named());
		assertEquals(count + 2, manager.mbeans.size());
		manager.unregisterMBeans();
		assertEquals(0, manager.mbeans.size());
	}

	@Test
	public void testGetMBeanInstanceByName() {
		Dummy dummy = new Dummy();
		manager.registerMBean(dummy);
		assertEquals(dummy, manager.getMBeanInstance(DUMMY_REGISTERED_NAME).getMBeanObject());
		Named named = new Named();
		manager.registerMBean(named);
		assertEquals(named, manager.getMBeanInstance(NAMED_REGISTERED_NAME).getMBeanObject());
		manager.unregisterMBeans();
	}

	@Test
	public void testGetMBeanInstanceByNameNull() {
		assertNull(manager.getMBeanInstance((String) null));
	}

	@Test
	public void testGetMBeanInstanceByClass() {
		Dummy dummy = new Dummy();
		manager.registerMBean(dummy);
		assertEquals(dummy, manager.getMBeanInstance(Dummy.class).getMBeanObject());
		Named named = new Named();
		manager.registerMBean(named);
		assertEquals(named, manager.getMBeanInstance(Named.class).getMBeanObject());
		manager.unregisterMBeans();
	}

	@Test
	public void testGetMBeanInstanceByClassNull() {
		assertNull(manager.getMBeanInstance((Class<?>) null));
	}

	@Test
	public void testGetObjectName() throws MalformedObjectNameException, NullPointerException {
		Dummy dummy = new Dummy();
		manager.registerMBean(dummy);
		assertEquals(new ObjectName(DUMMY_REGISTERED_NAME), manager.getObjectName(DUMMY_REGISTERED_NAME));
		Named named = new Named();
		manager.registerMBean(named);
		assertEquals(new ObjectName(NAMED_REGISTERED_NAME), manager.getObjectName(NAMED_REGISTERED_NAME));
		manager.unregisterMBeans();
	}

}
