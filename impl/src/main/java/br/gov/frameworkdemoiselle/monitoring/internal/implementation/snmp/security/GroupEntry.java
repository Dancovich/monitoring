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

import java.util.ArrayList;
import java.util.List;

import org.snmp4j.smi.OctetString;

/**
 * Holds information for a single <b>group entry</b>.
 * 
 * @author SERPRO
 */
public class GroupEntry {

	private OctetString name;
	private List<UserEntry> users;
	private AccessEntry access;

	public GroupEntry(String name) {
		this.name = new OctetString(name);
		this.users = new ArrayList<UserEntry>();
	}

	public OctetString getName() {
		return name;
	}

	public void setName(OctetString name) {
		this.name = name;
	}

	public List<UserEntry> getUsers() {
		return users;
	}

	public void setUsers(List<UserEntry> users) {
		this.users = users;
	}

	public AccessEntry getAccess() {
		return access;
	}

	public void setAccess(AccessEntry access) {
		this.access = access;
	}

	public String toString() {
		return "GroupEntry [name=" + name + ", users=" + users + ", access=" + access + "]";
	}
	
}
