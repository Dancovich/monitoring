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

import br.gov.frameworkdemoiselle.monitoring.annotation.SNMP;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.MIB;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.OID;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.SpecificTrap;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.type.Counter32;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.type.Gauge32;
import br.gov.frameworkdemoiselle.monitoring.stereotype.Trapper;

/**
 * @author SERPRO
 */
@Trapper
@SNMP
@MIB(".1.1")
public class MonitoringSNMPTrapper implements IMonitoringTrapper {

	@OID(".1")
	@SpecificTrap(1)
	public void sendAvailability(
			final String server,
			final String version,
			final long uptime) {
	}

	@OID(".2")
//	@SpecificTrap(2)
	public void sendActiveSessions(
			final String server,
			final @Gauge32 int sessions) {
	}

//	@OID(".3")
	@SpecificTrap(3)
	public void sendHttpRequestData(
			final String server,
			final String uri,
			final @Counter32 long countTotal,
			final @Counter32 long countFail,
			final @Gauge32 long minDuration,
			final @Gauge32 long maxDuration,
			final @Gauge32 long avgDuration) {
	}

/*
2011-08-23 16:46:32 0.0.0.0(via UDP: [127.0.0.1]:39986->[127.0.0.1]) TRAP, SNMP v1, community public
	SNMPv2-SMI::enterprises.35437.1.1.2425.2011.1.1.1 Enterprise Specific Trap (1) Uptime: 0:00:00.14
	SNMPv2-SMI::enterprises.35437.1.1.2425.2011.1.1.1.1.0 = STRING: "server1"
	SNMPv2-SMI::enterprises.35437.1.1.2425.2011.1.1.1.2.0 = STRING: "1.2.3"
	SNMPv2-SMI::enterprises.35437.1.1.2425.2011.1.1.1.3.0 = INTEGER: 12345
2011-08-23 16:46:49 0.0.0.0(via UDP: [127.0.0.1]:60041->[127.0.0.1]) TRAP, SNMP v1, community public
	SNMPv2-SMI::enterprises.35437.1.1.2425.2011.1.1.2 Enterprise Specific Trap (0) Uptime: 0:00:00.19
	SNMPv2-SMI::enterprises.35437.1.1.2425.2011.1.1.2.1.0 = STRING: "server2"
	SNMPv2-SMI::enterprises.35437.1.1.2425.2011.1.1.2.2.0 = Gauge32: 250
2011-08-23 16:46:54 0.0.0.0(via UDP: [127.0.0.1]:44643->[127.0.0.1]) TRAP, SNMP v1, community public
	SNMPv2-SMI::enterprises.35437.1.1.2425.2011.1.1 Enterprise Specific Trap (3) Uptime: 0:00:00.20
	SNMPv2-SMI::enterprises.35437.1.1.2425.2011.1.1.1.0 = STRING: "server1"
	SNMPv2-SMI::enterprises.35437.1.1.2425.2011.1.1.2.0 = STRING: "/url1"
	SNMPv2-SMI::enterprises.35437.1.1.2425.2011.1.1.3.0 = Counter32: 100
	SNMPv2-SMI::enterprises.35437.1.1.2425.2011.1.1.4.0 = Counter32: 2
	SNMPv2-SMI::enterprises.35437.1.1.2425.2011.1.1.5.0 = Gauge32: 10
	SNMPv2-SMI::enterprises.35437.1.1.2425.2011.1.1.6.0 = Gauge32: 60
	SNMPv2-SMI::enterprises.35437.1.1.2425.2011.1.1.7.0 = Gauge32: 30
*/
	
	// outras propostas:
	
//	@OID(".1")
//	@VariableTypes({ OctetString.class, OctetString.class, Counter64.class })
//	public void sendAvailability(final String server, final String version, final long uptime) {
//	}

//	@ManagedTrap(variableBindings = {
//			@ManagedObject(oid = ".1.1.0", type = OctetString.class),
//			@ManagedObject(oid = ".1.2.0", type = OctetString.class),
//			@ManagedObject(oid = ".1.3.0", type = Counter64.class) })
//	public void sendAvailability(final String server, final String version, final long uptime) {
//	}

	/*
	 * @ManagedTrap(specificType = 5, variableBindings = @ManagedObject(oid = ".1", type = OctetString.class)) public
	 * void enviarFalha(final String message) { }
	 * 
	 * @ManagedTrap(specificType = 15, variableBindings = {
	 * @ManagedObject(oid = ".1", type = OctetString.class),
	 * @ManagedObject(oid = ".2", type = Gauge32.class) }) public void enviarInfoBD(final String datasource, final int
	 * connections) { }
	 * 
	 * @ManagedTrap(genericType = TrapType.ENTERPRISE_SPECIFIC, specificType = 12345, variableBindings = {
	 * @ManagedObject(oid = ".1", type = OctetString.class),
	 * @ManagedObject(oid = ".2", type = Integer32.class),
	 * @ManagedObject(oid = ".3", type = Counter32.class) }) public void enviarMensagem(final String message, final int
	 * level, final int value) { }
	 * 
	 * @ManagedTrap(specificType = 2, variableBindings = @ManagedObject(oid = ".1", type = Gauge32.class), mbeanName =
	 * "java.lang:type=Memory", mbeanAttribute = "HeapMemoryUsage.used") public void enviarMemoriaHeapUsada() { }
	 * 
	 * @ManagedTrap(variableBindings = @ManagedObject(oid = ".1", type = OctetString.class), mbeanName =
	 * "br.gov.demoiselle:name=Escola", mbeanAttribute = "VersaoAplicacao") public void enviarVersaoAplicacao() { }
	 */

}
