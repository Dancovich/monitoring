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
package br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.snmp4j.TransportMapping;
import org.snmp4j.agent.BaseAgent;
import org.snmp4j.agent.CommandProcessor;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.io.ImportModes;
import org.snmp4j.agent.mo.MOTableRow;
import org.snmp4j.agent.mo.snmp.RowStatus;
import org.snmp4j.agent.mo.snmp.SnmpCommunityMIB;
import org.snmp4j.agent.mo.snmp.SnmpNotificationMIB;
import org.snmp4j.agent.mo.snmp.SnmpTargetMIB;
import org.snmp4j.agent.mo.snmp.StorageType;
import org.snmp4j.agent.mo.snmp.TransportDomains;
import org.snmp4j.agent.mo.snmp.VacmMIB;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.MessageProcessingModel;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModel;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.transport.TransportMappings;
import org.snmp4j.util.ThreadPool;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.monitoring.exception.AgentException;
import br.gov.frameworkdemoiselle.monitoring.internal.configuration.snmp.SNMPAgentConfig;
import br.gov.frameworkdemoiselle.monitoring.internal.configuration.snmp.SNMPSecurityParser;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security.AccessEntry;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security.GroupEntry;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security.UserEntry;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security.ViewTree;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.security.ViewTreeFamily;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * The <b>SNMP agent</b> implementation.
 * 
 * @author SERPRO
 */
public class SNMPAgent extends BaseAgent implements Runnable {

	@Inject
	private Logger logger;

	@Inject
	private SNMPAgentConfig config;
	
	@Inject
	private SNMPSecurityParser parser;
	
	@Inject
	@Name("demoiselle-monitoring-bundle")
	private ResourceBundle bundle;	

	private String address;
	private MIBManager manager;
	private List<Class<?>> mibs;
	
	/**
	 * @throws IOException
	 */
	public SNMPAgent() {
		this(new File("SNMPAgentBootCounter.cfg"), new File("SNMPAgentConfig.cfg"));
	}

	/**
	 * @param bootCounterFile
	 * @param configFile
	 */
	protected SNMPAgent(File bootCounterFile, File configFile) {
		super(bootCounterFile, configFile, new CommandProcessor(
				new OctetString(MPv3.createLocalEngineID())));
	}

	@PostConstruct
	public void initialize() {
		logger.info(bundle.getString("agent-snmp-creating"));
		agent.setWorkerPool(ThreadPool.create("RequestPool", 4));
		this.address = config.getAgentProtocol() + ":0.0.0.0/" + config.getAgentPort();
		this.manager = new MIBManager(this, config.getMibRoot(), bundle);
		parser.parseDocument(config.getSecurity());
	}
	
	public void assignMIBsList(final List<Class<?>> mibs) {
		this.mibs = mibs;
	}
	
	protected void initTransportMappings() throws IOException {
		logger.debug(bundle.getString("agent-snmp-initializing-transport"), this.address);
		transportMappings = new TransportMapping[1];
		Address addr = GenericAddress.parse(address);
		TransportMapping tm = TransportMappings.getInstance().createTransportMapping(addr);
		transportMappings[0] = tm;
	}

	protected void addCommunities(SnmpCommunityMIB communityMIB) {
		logger.debug(bundle.getString("agent-snmp-setting-communities"));
		
		// TODO: read params from a configuration file
		Variable[] com2sec = new Variable[] {
				new OctetString("public"), // community name
				new OctetString("public"), // security name
				getAgent().getContextEngineID(), // local engine ID
				new OctetString(), // default context name
				new OctetString(), // transport tag
				new Integer32(StorageType.nonVolatile), // storage type
				new Integer32(RowStatus.active) // row status
		};
		
		MOTableRow row = communityMIB.getSnmpCommunityEntry().createRow(
				new OctetString("public2public").toSubIndex(true), com2sec);
		
		communityMIB.getSnmpCommunityEntry().addRow(row);
		// snmpCommunityMIB.setSourceAddressFiltering(true);
	}

	protected void addNotificationTargets(SnmpTargetMIB targetMIB, SnmpNotificationMIB notificationMIB) {
		logger.debug(bundle.getString("agent-snmp-setting-notification"));
		
		// TODO: read params from a configuration file
		targetMIB.addDefaultTDomains();
		targetMIB.addTargetAddress(
				new OctetString("notification"),
				TransportDomains.transportDomainUdpIpv4,
				new OctetString(new UdpAddress("127.0.0.1/162").getValue()), 200, 1,
				new OctetString("notify"),
				new OctetString("v2c"),
				StorageType.permanent);
		targetMIB.addTargetParams(
				new OctetString("v2c"),
				MessageProcessingModel.MPv2c,
				SecurityModel.SECURITY_MODEL_SNMPv2c,
				new OctetString("public"),
				SecurityLevel.NOAUTH_NOPRIV,
				StorageType.permanent);
		
		notificationMIB.addNotifyEntry(
				new OctetString("default"),
				new OctetString("notify"),
				SnmpNotificationMIB.SnmpNotifyTypeEnum.trap,
				StorageType.permanent);
	}

	protected void addUsmUser(USM usm) {
		logger.debug(bundle.getString("agent-snmp-setting-usm"));
		
		// loop through configured users
		final List<UsmUser> users = parser.getUsersList();
		for (UsmUser user : users) {
			logger.debug(bundle.getString("agent-snmp-adding-user",
					user.getSecurityName(), user.getAuthenticationProtocol(), user.getPrivacyProtocol()));
			usm.addUser(user.getSecurityName(), usm.getLocalEngineID(), user);
		}
	}

	protected void addViews(VacmMIB vacm) {
		logger.debug(bundle.getString("agent-snmp-setting-vacm"));
		
		// loop through configured views
		final List<ViewTreeFamily> views = parser.getViewsList();
		for (ViewTreeFamily vtf : views) {
			
			// includes
			for (ViewTree view : vtf.getIncludes()) {
				logger.debug(bundle.getString("agent-snmp-including-subtree",
						view.getSubtree(), view.getMask(), vtf.getViewName()));
				vacm.addViewTreeFamily(vtf.getViewName(), view.getSubtree(), view.getMask(),
						VacmMIB.vacmViewIncluded, StorageType.nonVolatile);
			}
			
			// excludes
			for (ViewTree view : vtf.getExcludes()) {
				logger.debug(bundle.getString("agent-snmp-excluding-subtree",
						view.getSubtree(), view.getMask(), vtf.getViewName()));
				vacm.addViewTreeFamily(vtf.getViewName(), view.getSubtree(), view.getMask(),
						VacmMIB.vacmViewExcluded, StorageType.nonVolatile);
			}
		}
		
		// loop through configured groups
		final List<GroupEntry> groups = parser.getGroupsList();
		for (GroupEntry group : groups) {
            final OctetString groupName = group.getName();
			
			// include users
			for (UserEntry user : group.getUsers()) {
				int securityModel = user.getModel().getId();
				OctetString securityName = user.getName();
                
				logger.debug(bundle.getString("agent-snmp-including-user-group",
						securityName, groupName, user.getModel()));
    			vacm.addGroup(securityModel, securityName, groupName, StorageType.nonVolatile);
			}
			
			// include access
			if (group.getAccess() != null) {
				AccessEntry access = group.getAccess();
				
				logger.debug(bundle.getString("agent-snmp-granting-access-views",
						access.getReadView(), access.getWriteView(), access.getNotifyView(),
						groupName, access.getModel(), access.getLevel(), access.getMatch()));
				
				vacm.addAccess(groupName, access.getContext(),
						access.getModel().getId(), access.getLevel().getId(), access.getMatch().getId(),
						access.getReadView(), access.getWriteView(), access.getNotifyView(),
						StorageType.nonVolatile);
			}
		}
	}

	protected void registerManagedObjects() {
		//MBeanManager.getInstance().initializeConfiguredMBeans();
	}

	protected void unregisterManagedObjects() {
		//MBeanManager.getInstance().unregisterMBeans();
	}

	protected void registerSnmpMIBs() {
		logger.debug(bundle.getString("agent-snmp-registering-mibs"));
		if (mibs != null) {
			for (Class<?> clazz : mibs) {
				try {
					manager.registerMIB(clazz);
				} catch (DuplicateRegistrationException e) {
					logger.warn(bundle.getString("agent-snmp-duplicated-registration-error", clazz));
				}
			}
		}
	}

	protected void finishInit() {
		parser.releaseResources();
		parser = null;
		super.finishInit();
		logger.info(bundle.getString("agent-snmp-ready-to-serve"));
	}
	
	public void startup() {
		try {
			init();
		} catch (IOException e) {
			throw new AgentException(bundle.getString("agent-snmp-startup-error", e));
		}
		loadConfig(ImportModes.UPDATE_CREATE);
		addShutdownHook();
		finishInit();
		run();
	}

	public void shutdown() {
		logger.info(bundle.getString("agent-snmp-shutting-down"));
	}

}
