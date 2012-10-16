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
package br.gov.frameworkdemoiselle.monitoring.internal.implementation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;

import org.slf4j.Logger;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.Counter32;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Gauge32;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.monitoring.annotation.JMXQuery;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.MIB;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.SpecificTrap;
import br.gov.frameworkdemoiselle.monitoring.internal.configuration.snmp.SNMPTrapperConfig;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.jmx.MBeanHelper;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.SNMPTypes;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.snmp.SNMPSender;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Handler intended to act over <b>custom SNMP trappers</b>.
 * 
 * @author SERPRO
 */
public class SNMPTrapperHandler {

	@Inject
	private Logger logger;

	@Inject
	private SNMPTrapperConfig config;

	@Inject
	@Name("demoiselle-monitoring-bundle")
	private ResourceBundle bundle;	

	private OID oidEnterprise;
	private final long startTime;
	private boolean started = false;
	
    private SNMPSender sender;
    private final BlockingQueue<PDU> queue = new LinkedBlockingQueue<PDU>();
	
	@PostConstruct
	public void init() {
		logger.info(bundle.getString("trapper-snmp-initializing"));
		
		// ex: udp:localhost/162
		final String address = String.format("%s:%s/%d", config.getTrapperProtocol(),
				config.getTrapperServer(), config.getTrapperPort());		
		
		final Address targetAddress = GenericAddress.parse(address);
		final CommunityTarget target = new CommunityTarget();
		target.setCommunity(new OctetString(config.getTrapperCommunity()));
		target.setAddress(targetAddress);
		target.setVersion(SnmpConstants.version1);
		
		logger.debug(bundle.getString("trapper-snmp-initializing-target", target));
		sender = new SNMPSender(target, queue, bundle);
	}

	@PreDestroy
	public void term() {
		logger.info(bundle.getString("trapper-snmp-terminating"));
		sender.interrupt();
	}
    
	/**
     * Create a new SNMP trapper using default constructor.
	 */
	public SNMPTrapperHandler() {
		this.startTime = System.currentTimeMillis();
	}
	
    /**
     * @param clz
     */
    public void initialize(final Class<?> clz) {
    	final String trapperEnterprise = config.getTrapperEnterprise();

    	final MIB mib = clz.getAnnotation(MIB.class);
    	if (mib == null) {
    		this.oidEnterprise = new OID(trapperEnterprise);
    	} else {
    		final String oidTrapper = mib.value();
    		if (oidTrapper.startsWith(".")) {
    			this.oidEnterprise = new OID(trapperEnterprise.concat(oidTrapper));
    		} else {
    			this.oidEnterprise = new OID(oidTrapper);
    		}
    	}

		logger.debug(bundle.getString("trapper-snmp-setting-enterprise", this.oidEnterprise));
    }
	
	public void start() {
		if (!started) {
			logger.debug(bundle.getString("trapper-snmp-starting"));
			sender.start();
			started = true;
		}
	}

	public void stop() {
		logger.debug(bundle.getString("trapper-snmp-stopping"));
        sender.stopping();
        try {
            sender.join();
        } catch (InterruptedException e) {
            // ignore, we're done anyway...
        }
	}

	private void checkStarted() {
		if (!started) {
			start();
		}
	}
	
	/**
	 * Send a SNMP trap using parameters defined in the InvocationContext.
	 * 
	 * @param ctx
	 */
	public void sendTrap(final InvocationContext ctx) {
		
		this.checkStarted();

		final Method method = ctx.getMethod();
		final Object[] values = ctx.getParameters();

		// TODO: pré-configurar essas anotações no handler durante o @PostConstruct
		br.gov.frameworkdemoiselle.monitoring.annotation.snmp.OID oid =
				method.getAnnotation(br.gov.frameworkdemoiselle.monitoring.annotation.snmp.OID.class);
		SpecificTrap specificTrap = method.getAnnotation(SpecificTrap.class);
		JMXQuery jmxQuery = method.getAnnotation(JMXQuery.class);
		
		final String mbeanName = (jmxQuery != null ? jmxQuery.mbeanName() : null);
		final String mbeanAttribute = (jmxQuery != null ? jmxQuery.mbeanAttribute() : null);
		final boolean hasMBean = (mbeanName != null && !mbeanName.isEmpty());

		final String enterpriseOID;
		boolean skipFirstArg = false;
		if (oid != null) {
			final String oidValue = oid.value();
			if (oidValue.startsWith(".")) {
				enterpriseOID = oidEnterprise.toString().concat(oidValue);
			} else if ("*".equals(oidValue)) {
				enterpriseOID = values[0].toString();
				skipFirstArg = true;
			} else {
				enterpriseOID = oidValue;
			}
		} else {
			enterpriseOID = oidEnterprise.toString();
		}

		// FIXME: melhorar esse log
		/*
		logger.debug("Enterprise OID = " + enterpriseOID +
				((jmxQuery != null) ? ", jmxQuery = " + jmxQuery.mbeanName()
				+ "[" + jmxQuery.mbeanAttribute() + "]" : "") +
				", values = " + Arrays.toString(values));
		*/

		PDUv1 pdu = new PDUv1();
		pdu.setType(PDU.V1TRAP);
		pdu.setEnterprise(new OID(enterpriseOID));
		pdu.setTimestamp(this.getSystemUptime());
		pdu.setGenericTrap(PDUv1.ENTERPRISE_SPECIFIC);
		if (specificTrap != null) {
			pdu.setSpecificTrap(specificTrap.value());
		}
		
		/*
		if (hasMBean && (mbeanAttribute == null || mbeanAttribute.isEmpty())) {
			throw new TrapperException(
					"Parameter 'mbeanAttribute' must be defined along with 'mbeanName' on @JMXQuery annotation");
		}
		*/

		VariableBinding vb = null;
		OID oidVariable;
		
		int counter = 1;
		for (int argPos = 0; argPos < values.length; argPos++) {
			
			if (skipFirstArg) {
				skipFirstArg = false;
				continue;
			}
			
			oidVariable = new OID(enterpriseOID.concat(".").concat(String.valueOf(counter++)).concat(".0"));
			
			Object value = null;
			if (!hasMBean || argPos < values.length) {
				value = values[argPos];
			} else {
				// FIXME: não vai passar por aqui...
				value = MBeanHelper.query(mbeanName, mbeanAttribute);
			}
			
			Annotation[] panns = method.getParameterAnnotations()[argPos];
			Class<?> varType = SNMPTypes.retrieveDataType(panns, value);
			
			Variable variable = null;
			if (varType == OctetString.class) {
				variable = new OctetString(value != null ? value.toString() : null);
			} else if (varType == Counter32.class) {
				variable = new Counter32(new Long(value.toString()));
			} else if (varType == Gauge32.class) {
				variable = new Gauge32(new Long(value.toString()));
			} else if (varType == Integer32.class) {
				variable = new Integer32(new Integer(value.toString()));
			} else if (varType == TimeTicks.class) {
				variable = new TimeTicks(new Long(value.toString()));
			} else if (varType == Counter64.class) {
				variable = new Counter64(new Long(value.toString()));
			} else if (varType == IpAddress.class) {
				variable = new IpAddress(value.toString());
			} else if (varType == OID.class) {
				variable = new OID(value.toString());
			} else {
				variable = new OctetString(value.toString());
			}
			
			vb = new VariableBinding();
			vb.setOid(oidVariable);
			vb.setVariable(variable);
			
			pdu.add(vb);
		}
		
		queue.offer(pdu);
	}
	
	/**
	 * Returns system uptime since the service was put online.
	 * 
	 * @return long
	 */
	protected long getSystemUptime() {
		return (System.currentTimeMillis() - this.startTime);
	}

}
