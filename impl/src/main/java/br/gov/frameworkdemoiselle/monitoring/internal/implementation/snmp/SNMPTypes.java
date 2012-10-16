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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.snmp4j.agent.MOAccess;
import org.snmp4j.agent.mo.MOAccessImpl;
import org.snmp4j.smi.Counter32;
import org.snmp4j.smi.Counter64;
import org.snmp4j.smi.Gauge32;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;

import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.access.Notify;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.access.ReadCreate;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.access.ReadOnly;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.access.ReadWrite;
import br.gov.frameworkdemoiselle.monitoring.annotation.snmp.access.WriteOnly;

/**
 * Utility class used when translating SNMP types to Java data types.
 * 
 * @author SERPRO
 */
public class SNMPTypes {

	protected static final Map<Class<?>, Class<?>> dataTypesMap =
			Collections.synchronizedMap(new HashMap<Class<?>, Class<?>>());

	static {
		dataTypesMap.put(br.gov.frameworkdemoiselle.monitoring.annotation.snmp.type.Counter32.class, Counter32.class);
		dataTypesMap.put(br.gov.frameworkdemoiselle.monitoring.annotation.snmp.type.Counter64.class, Counter64.class);
		dataTypesMap.put(br.gov.frameworkdemoiselle.monitoring.annotation.snmp.type.Gauge32.class, Gauge32.class);
		dataTypesMap.put(br.gov.frameworkdemoiselle.monitoring.annotation.snmp.type.Integer32.class, Integer32.class);
		dataTypesMap.put(br.gov.frameworkdemoiselle.monitoring.annotation.snmp.type.IPAddress.class, IpAddress.class);
		dataTypesMap.put(br.gov.frameworkdemoiselle.monitoring.annotation.snmp.type.ObjectIdentifier.class, OID.class);
		dataTypesMap.put(br.gov.frameworkdemoiselle.monitoring.annotation.snmp.type.OctetString.class, OctetString.class);
		dataTypesMap.put(br.gov.frameworkdemoiselle.monitoring.annotation.snmp.type.TimeTicks.class, TimeTicks.class);
	}

	protected static final Map<Class<?>, MOAccess> accessTypesMap =
		Collections.synchronizedMap(new HashMap<Class<?>, MOAccess>());

	static {
		accessTypesMap.put(ReadOnly.class, MOAccessImpl.ACCESS_READ_ONLY);
		accessTypesMap.put(ReadWrite.class, MOAccessImpl.ACCESS_READ_WRITE);
		accessTypesMap.put(ReadCreate.class, MOAccessImpl.ACCESS_READ_CREATE);
		accessTypesMap.put(Notify.class, MOAccessImpl.ACCESS_FOR_NOTIFY);
		accessTypesMap.put(WriteOnly.class, MOAccessImpl.ACCESS_WRITE_ONLY);
	}
	
	public static final MOAccess DEFAULT_ACCESS_TYPE = MOAccessImpl.ACCESS_READ_ONLY;
	
	/**
	 * @param annotations
	 * @param object
	 * @return
	 */
	public static Class<?> retrieveDataType(final Annotation[] annotations, final Object object) {
		if (annotations != null && annotations.length > 0) {
			for (Annotation annotation : annotations) {
				Class<?> dataType = SNMPTypes.retrieveDataTypeFromAnnotation(annotation.annotationType());
				if (dataType != null) {
					return dataType;
				}
			}
		}
		return SNMPTypes.retrieveDataTypeFromObject(object);
	}
	
	/**
	 * Retrieves the SNMP data type corresponding to the given annotation.
	 * Example: Gauge32.class => @Gauge32
	 * 
	 * @param annotationType
	 * @return a class
	 */
	private static Class<?> retrieveDataTypeFromAnnotation(final Class<?> annotationType) {
		return dataTypesMap.get(annotationType);
	}

	/**
	 * Retrieves the SNMP data type corresponding to an object instance.
	 * 
	 * @param object
	 * @return a class
	 */
	private static Class<?> retrieveDataTypeFromObject(final Object object) {
		if (object instanceof String || String.class.equals(object)) {
			return OctetString.class;
		} else if (Number.class.isInstance(object)) {
			return Integer32.class;
		} else if (object instanceof Class) {
			Class<?> clz = (Class<?>) object;
			if (clz.isPrimitive()) {
				if (Integer.TYPE.equals(clz) ||
					Long.TYPE.equals(clz) ||
					Byte.TYPE.equals(clz) ||
					Short.TYPE.equals(clz)
				) {
					return Integer32.class;
				}
			} else {
				if (Integer.class.equals(clz) ||
					Long.class.equals(clz) ||
					Byte.class.equals(clz) ||
					Short.class.equals(clz)
				) {
					return Integer32.class;
				}
			}
		}
		return null;
	}

	/**
	 * @param annotation
	 * @return
	 */
	public static MOAccess retrieveAccessType(final Class<?> annotationType) {
		return accessTypesMap.get(annotationType);
	}
	
}
