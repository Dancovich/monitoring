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

import org.snmp4j.security.SecurityModel;

/**
 * Handles the <b>security model</b> for SNMP (i.e., any, SNMPv1, SNMPv2c or USM).
 * 
 * @author SERPRO
 */
public enum SecModel {

	ANY (SecurityModel.SECURITY_MODEL_ANY),
	SNMPv1 (SecurityModel.SECURITY_MODEL_SNMPv1),
	SNMPv2c (SecurityModel.SECURITY_MODEL_SNMPv2c),
	USM (SecurityModel.SECURITY_MODEL_USM);
	
	private final int id;

	private SecModel(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static SecModel parseString(String code) {
		if (code != null && !code.isEmpty()) {
			if (code.toUpperCase().equals("USM")) {
				return USM;
			} else if (code.toUpperCase().equals("SNMPV1")) {
				return SNMPv1;
			} else if (code.toUpperCase().equals("SNMPV2C")) {
				return SNMPv2c;
			} else if (code.toUpperCase().equals("ANY")) {
				return ANY;
			} else {
				return null;
			}
		}
		return null;
	}

}
