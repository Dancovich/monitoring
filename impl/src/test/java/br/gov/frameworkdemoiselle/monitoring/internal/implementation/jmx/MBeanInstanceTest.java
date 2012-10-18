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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author SERPRO
 */
@Ignore
public class MBeanInstanceTest {

	private ObjectInstance oi; 
	
	@Before
	public void setUp() throws Exception {
		oi = new ObjectInstance("test:type=Test", String.class.getName());
	}

	@After
	public void tearDown() throws Exception {
		oi = null;
	}

	@Test
	public void testGetMBeanClass() {
		MBeanInstance inst = new MBeanInstance(new String(), null);
		assertEquals(String.class, inst.getMBeanClass());
	}

	@Test
	public void testGetMBeanClassNull() {
		MBeanInstance inst = new MBeanInstance(null, null);
		assertNull(inst.getMBeanClass());
	}

	@Test
	public void testGetMBeanObject() throws MalformedObjectNameException {
		String obj = "abc";
		MBeanInstance inst = new MBeanInstance(obj, oi);
		assertNotNull(inst.getMBeanObject());
		assertEquals(obj, inst.getMBeanObject());
	}

	@Test
	public void testGetObjectInstance() throws MalformedObjectNameException {
		MBeanInstance inst = new MBeanInstance("abc", oi);
		assertNotNull(inst.getObjectInstance());
		assertEquals(oi, inst.getObjectInstance());
	}

	@Test
	public void testGetObjectName() throws MalformedObjectNameException {
		MBeanInstance inst = new MBeanInstance("abc", oi);
		assertEquals(new ObjectName("test:type=Test"), inst.getObjectName());
	}

	@Test
	public void testGetObjectNameNull() throws MalformedObjectNameException {
		MBeanInstance inst = new MBeanInstance("abc", null);
		assertNull(inst.getObjectName());
	}

}
