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
package br.gov.frameworkdemoiselle.monitoring.example.mbean;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.AllowedValues;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.MIB;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.ModuleName;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.OID;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.TextualConvention;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.access.ReadWrite;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.type.Counter32;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.type.Gauge32;
import br.gov.frameworkdemoiselle.monitoring.stereotype.MBean;

/**
 * @author SERPRO
 */
@MBean
@Name("br.gov.frameworkdemoiselle:name=Escola")
@MIB(".25.3.1")
@ModuleName("ESCOLA-MIB")
public class EscolaMonitoring implements EscolaMonitoringMBean {

	private static final String VERSAO = "2.4.1-RC2";
	private static final String[] USUARIOS = { "Fulano", "Sicrano", "Beltrano" };

	private int qtdTurmas = 0;
	private long qtdAlunos = 0;

	@OID(".1")
	@Override
	public String getVersaoAplicacao() {
		return VERSAO;
	}

	@OID(".2")
	@Gauge32
	@Override
	public long getQtdAlunosMatriculados() {
		this.qtdAlunos = (int) (Math.random() * 100) + 100;
		return this.qtdAlunos;
	}

	@OID(".3")
	@Counter32
	@Override
	public int getQtdTurmasIncluidas() {
		this.qtdTurmas += (int) (Math.random() * 10);
		return this.qtdTurmas;
	}

	@OID(".4")
	@TextualConvention("Usuario")
	@Override
	public String getUltimoUsuarioLogado() {
		int pos = (int) (Math.random() * USUARIOS.length);
		return USUARIOS[pos];
	}

	@OID(".5")
	@ReadWrite
	@AllowedValues({1, 2, 3, 4})
	private int nivelLog = 1;

	@Override
	public int getNivelLog() {
		return this.nivelLog;
	}

	@Override
	public void setNivelLog(int nivelLog) {
		this.nivelLog = nivelLog;
	}

}
