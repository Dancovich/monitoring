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

/**
 * Holds settings for the <b>Zabbix Trapper</b> (i.e., trapping and passive check).
 * 
 * @author SERPRO
 */
@Configuration(prefix = ConfigurationConstants.DEFAULT_PREFIX + ".zabbix.trapper")
public class ZabbixTrapperConfig {

	public static final int DEFAULT_TRAPPER_PORT = 10051;

	@Name("server")
	private String trapperServer;

	@Name("port")
	private int trapperPort = DEFAULT_TRAPPER_PORT;

	@Name("host")
	private String trapperHost;

	@Name("default_key")
	private String trapperDefaultKey;

	@Name("active_checks")
	private boolean activeChecks = false;

	public String getTrapperServer() {
		return trapperServer;
	}

	public int getTrapperPort() {
		return trapperPort;
	}

	public String getTrapperHost() {
		return trapperHost;
	}

	public String getTrapperDefaultKey() {
		return trapperDefaultKey;
	}

	public boolean isActiveChecks() {
		return activeChecks;
	}

}
