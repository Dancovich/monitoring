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
package br.gov.frameworkdemoiselle.monitoring.example.trapper;

import javax.inject.Inject;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.monitoring.annotation.JMXQuery;
import br.gov.frameworkdemoiselle.monitoring.annotation.Zabbix;
import br.gov.frameworkdemoiselle.monitoring.annotation.zabbix.HostName;
import br.gov.frameworkdemoiselle.monitoring.annotation.zabbix.ItemKey;
import br.gov.frameworkdemoiselle.monitoring.stereotype.Trapper;

/**
 * @author SERPRO
 */
@Trapper
@Zabbix
//@HostName("Armenia Server")
@HostName("Marumbi Server")
public class MyZabbixTrapper implements IMyTrapper {

	@Inject
	private Logger logger;
	
	@ItemKey("app.failure")
	@Override
	public void sendFailure(String message) {
		logger.debug(String.format("sendFailure('%s')", message));
	}

	@ItemKey("app.database")
	@Override
	public void sendDatabaseInfo(int connections) {
	}

	@ItemKey("app.database[*]")
	@Override
	public void sendDatabaseInfo(String datasource, int connections) {
		logger.debug(String.format("sendDatabaseInfo('%s', %d)", datasource, connections));
	}

	@ItemKey("java.memory[used]")
	@JMXQuery(mbeanName = "java.lang:type=Memory", mbeanAttribute = "HeapMemoryUsage.used")
	@Override
	public void sendUsedHeapMemory() {
		logger.debug("sendHeapMemoryUsed()");
	}

}
