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
package br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Ignore;
import org.junit.Test;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;

/**
 * @author SERPRO
 */
@Ignore
public class SNMPSecurityTest {

	@Test
	public void testAuthProtocol() {
		AuthProtocol md5 = AuthProtocol.parseString("md5");
		assertEquals(AuthProtocol.MD5, md5);
		AuthProtocol sha = AuthProtocol.parseString("sha");
		assertEquals(AuthProtocol.SHA, sha);
		AuthProtocol other = AuthProtocol.parseString("other");
		assertNull(other);
	}

	@Test
	public void testPrivProtocol() {
		PrivProtocol des = PrivProtocol.parseString("des");
		assertEquals(PrivProtocol.DES, des);
		PrivProtocol des3 = PrivProtocol.parseString("3des");
		assertEquals(PrivProtocol.DES3, des3);
		PrivProtocol aes128 = PrivProtocol.parseString("aes128");
		assertEquals(PrivProtocol.AES128, aes128);
		PrivProtocol aes192 = PrivProtocol.parseString("aes192");
		assertEquals(PrivProtocol.AES192, aes192);
		PrivProtocol aes256 = PrivProtocol.parseString("aes256");
		assertEquals(PrivProtocol.AES256, aes256);
	}

	@Test
	public void testMatchType() {
		assertEquals(MatchType.EXACT, MatchType.parseBoolean(true));
		assertEquals(MatchType.PREFIX, MatchType.parseBoolean(false));
	}

	@Test
	public void testSecLevel() {
		assertEquals(SecLevel.AUTH_PRIV, SecLevel.parsePair(true, true));
		assertEquals(SecLevel.NOAUTH_NOPRIV, SecLevel.parsePair(false, false));
		assertEquals(SecLevel.AUTH_NOPRIV, SecLevel.parsePair(true, false));
		assertNull(SecLevel.parsePair(false, true));
	}

	@Test
	public void testSecModel() {
		assertEquals(SecModel.USM, SecModel.parseString("usm"));
		assertEquals(SecModel.SNMPv1, SecModel.parseString("snmpv1"));
		assertEquals(SecModel.SNMPv2c, SecModel.parseString("snmpv2c"));
		assertEquals(SecModel.ANY, SecModel.parseString("any"));
		assertNull(SecModel.parseString("other"));
	}

	@Test
	public void testAccessEntry() {
		AccessEntry access = new AccessEntry("", "", false, false, false);
		access.setContext(new OctetString("context"));
		access.setModel(SecModel.SNMPv2c);
		access.setLevel(SecLevel.AUTH_PRIV);
		access.setMatch(MatchType.EXACT);
		assertEquals(new OctetString("context"), access.getContext());
		assertEquals(SecModel.SNMPv2c, access.getModel());
		assertEquals(SecLevel.AUTH_PRIV, access.getLevel());
		assertEquals(MatchType.EXACT, access.getMatch());
		assertNotNull(access.toString());
	}

	@Test
	public void testGroupEntry() {
		GroupEntry group = new GroupEntry("");
		group.setName(new OctetString("group"));
		group.setUsers(null);
		assertEquals(new OctetString("group"), group.getName());
		assertNotNull(group.toString());
	}

	@Test
	public void testUserEntry() {
		UserEntry user = new UserEntry("", "");
		user.setName(new OctetString("user"));
		user.setModel(SecModel.SNMPv1);
		assertEquals(new OctetString("user"), user.getName());
		assertEquals(SecModel.SNMPv1, user.getModel());
		assertNotNull(user.toString());
	}

	@Test
	public void testViewTree() {
		ViewTree view = new ViewTree(null, null);
		view.setSubtree(new OID("1.2.3"));
		view.setMask(new OctetString("abc"));
		assertEquals(new OID("1.2.3"), view.getSubtree());
		assertEquals(new OctetString("abc"), view.getMask());
		assertNotNull(view.toString());
	}

	@Test
	public void testViewTreeFamily() {
		ViewTreeFamily family = new ViewTreeFamily("");
		family.setViewName(new OctetString("view"));
		family.setIncludes(null);
		family.setExcludes(null);
		assertEquals(null, family.getIncludes());
		assertEquals(null, family.getExcludes());
		assertNotNull(family.toString());
	}
	
}
