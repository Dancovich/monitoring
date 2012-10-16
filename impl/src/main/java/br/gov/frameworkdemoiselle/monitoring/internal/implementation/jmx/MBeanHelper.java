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
package br.gov.frameworkdemoiselle.monitoring.internal.implementation.jmx;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.monitoring.exception.MBeanException;

/**
 * Support class used for handling MBeans in the JMX Server.
 * 
 * @author SERPRO
 */
public final class MBeanHelper {

	private static final Logger logger = LoggerProducer.create(MBeanHelper.class);
	
	private static final MBeanServer server = ManagementFactory.getPlatformMBeanServer();

//	@Inject
//	@Name("demoiselle-monitoring-bundle")
//	private ResourceBundle bundle;	

	/**
	 * Return the MBean Server instance.
	 * 
	 * @return	MBeanServer
	 */
	public static final MBeanServer getMBeanServer() {
		return server;
	}
	
	/**
	 * Register a given managed bean (MBean) with the specified name.
	 * 
	 * @param mbean	the managed bean to register
	 * @param name	the name under which to register the bean
	 * @return the object name of the mbean, for later deregistration
	 */
	public static ObjectInstance register(final Object mbean, final String name) {

		logger.info("Registering MBean [" + name + "]: " + mbean);

		ObjectInstance instance = null;
		try {
			ObjectName objectName = new ObjectName(name);
			instance = server.registerMBean(mbean, objectName);
		} catch (Exception e) {
			throw new MBeanException(
					"Unable to register MBean [" + name + "]", e);
		}

		return instance;
	}

	/**
	 * Remove the registration of a bean.
	 * 
	 * @param objectName	the name of the bean to unregister
	 */
	public static void unregister(final ObjectName objectName) {

		logger.info("Unregistering MBean [" + objectName + "]");

		try {
			server.unregisterMBean(objectName);
		} catch (Exception e) {
			throw new MBeanException(
					"Unable to unregister MBean [" + objectName + "]", e);
		}
	}
	
	/**
	 * Perform a JMX query given an MBean name and the name of an attribute on
	 * that MBean.
	 * 
	 * @param name		the object name of the MBean to query
	 * @param attribute	the attribute to query for
	 * @return the value of the attribute
	 */
	public static Object query(final String name, final String attribute) {
		
		logger.debug("JMX query: [" + name + "][" + attribute + "]");

		// retrieve MBean instance (already registered)
		ObjectInstance bean = null;
		try {
			bean = server.getObjectInstance(new ObjectName(name));
		} catch (Exception e) {
			throw new MBeanException("Error retrieving MBean instance [" + name + "]", e);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Found MBean class " + bean.getClassName());
		}

		// query MBean simple attribute
		final int dot = attribute.indexOf('.');
		if (dot < 0) {
			Object ret = null;
			try {
				ret = server.getAttribute(new ObjectName(name), attribute);
			} catch (Exception e) {
				throw new MBeanException(
						"Error querying MBean simple attribute [" + name + "][" + attribute + "]", e);
			}
			return ret;
		}

		// query MBean composite attribute
		try {
			CompositeData data = (CompositeData) server.getAttribute(
					new ObjectName(name), attribute.substring(0, dot)); 
			String field = attribute.substring(dot + 1);
			return resolveFields(data, field);
		} catch (Exception e) {
			throw new MBeanException(
					"Error querying MBean composite attribute [" + name + "][" + attribute + "]", e);
		}
	}

	/**
	 * Support method intended to resolve fields when using attributes with composite data types.
	 * 
	 * @param attribute	the attribute instance
	 * @param field		the field name which value must be retrieved
	 * @return	the value corresponding to the composite data type field
	 * @throws Exception
	 */
	private static Object resolveFields(final CompositeData attribute, final String field) throws Exception {
		
		final int dot = field.indexOf('.');
		if (dot < 0) {
			final Object ret = attribute.get(field);
			return ret;
		}
		
		return resolveFields((CompositeData) attribute.get(field.substring(0, dot)), field.substring(dot + 1));
	}

}
