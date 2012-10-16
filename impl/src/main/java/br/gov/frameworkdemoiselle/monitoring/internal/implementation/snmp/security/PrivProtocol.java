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

import org.snmp4j.security.Priv3DES;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivAES192;
import org.snmp4j.security.PrivAES256;
import org.snmp4j.security.PrivDES;
import org.snmp4j.smi.OID;

/**
 * Handles the <b>privacy protocol</b> for SNMP (i.e., DES or AES).
 * 
 * @author SERPRO
 */
public enum PrivProtocol {
	
	DES (PrivDES.ID),
	DES3 (Priv3DES.ID),
	AES128 (PrivAES128.ID),
	AES192 (PrivAES192.ID),
	AES256 (PrivAES256.ID);

	private final OID oid;

	private PrivProtocol(OID oid) {
		this.oid = oid;
	}

	public OID getOID() {
		return oid;
	}
	
	public static PrivProtocol parseString(String code) {
		if (code != null && !code.isEmpty()) {
			code = code.toUpperCase();
			if (code.equals("DES")) {
				return DES;
			} else if (code.equals("3DES")) {
				return DES3;
			} else if (code.equals("AES") || code.equals("AES128")) {
				return AES128;
			} else if (code.equals("AES192")) {
				return AES192;
			} else if (code.equals("AES256")) {
				return AES256;
			} else {
				return null;
			}
		}
		return null;
	}

}
