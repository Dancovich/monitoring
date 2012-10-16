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
package br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security;

import org.snmp4j.smi.OctetString;

/**
 * Holds information for a single <b>access entry</b>.
 * 
 * @author SERPRO
 */
public class AccessEntry {

	private OctetString context;
	private SecModel model;
	private SecLevel level;
	private MatchType match;
	
	private OctetString readView;
	private OctetString writeView;
	private OctetString notifyView;

	public AccessEntry(String context, String model, boolean auth, boolean priv, boolean exact) {
		this.context = new OctetString(context);
		this.model = SecModel.parseString(model);
		this.level = SecLevel.parsePair(auth, priv);
		this.match = MatchType.parseBoolean(exact);
	}

	public OctetString getContext() {
		return context;
	}

	public void setContext(OctetString context) {
		this.context = context;
	}

	public SecModel getModel() {
		return model;
	}

	public void setModel(SecModel model) {
		this.model = model;
	}

	public SecLevel getLevel() {
		return level;
	}

	public void setLevel(SecLevel level) {
		this.level = level;
	}

	public OctetString getReadView() {
		return readView;
	}

	public void setReadView(String readView) {
		this.readView = new OctetString(readView);
	}

	public OctetString getWriteView() {
		return writeView;
	}

	public void setWriteView(String writeView) {
		this.writeView = new OctetString(writeView);
	}

	public OctetString getNotifyView() {
		return notifyView;
	}

	public void setNotifyView(String notifyView) {
		this.notifyView = new OctetString(notifyView);
	}

	public MatchType getMatch() {
		return match;
	}

	public void setMatch(MatchType match) {
		this.match = match;
	}

	public String toString() {
		return "AccessEntry [context=" + context + ", level=" + level
				+ ", match=" + match + ", model=" + model + ", notifyView="
				+ notifyView + ", readView=" + readView + ", writeView="
				+ writeView + "]";
	}
	
}
