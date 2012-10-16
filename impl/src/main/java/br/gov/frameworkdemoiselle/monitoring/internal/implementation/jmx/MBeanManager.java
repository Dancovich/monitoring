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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.monitoring.exception.MBeanException;
import br.gov.frameworkdemoiselle.monitoring.stereotype.MBean;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Singleton class reserved for managing MBeans in the JMX Server.
 * 
 * @author SERPRO
 */
@Singleton
public class MBeanManager {

	@Inject
	private Logger logger;

	@Inject
	@Name("demoiselle-monitoring-bundle")
	private ResourceBundle bundle;	

	// mapa com nome do MBean (registrado no JMX) e instância da classe
	protected Map<String, MBeanInstance> mbeans = new LinkedHashMap<String, MBeanInstance>();
	
	// mapa com nome da classe e nome do MBean registrado
	protected Map<Class<?>, String> types = new LinkedHashMap<Class<?>, String>();

	/**
	 * Registers the given MBean object.
	 * 
	 * @param mbean	the instance
	 * @return	ObjectInstance
	 */
	public ObjectInstance registerMBean(final Object mbean) {
		
		final Class<?> clazz = mbean.getClass();
		
		if (types.containsKey(clazz)) {
			MBeanInstance inst = this.getMBeanInstance(clazz);
			return (inst != null ? inst.getObjectInstance() : null);
		}
		
		MBean mbeanAnnotation = clazz.getAnnotation(MBean.class);
		if (mbeanAnnotation == null) {
			throw new MBeanException(
					bundle.getString("mbean-manager-absent-annotation-error", clazz));
		}

		Name nameAnnotation = clazz.getAnnotation(Name.class);
		final String name;
		if (nameAnnotation == null || "".equals(nameAnnotation)) {
			name = clazz.getPackage().getName() + ":name=" + clazz.getSimpleName();
		} else {
			name = nameAnnotation.value();
		}

		logger.debug(bundle.getString("mbean-manager-registering", name));
		
		ObjectInstance objectInstance = MBeanHelper.register(mbean, name);
		MBeanInstance instance = new MBeanInstance(mbean, objectInstance);

		this.mbeans.put(name, instance);
		this.types.put(clazz, name);

		return objectInstance;
	}
	
	public void unregisterMBean(final String name) {

		MBeanInstance instance = this.mbeans.get(name);
		Class<?> clazz = null;
		
		if (instance != null) {
			clazz = instance.getMBeanClass();
			ObjectName objectName = instance.getObjectName();
			MBeanHelper.unregister(objectName);
		}
		
		if (clazz != null) {
			this.types.remove(clazz);
		}
		this.mbeans.remove(name);
	}

	/**
	 * Unregisters all previously exported MBeans.
	 */
	public void unregisterMBeans() {

		logger.info(bundle.getString("mbean-manager-unregistering-exported"));

		for (String name : this.mbeans.keySet()) {
			MBeanInstance instance = this.mbeans.get(name);
			ObjectName objectName = instance.getObjectName();
			MBeanHelper.unregister(objectName);
		}

		this.mbeans.clear();
		this.types.clear();
	}

	public MBeanInstance getMBeanInstance(final String name) {
		return this.mbeans.get(name);
	}

	public MBeanInstance getMBeanInstance(final Class<?> clazz) {
		if (types.containsKey(clazz)) {
			return mbeans.get(types.get(clazz));
		}
		return null;
	}

	public ObjectName getObjectName(final String name) {
		MBeanInstance instance = this.mbeans.get(name);
		ObjectName objectName = instance.getObjectName();
		return objectName;
	}

}