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
package br.gov.frameworkdemoiselle.monitoring.internal.configuration.zabbix;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.configuration.Configuration;
import br.gov.frameworkdemoiselle.monitoring.internal.configuration.ConfigurationConstants;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.zabbix.ProtocolVersion;

/**
 * Holds settings for the <b>Zabbix Agent</b> (i.e, polling and active check).
 * 
 * @author SERPRO
 */
@Configuration(prefix = ConfigurationConstants.DEFAULT_PREFIX + ".zabbix.agent")
public class ZabbixAgentConfig {

	public static final int DEFAULT_AGENT_PORT = 10052;
	public static final String DEFAULT_AGENT_ADDRESS = "*";
	public static final String DEFAULT_AGENT_PROTOCOL = "1.4";

	@Name("enabled")
	private boolean agentEnabled = false;

	@Name("port")
	private int agentPort = DEFAULT_AGENT_PORT;

	@Name("address")
	private String listenAddress = DEFAULT_AGENT_ADDRESS;

	@Name("protocol")
	private String protocolVersion = DEFAULT_AGENT_PROTOCOL;

	/**
	 * Indicates whether Zabbix agent will be enabled.
	 * 
	 * @return	boolean
	 */
	public boolean isAgentEnabled() {
		return agentEnabled;
	}

	/**
	 * Returns the agent port (default: 10052).
	 * 
	 * @return	int
	 */
	public int getAgentPort() {
		return agentPort;
	}

	/**
	 * Returns the IP address the agent will be listening on (default: anyone).
	 * 
	 * @return String
	 */
	public String getListenAddress() {
		return listenAddress;
	}

	/**
	 * Returns the Zabbix internal protocol to be considered.
	 * 
	 * @return	ProtocolVersion
	 */
	public ProtocolVersion getProtocol() {
		return ProtocolVersion.parseString(this.protocolVersion);
	}

}
