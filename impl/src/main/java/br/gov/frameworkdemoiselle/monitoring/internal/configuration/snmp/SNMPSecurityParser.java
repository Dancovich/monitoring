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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.xerces.parsers.DOMParser;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.monitoring.exception.ConfigException;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security.AccessEntry;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security.AuthProtocol;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security.GroupEntry;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security.PrivProtocol;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security.UserEntry;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security.ViewTree;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security.ViewTreeFamily;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * A parser for reading SNMP security definitions file in XML format.
 * 
 * @author SERPRO
 */
@Singleton
public class SNMPSecurityParser {

	private final DOMParser parser;
	
	private Document doc;

	private List<UsmUser> usersList;
	private List<ViewTreeFamily> viewsList;
	private List<GroupEntry> groupsList;
	
	@Inject
	@Name("demoiselle-monitoring-bundle")
	private ResourceBundle bundle;

	/**
	 * Constructor for the class.
	 */
	public SNMPSecurityParser() {
		this.parser = new DOMParser();
	}

	/**
	 * Starts the XML file parsing.
	 * 
	 * @param xmlFile	the XML file
	 */
	public void parseDocument(String xmlFile) {
		
		if (!xmlFile.startsWith("/")) {
			xmlFile = this.getClass().getResource("/" + xmlFile).toString();
		}
		
		try {
			parser.parse(xmlFile);
		} catch (Exception e) {
			throw new ConfigException(
					bundle.getString("security-parser-file-definition-error", xmlFile), e);
		}
		doc = parser.getDocument();
		
		this.readUsers();
		this.readViews();
		this.readGroups();
	}

	private void readUsers() {
		
		usersList = new ArrayList<UsmUser>();
		
		// get the root element
		Element root = doc.getDocumentElement();

		// get a nodelist of elements
		NodeList nl = root.getElementsByTagName("user");
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				UsmUser user = this.readUsmUser(el);
				usersList.add(user);
			}
		}
	}
	
	private UsmUser readUsmUser(Element elem) {
		
		NodeList nl = null;
        OID authProt = null, privProt = null;
        OctetString authPass = null, privPass = null;
        
		// security name
		String name = elem.getAttribute("name");
		
		// authentication
		nl = elem.getElementsByTagName("auth");
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			String prot = el.getAttribute("protocol");
			AuthProtocol authProtocol = AuthProtocol.parseString(prot);
			authProt = (authProtocol != null ? authProtocol.getOID() : null);
			String pass = el.getAttribute("pass");
			authPass = new OctetString(pass);
		}
		
		// privacy
		nl = elem.getElementsByTagName("priv");
		if (nl != null && nl.getLength() > 0) {
			Element el = (Element) nl.item(0);
			String prot = el.getAttribute("protocol");
			PrivProtocol privProtocol = PrivProtocol.parseString(prot);
			privProt = (privProtocol != null ? privProtocol.getOID() : null);
			String pass = el.getAttribute("pass");
			privPass = new OctetString(pass);
		}
		
		UsmUser usr = null;
		try {
			usr = new UsmUser(new OctetString(name),
					authProt, authPass, privProt, privPass);
		} catch (Exception e) {
			throw new ConfigException(
					bundle.getString("security-parser-user-creation-error", name), e);
		}
		
		return usr;
	}

	/**
	 * Returns the users list.
	 * 
	 * @return	List<UsmUser>
	 */
	public List<UsmUser> getUsersList() {
		return usersList;
	}

	private void readViews() {
		
		viewsList = new ArrayList<ViewTreeFamily>();
		
		// get the root element
		Element root = doc.getDocumentElement();

		// get a nodelist of elements
		NodeList nl = root.getElementsByTagName("view");
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				ViewTreeFamily view = this.readView(el);
				viewsList.add(view);
			}
		}
	}
	
	private ViewTreeFamily readView(Element elem) {
		
		NodeList nl = null;

		// view name
		String name = elem.getAttribute("name");
		
		ViewTreeFamily vtf = new ViewTreeFamily(name);

		// includes
		nl = elem.getElementsByTagName("include");
		if (nl != null) {
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				String subtree = el.getAttribute("subtree");
				String mask = el.getAttribute("mask");
				ViewTree view = new ViewTree(subtree, mask);
				vtf.getIncludes().add(view);
			}
		}
		
		// excludes
		nl = elem.getElementsByTagName("exclude");
		if (nl != null) {
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				String subtree = el.getAttribute("subtree");
				String mask = el.getAttribute("mask");
				ViewTree view = new ViewTree(subtree, mask);
				vtf.getExcludes().add(view);
			}
		}
		
		return vtf;
	}

	/**
	 * Returns the views list.
	 * 
	 * @return	List<ViewTreeFamily>
	 */
	public List<ViewTreeFamily> getViewsList() {
		return viewsList;
	}

	private void readGroups() {
		
		groupsList = new ArrayList<GroupEntry>();
		
		// get the root element
		Element root = doc.getDocumentElement();

		// get a nodelist of elements
		NodeList nl = root.getElementsByTagName("group");
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				GroupEntry group = this.readGroup(el);
				groupsList.add(group);
			}
		}
	}

	private GroupEntry readGroup(Element elem) {
		
		NodeList nl = null;

		// view name
		String name = elem.getAttribute("name");
		
		GroupEntry group = new GroupEntry(name);

		// users
		nl = elem.getElementsByTagName("security");
		if (nl != null) {
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				String secName = el.getAttribute("name");
				String model = el.getAttribute("model");
				UserEntry usr = new UserEntry(secName, model);
				group.getUsers().add(usr);
			}
		}
		
		// access
		nl = elem.getElementsByTagName("access");
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				Element el = (Element) nl.item(i);
				
				String context = el.getAttribute("context");
				String model = el.getAttribute("model");
				Boolean auth = new Boolean(el.getAttribute("auth"));
				Boolean priv = new Boolean(el.getAttribute("priv"));
				Boolean exact = new Boolean(el.getAttribute("exact"));
				
				AccessEntry access = new AccessEntry(context, model, auth, priv, exact);
				
				NodeList views = el.getElementsByTagNameNS("*", "*"); 
				for (int j = 0; j < views.getLength(); j++) {
					Element viewElem = (Element) views.item(j);
					String viewType = viewElem.getNodeName();
					String viewName = viewElem.getAttribute("view");
					if ("read".equals(viewType)) {
						access.setReadView(viewName);
					} else if ("write".equals(viewType)) {
						access.setWriteView(viewName);
					} else if ("notify".equals(viewType)) {
						access.setNotifyView(viewName);
					}
				}
				
				group.setAccess(access);
			}
		}
		
		return group;
	}

	/**
	 * Returns the groups list.
	 * 
	 * @return	List<GroupEntry>
	 */
	public List<GroupEntry> getGroupsList() {
		return groupsList;
	}

	/**
	 * Releases all allocated resources for the parser.
	 */
	public void releaseResources() {
		if (usersList != null) {
			usersList.clear();
			usersList = null;
		}
		if (viewsList != null) {
			viewsList.clear();
			viewsList = null;
		}
		if (groupsList != null) {
			groupsList.clear();
			groupsList = null;
		}
		doc = null;
	}

}
