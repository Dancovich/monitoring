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

import br.gov.frameworkdemoiselle.monitoring.annotation.JMXQuery;
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
@MIB(".1")
public class EscolaTrapper implements IEscolaTrapper {

	@OID(".1")
	@SpecificTrap(5)
	@Override
	public void enviarFalha(String mensagem) {
	}

	@OID(".2")
	@SpecificTrap(15)
	@Override
	public void enviarInfoBD(String base, @Gauge32 int conexoes) {
	}

	@OID(".3")
	@SpecificTrap(2)
	@JMXQuery(mbeanName = "java.lang:type=Memory", mbeanAttribute = "HeapMemoryUsage.used")
	@Gauge32
	@Override
	public void enviarMemoriaHeapUsada() {
	}

	@OID(".4")
	@SpecificTrap(12345)
	@Override
	public void enviarMensagem(String mensagem, int nivel, @Counter32 int valor) {
	}

	@OID(".5")
	@JMXQuery(mbeanName = "br.gov.demoiselle:name=Escola", mbeanAttribute = "VersaoAplicacao")
	@Override
	public void enviarVersaoAplicacao() {
	}
	
}
