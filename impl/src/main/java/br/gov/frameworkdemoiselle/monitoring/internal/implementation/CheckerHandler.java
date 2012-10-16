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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.monitoring.annotation.Scheduled;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Handler intended to act over <b>checkers</b>.
 * 
 * @author SERPRO
 */
public class CheckerHandler {

	@Inject
	private Logger logger;

	@Inject
	@Name("demoiselle-monitoring-bundle")
	private ResourceBundle bundle;	

	private Object checker;
	
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	/**
	 * Starts the checker using the given class.
	 * 
	 * @param checker
	 */
	public void start(final Object checker) {
		logger.info(bundle.getString("checker-starting"), checker);
		this.checker = checker;
		scheduleChecks();
	}

	protected void scheduleChecks() {
		
		// retrieve annotated scheduled checks
		Collection<Method> methods = this.getMethodsToSchedule();
		for (final Method method : methods) {
			
			// retrieve checking intervals
			Scheduled scheduled = method.getAnnotation(Scheduled.class);
			final int time = scheduled.interval();
			final TimeUnit unit = scheduled.unit();
			
			logger.debug(bundle.getString("checker-scheduling-method",
					method.getName(), time, unit.toString()));
			
			// finally schedule the event!
	        scheduler.scheduleAtFixedRate(
	        		new ScheduledTask(this.checker, method), 0, time, unit);
		}
	}
	
	/**
	 * Stops the checker.
	 */
	public void stop() {
		logger.info(bundle.getString("checker-stopping"), this.checker);
		scheduler.shutdown();
	}

	/**
	 * Returns the list of methods annotated with @Scheduled in the given object.
	 * 
	 * @return Collection<Method>
	 */
	protected Collection<Method> getMethodsToSchedule() {
		
		final Collection<Method> list = new ArrayList<Method>();
		final Class<?> clazz = this.checker.getClass().getSuperclass();
		
		for (Method method : clazz.getDeclaredMethods()) {
			if (method.isAnnotationPresent(Scheduled.class)) {
				list.add(method);
			}
		}
		
		return list;
	}

	/**
	 * Internal class designed to schedule checking events.
	 */
	private class ScheduledTask implements Runnable {
		
		private final Object instance;
		private final Method method;

		private ScheduledTask(Object instance, Method method) {
			this.instance = instance;
			this.method = method;
		}

		public void run() {
        	try {
        		logger.debug(bundle.getString("checker-invoking-method"),
        				method.getName(), instance.toString());
				method.invoke(instance, (Object[]) null);
			} catch (Exception e) {
				logger.error(bundle.getString("checker-invoking-method-error", method.getName()), e);
			}
		}
	}

}
