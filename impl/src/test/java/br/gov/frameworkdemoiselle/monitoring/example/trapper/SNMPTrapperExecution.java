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

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.frameworkdemoiselle.junit.DemoiselleRunner;
import br.gov.frameworkdemoiselle.monitoring.annotation.SNMP;

/**
 * @author SERPRO
 */
@RunWith(DemoiselleRunner.class)
public class SNMPTrapperExecution {

	@Inject
	@SNMP
	private IMonitoringTrapper trapper;

	@Test
	public void sendAvailability() {
		trapper.sendAvailability("server1", "1.2.3", 12345L);
		trapper.sendAvailability("server2", "2.0.1", 45678L);
	}
	
	@Test
	public void sendActiveSessions() {
		trapper.sendActiveSessions("server1", 120);
		trapper.sendActiveSessions("server2", 250);
	}
	
	@Test
	public void sendHttpRequestData() {
		trapper.sendHttpRequestData("server1", "/url1", 100, 2, 10, 60, 30);
		trapper.sendHttpRequestData("server1", "/url2", 10, 0, 1, 6, 3);
		trapper.sendHttpRequestData("server2", "/url1", 200, 4, 20, 120, 60);
		trapper.sendHttpRequestData("server2", "/url2", 20, 5, 2, 12, 6);
	}
	
	@AfterClass
	public static void waitForMessages() {
		try {
			Thread.sleep(10000L);
		} catch (InterruptedException e) {
		}
	}
	
}
