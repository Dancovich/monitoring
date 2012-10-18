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

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.frameworkdemoiselle.junit.DemoiselleRunner;
import br.gov.frameworkdemoiselle.monitoring.annotation.SNMP;
import br.gov.frameworkdemoiselle.monitoring.annotation.Zabbix;
import br.gov.frameworkdemoiselle.monitoring.trapping.SimpleTrapper;

/**
 * @author SERPRO
 */
@RunWith(DemoiselleRunner.class)
@Ignore
public class TrapperExecution {

	@Inject
	@SNMP
	private IMyTrapper snmpTrapper;

	@Inject
	@Zabbix
	private IMyTrapper zabbixTrapper;

	@Inject
	private IMySecondTrapper secondTrapper;

	@Inject
	@Zabbix
	private SimpleTrapper zabbix;

	@Inject
	@SNMP
	private SimpleTrapper snmp;

	@Inject
	@SNMP
	@Zabbix
	private ZabbixSNMPExecution both;
	
	@Test
	public void sendFailureMessage() {
		snmpTrapper.sendFailure("message to snmpd");
		zabbixTrapper.sendFailure("message to zabbixd");
		secondTrapper.sendFailure("message to default");

		zabbixTrapper.sendDatabaseInfo(50);
		
		zabbixTrapper.sendDatabaseInfo("db1", 100);
		zabbixTrapper.sendUsedHeapMemory();
	}

	@Test
	public void simpleTrapping() {
		// key specified programatically
		zabbix.send("app.quote", "You say yes. I say no!");
		snmp.send("1.3.6.1.4.1.35437.1.1.2425.2011.1.1", "You say yes. I say no!");
		
		// key not specified
		zabbix.send("Let it be. Let it be!");
		snmp.send("Let it be. Let it be!");
	}

	@Test
	public void bothTrappers() {
		both.sendAvailability("2.1.3", 14400);
	}

}
