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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.frameworkdemoiselle.junit.DemoiselleRunner;
import br.gov.frameworkdemoiselle.monitoring.exception.MBeanException;

/**
 * @author SERPRO
 */
@RunWith(DemoiselleRunner.class)
@Ignore
public class MBeanHelperTest {

	@Test
	public void testGetMBeanServer() {
		MBeanServer server = MBeanHelper.getMBeanServer();
		assertNotNull(server);
		assertTrue(server.getMBeanCount() > 0);
	}

	@Test(expected = MBeanException.class)
	public void testRegisterInvalidObject() {
		MBeanHelper.register(new Object(), "dummy");
	}

	@Test(expected = MBeanException.class)
	public void testUnregisterInvalidObject() {
		ObjectName name = null;
		try {
			name = new ObjectName("dummy");
		} catch (Exception e) {
		}
		MBeanHelper.unregister(name);
	}

	@Test
	public void testQueryLoad() {
		Double load = (Double) MBeanHelper.query("java.lang:type=OperatingSystem", "SystemLoadAverage");
		assertNotNull(load);
		assertTrue(load > 0.0);
	}

	@Test
	public void testQueryOS() {
		String name = (String) MBeanHelper.query("java.lang:type=OperatingSystem", "Name");
		assertNotNull(name);
		String version = (String) MBeanHelper.query("java.lang:type=OperatingSystem", "Version");
		assertNotNull(version);
	}

	@Test
	public void testQueryJVM() {
		String name = (String) MBeanHelper.query("java.lang:type=Runtime", "VmName");
		assertNotNull(name);
		String version = (String) MBeanHelper.query("java.lang:type=Runtime", "VmVersion");
		assertNotNull(version);
	}

	@Test
	public void testQueryClassLoader() {
		Integer loaded = (Integer) MBeanHelper.query("java.lang:type=ClassLoading", "LoadedClassCount");
		assertNotNull(loaded);
		Long unloaded = (Long) MBeanHelper.query("java.lang:type=ClassLoading", "UnloadedClassCount");
		assertNotNull(unloaded);
	}

	@Test
	public void testQueryHeapMemory() {
		Long init = (Long) MBeanHelper.query("java.lang:type=Memory", "HeapMemoryUsage.init");
		assertNotNull(init);
		assertTrue(init > 0);
		Long used = (Long) MBeanHelper.query("java.lang:type=Memory", "HeapMemoryUsage.used");
		assertNotNull(used);
		assertTrue(used > 0);
	}

	@Test(expected = MBeanException.class)
	public void testQueryInvalidObject() {
		MBeanHelper.query("dummy", "dummy");
	}

	@Test(expected = MBeanException.class)
	public void testQueryInvalidSimpleAttribute() {
		MBeanHelper.query("java.lang:type=OperatingSystem", "dummy");
	}

	@Test(expected = MBeanException.class)
	public void testQueryInvalidCompositeAttribute() {
		MBeanHelper.query("java.lang:type=Memory", "HeapMemoryUsage.dummy");
	}

}
