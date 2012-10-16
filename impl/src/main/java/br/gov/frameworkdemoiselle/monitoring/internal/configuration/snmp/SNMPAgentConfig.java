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
 * Holds settings for the <b>SNMP Agent</b> (i.e., polling and active check).
 * 
 * @author SERPRO
 */
@Configuration(prefix = ConfigurationConstants.DEFAULT_PREFIX + ".snmp.agent")
public class SNMPAgentConfig {
	
	public static final String DEFAULT_AGENT_PROTOCOL = "udp";
	public static final int DEFAULT_AGENT_PORT = 1161;
	public static final String DEFAULT_AGENT_MIB_ROOT = "1.3.6.1.4.1.35437";
	public static final String DEFAULT_AGENT_SECURITY = "snmp-security.xml";

	@Name("enabled")
	private boolean agentEnabled = false;

	@Name("protocol")
	private String agentProtocol = DEFAULT_AGENT_PROTOCOL;

	@Name("port")
	private int agentPort = DEFAULT_AGENT_PORT;

	@Name("mib.root")
	private String mibRoot = DEFAULT_AGENT_MIB_ROOT;

	private String security = DEFAULT_AGENT_SECURITY;

	/**
	 * Indicates whether SNMP agent will be enabled.
	 * 
	 * @return	boolean
	 */
	public boolean isAgentEnabled() {
		return agentEnabled;
	}

	/**
	 * Returns the agent protocol (i.e., UDP or TCP).
	 * 
	 * @return	String
	 */
	public String getAgentProtocol() {
		return agentProtocol;
	}

	/**
	 * Returns the agent port (default: 1161).
	 * 
	 * @return	int
	 */
	public int getAgentPort() {
		return agentPort;
	}

	/**
	 * Returns the SNMP MIB root (e.g., "1.3.6.1.4.1.35437").
	 * 
	 * @return	String
	 */
	public String getMibRoot() {
		return mibRoot;
	}

	/**
	 * Returns the SNMP security definitions file (default: "snmp-security.xml").
	 * 
	 * @return	String
	 */
	public String getSecurity() {
		return security;
	}

}
