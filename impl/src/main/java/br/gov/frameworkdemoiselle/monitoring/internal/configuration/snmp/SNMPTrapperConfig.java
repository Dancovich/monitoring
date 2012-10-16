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

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.configuration.Configuration;
import br.gov.frameworkdemoiselle.monitoring.internal.configuration.ConfigurationConstants;

/**
 * Holds settings for the <b>SNMP Trapper</b> (i.e., trapping and passive check).
 * 
 * @author SERPRO
 */
@Configuration(prefix = ConfigurationConstants.DEFAULT_PREFIX + ".snmp.trapper")
public class SNMPTrapperConfig {

	public static final String DEFAULT_PROTOCOL = "udp";
	public static final int DEFAULT_PORT = 162;
	public static final String DEFAULT_COMMUNITY = "public";
	public static final String DEFAULT_SNMP_TRAP_VERSION = "v1";
	public static final String DEFAULT_ENTERPRISE_OID = "1.3.6.1.4.1.35437";

	@Name("protocol")
	private String trapperProtocol = DEFAULT_PROTOCOL;

	@Name("server")
	private String trapperServer;

	@Name("port")
	private int trapperPort = DEFAULT_PORT;

	@Name("community")
	private String trapperCommunity = DEFAULT_COMMUNITY;

	@Name("version")
	private String trapperVersion = DEFAULT_SNMP_TRAP_VERSION;

	@Name("enterprise")
	private String trapperEnterprise = DEFAULT_ENTERPRISE_OID;

	/**
	 * Returns the trapper protocol (i.e., UDP or TCP).
	 * 
	 * @return	String
	 */
	public String getTrapperProtocol() {
		return trapperProtocol;
	}

	/**
	 * Returns the trapper server.
	 * 
	 * @return	String
	 */
	public String getTrapperServer() {
		return trapperServer;
	}

	/**
	 * Returns the trapper port (default: 162).
	 * 
	 * @return int
	 */
	public int getTrapperPort() {
		return trapperPort;
	}

	/**
	 * Returns the trapper community.
	 * 
	 * @return	String
	 */
	public String getTrapperCommunity() {
		return trapperCommunity;
	}

	/**
	 * Returns the trapper SNMP version (default: "v1").
	 * 
	 * @return	String
	 */
	public String getTrapperVersion() {
		return trapperVersion;
	}

	/**
	 * Returns the trapper enterprise OID (default: "1.3.6.1.4.1.35437").
	 * 
	 * @return	String
	 */
	public String getTrapperEnterprise() {
		return trapperEnterprise;
	}

}
