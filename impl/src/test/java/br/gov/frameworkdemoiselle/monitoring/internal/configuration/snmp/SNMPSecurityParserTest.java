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
package br.gov.frameworkdemoiselle.monitoring.internal.configuration.snmp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.inject.Inject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;

import br.gov.frameworkdemoiselle.junit.DemoiselleRunner;
import br.gov.frameworkdemoiselle.monitoring.exception.ConfigException;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security.AccessEntry;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security.AuthProtocol;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security.GroupEntry;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security.MatchType;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security.PrivProtocol;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security.SecLevel;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security.SecModel;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security.UserEntry;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security.ViewTree;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security.ViewTreeFamily;

/**
 * @author SERPRO
 */
@RunWith(DemoiselleRunner.class)
public class SNMPSecurityParserTest {

	@Inject
	private SNMPSecurityParser parser;
	
	@Before
	public void setUp() throws Exception {
		parser.parseDocument("snmp-security.xml");
	}

	@After
	public void tearDown() throws Exception {
		parser.releaseResources();
		parser = null;
	}

	@Test
	public void testGetUsersList() {
		List<UsmUser> users = parser.getUsersList();
		assertNotNull(users);
		assertEquals(5, users.size());
		
		UsmUser user = users.get(0);
		assertEquals(new OctetString("SHADES"), user.getSecurityName());
		assertEquals(AuthProtocol.parseString("SHA").getOID(), user.getAuthenticationProtocol());
		assertEquals(new OctetString("SHADESAuthPassword"), user.getAuthenticationPassphrase());
		assertEquals(PrivProtocol.parseString("DES").getOID(), user.getPrivacyProtocol());
		assertEquals(new OctetString("SHADESPrivPassword"), user.getPrivacyPassphrase());
	}

	@Test
	public void testGetViewsList() {
		List<ViewTreeFamily> views = parser.getViewsList();
		assertNotNull(views);
		assertEquals(12, views.size());
		
		ViewTreeFamily view = views.get(0);
		assertEquals(new OctetString("fullReadView"), view.getViewName());
		assertNotNull(view.getIncludes());
		assertEquals(1, view.getIncludes().size());
		
		ViewTree tree = view.getIncludes().get(0);
		assertNotNull(tree);
		assertEquals(new OID("1.3"), tree.getSubtree());
		assertEquals(new OctetString(), tree.getMask());
		assertNotNull(view.getExcludes());
		assertEquals(0, view.getExcludes().size());
	}

	@Test
	public void testGetGroupsList() {
		List<GroupEntry> groups = parser.getGroupsList();
		assertNotNull(groups);
		assertEquals(5, groups.size());
		
		GroupEntry group = groups.get(0);
		assertNotNull(group);
		assertEquals(new OctetString("v1v2group"), group.getName());
		
		List<UserEntry> users = group.getUsers();
		assertNotNull(users);
		assertEquals(2, users.size());
		UserEntry user = users.get(0);
		assertEquals(new OctetString("public"), user.getName());
		assertEquals(SecModel.parseString("SNMPv1"), user.getModel());
		
		AccessEntry access = group.getAccess();
		assertNotNull(access);
		assertEquals(new OctetString(), access.getContext());
		assertEquals(SecModel.parseString("ANY"), access.getModel());
		assertEquals(SecLevel.NOAUTH_NOPRIV, access.getLevel());
		assertEquals(MatchType.EXACT, access.getMatch());
		assertEquals(new OctetString("fullReadView"), access.getReadView());
		assertEquals(new OctetString("fullWriteView"), access.getWriteView());
		assertEquals(new OctetString("fullNotifyView"), access.getNotifyView());
	}

	@Test(expected = ConfigException.class)
	public void testParseEmptyFile() {
		parser.parseDocument("");
	}

}
