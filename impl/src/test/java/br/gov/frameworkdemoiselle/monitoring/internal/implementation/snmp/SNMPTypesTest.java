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
package br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp;

import static br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.SNMPTypes.*;

import java.lang.annotation.Annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.snmp4j.agent.MOAccess;
import org.snmp4j.smi.Gauge32;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OctetString;

import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.access.ReadOnly;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.access.ReadWrite;

/**
 * @author SERPRO
 */
public class SNMPTypesTest {

	@Test
	public void testRetrieveDataTypeByAnnotations() {
		Annotation[] anns = {
			new Annotation() {
				@Override
				public Class<? extends Annotation> annotationType() {
					return br.gov.frameworkdemoiselle.monitoring.annotation.snmp.type.Integer32.class;
				}
			}
		};
		assertEquals(Integer32.class, retrieveDataType(anns, null));
		Annotation[] anns2 = {
			new Annotation() {
				@Override
				public Class<? extends Annotation> annotationType() {
					return br.gov.frameworkdemoiselle.monitoring.annotation.snmp.type.Gauge32.class;
				}
			}
		};
		assertEquals(Gauge32.class, retrieveDataType(anns2, null));
	}

	@Test
	public void testRetrieveDataTypeByObject() {
		assertEquals(Integer32.class, retrieveDataType(null, new Byte((byte) 1)));
		assertEquals(Integer32.class, retrieveDataType(null, new Short((short) 10)));
		assertEquals(Integer32.class, retrieveDataType(null, new Integer(100)));
		assertEquals(Integer32.class, retrieveDataType(null, new Long(1000L)));
		assertEquals(OctetString.class, retrieveDataType(null, "abc"));
		assertNull(retrieveDataType(null, new Boolean(true)));
	}

	@Test
	public void testRetrieveDataTypeByType() {
		assertEquals(Integer32.class, retrieveDataType(null, Byte.TYPE));
		assertEquals(Integer32.class, retrieveDataType(null, Short.TYPE));
		assertEquals(Integer32.class, retrieveDataType(null, Integer.TYPE));
		assertEquals(Integer32.class, retrieveDataType(null, Long.TYPE));
		assertNull(retrieveDataType(null, Boolean.TYPE));
	}
	
	@Test
	public void testRetrieveAccessTypeReadOnly() {
		MOAccess access = retrieveAccessType(ReadOnly.class);
		assertFalse(access.isAccessibleForCreate());
		assertTrue(access.isAccessibleForNotify());
		assertTrue(access.isAccessibleForRead());
		assertFalse(access.isAccessibleForWrite());
	}

	@Test
	public void testRetrieveAccessTypeReadWrite() {
		MOAccess access = retrieveAccessType(ReadWrite.class);
		assertFalse(access.isAccessibleForCreate());
		assertTrue(access.isAccessibleForNotify());
		assertTrue(access.isAccessibleForRead());
		assertTrue(access.isAccessibleForWrite());
	}

}
