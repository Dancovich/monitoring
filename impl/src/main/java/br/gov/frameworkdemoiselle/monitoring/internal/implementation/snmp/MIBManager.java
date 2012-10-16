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

import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import javax.inject.Singleton;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.snmp4j.agent.DuplicateRegistrationException;
import org.snmp4j.agent.MOAccess;
import org.snmp4j.agent.mo.MOFactory;
import org.snmp4j.agent.mo.MOScalar;
import org.snmp4j.agent.mo.jmx.JMXDefaultMOFactory;
import org.snmp4j.agent.mo.jmx.MBeanAttributeMOInfo;
import org.snmp4j.agent.mo.jmx.MBeanAttributeMOScalarSupport;
import org.snmp4j.agent.mo.snmp.smi.EnumerationConstraint;
import org.snmp4j.agent.mo.snmp.smi.ValueConstraint;
import org.snmp4j.agent.mo.snmp.smi.ValueConstraintValidator;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.Variable;

import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.AllowedValues;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.MIB;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.ModuleName;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.OID;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.TextualConvention;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.jmx.MBeanInstance;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.jmx.MBeanManager;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Singleton class reserved for managing MIB trees to be exported by the SNMP agent.
 * 
 * @author SERPRO
 */
@Singleton
public class MIBManager {

	private Logger logger = LoggerProducer.create(MIBManager.class);
	
	private final ResourceBundle bundle;	

	private final SNMPAgent agent;
	private final String mibRoot;
	private final MBeanManager mbeanManager;
	
	public MIBManager(final SNMPAgent agent, final String mibRoot, final ResourceBundle bundle) {
		this.agent = agent;
		this.mibRoot = mibRoot;
		this.bundle = bundle;
		this.mbeanManager = Beans.getReference(MBeanManager.class);
	}

	public void registerMIB(final Class<?> clazz) throws DuplicateRegistrationException {

		logger.info(bundle.getString("mib-manager-registering-class", clazz));
		
		final MBeanServerConnection mbeanServer = ManagementFactory.getPlatformMBeanServer();
		final MBeanAttributeMOScalarSupport scalarSupport = new MBeanAttributeMOScalarSupport(mbeanServer);
		final MOFactory moFactory = new JMXDefaultMOFactory(mbeanServer, scalarSupport);
		
		MBeanInstance instance = mbeanManager.getMBeanInstance(clazz);
		
		ObjectName objectName = instance.getObjectName();
		Object object = instance.getMBeanObject();

		final MIB mib = clazz.getAnnotation(MIB.class);
		// TODO: considerar uso (ou não) do prefixo "."
		final String oidPrefix = mibRoot.concat(mib.value());
		
		for (AccessibleObject element : getFieldsAndMethodsToInject(object)) {
			
			final OID oidAnnotation = element.getAnnotation(OID.class);
			
			// TODO: considerar o prefixo "." na construção da OID
			final String oidString = oidPrefix.concat(oidAnnotation.value()).concat(".0");
			
			String attributeName = null;
			Class<?> attributeType = null;
			if (element instanceof Method) {
				final Method method = (Method) element;
				attributeName = method.getName().replaceFirst("^(get|set|is)", "");
				attributeType = method.getReturnType();
			} else if (element instanceof Field) {
				final Field field = (Field) element;
				final String fieldName = field.getName();
				attributeName = fieldName.substring(0, 1).toUpperCase().concat(fieldName.substring(1));
				attributeType = field.getType();
			}
			
			Class<?> varType = SNMPTypes.retrieveDataType(element.getAnnotations(), attributeType);
			if (varType == null) {
				varType = OctetString.class;
			}
			
			Variable value = null;
			try {
				value = (Variable) varType.newInstance();
			} catch (InstantiationException e) {
				logger.error(e.getMessage());
			} catch (IllegalAccessException e) {
				logger.error(e.getMessage());
			}
			
			final org.snmp4j.smi.OID oid = new org.snmp4j.smi.OID(oidString);
			
			final ModuleName moduleName = element.getAnnotation(ModuleName.class);
			final TextualConvention textualConvention = element.getAnnotation(TextualConvention.class);
			final AllowedValues allowedValues = element.getAnnotation(AllowedValues.class);
			
			final String module = (moduleName != null ? moduleName.value() : null);
			final String convention = (textualConvention != null ? textualConvention.value() : null);
			final MOAccess access = retrieveAccessType(element);
			
			MOScalar scalar = null;
			if (module != null && !module.equals("") && convention != null && !convention.equals("")) {
				scalar = moFactory.createScalar(oid, access, value, module, convention);
			} else {
				scalar = moFactory.createScalar(oid, access, value);
			}
			
			if (access.isAccessibleForWrite() && allowedValues != null) {
				ValueConstraint constraint = new EnumerationConstraint(allowedValues.value());
				scalar.addMOValueValidationListener(new ValueConstraintValidator(constraint));
			}
			
			logger.debug(bundle.getString("mib-manager-associating-mbean", objectName, attributeName, oid));
			agent.getServer().register(scalar, null);
			
			MBeanAttributeMOInfo mba = new MBeanAttributeMOInfo(objectName, attributeName, Integer.class);
			scalarSupport.add(oid, mba);
		}
	}
	
	/**
	 * @param element
	 * @return
	 */
	private MOAccess retrieveAccessType(final AccessibleObject element) {
		Annotation[] annotations = element.getAnnotations();
		for (Annotation annotation : annotations) {
			MOAccess accessType = SNMPTypes.retrieveAccessType(annotation.annotationType());
			if (accessType != null) {
				return accessType;
			}
		}
		return SNMPTypes.DEFAULT_ACCESS_TYPE;
	}

	/**
	 * Retrieves a list of every field or method annotated with @MIB in the given MBean class.
	 * 
	 * @param object	the class instance
	 * @return list of fields and methods
	 */
	protected Collection<AccessibleObject> getFieldsAndMethodsToInject(final Object object) {
		
		final Collection<AccessibleObject> list = new ArrayList<AccessibleObject>();
		final Class<?> clazz = object.getClass();
		
		// searches all fields
		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(OID.class)) {
				list.add(field);
			}
		}
		
		// searches all methods
		for (Method method : object.getClass().getDeclaredMethods()) {
			if (method.isAnnotationPresent(OID.class)) {
				list.add(method);
			}
		}
		
		return list;
	}

}
