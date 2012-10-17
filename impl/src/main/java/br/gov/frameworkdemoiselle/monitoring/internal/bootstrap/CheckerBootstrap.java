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
package br.gov.frameworkdemoiselle.monitoring.internal.bootstrap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.ProcessAnnotatedType;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.internal.bootstrap.AbstractLifecycleBootstrap;
import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.internal.producer.ResourceBundleProducer;
import br.gov.frameworkdemoiselle.lifecycle.Startup;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.CheckerHandler;
import br.gov.frameworkdemoiselle.monitoring.stereotype.Checker;
import br.gov.frameworkdemoiselle.util.Beans;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Bootstrap class intented to initialize and start <b>checkers</b> automatically on
 * application startup. Moreover, it stops the checkers before application shutdown.
 * <p>
 * The given classes need to be annotated with {@code @Checker} to be handled.
 * 
 * @author SERPRO
 */
public class CheckerBootstrap extends AbstractLifecycleBootstrap<Startup> {
	
	private static Logger logger = LoggerProducer.create(CheckerBootstrap.class);

	protected static List<AnnotatedType<?>> types = Collections.synchronizedList(new ArrayList<AnnotatedType<?>>());

	protected static final List<CheckerHandler> handlers = Collections.synchronizedList(new ArrayList<CheckerHandler>());
	
	private ResourceBundle bundle;

	public <T> void detectAnnotation(@Observes final ProcessAnnotatedType<T> event, final BeanManager beanManager) {
		if (event.getAnnotatedType().isAnnotationPresent(Checker.class)) {
			types.add(event.getAnnotatedType());
		}
	}

	public void startCheckers(@Observes final AfterDeploymentValidation event) {
		CheckerHandler handler = null;
		logger.info(getBundle(BootstrapConstants.BUNDLE_NAME).getString("bootstrap-checkers-starting"));
		for (AnnotatedType<?> type : types) {
			logger.debug(getBundle(BootstrapConstants.BUNDLE_NAME).
					getString("bootstrap-checkers-loading", type.getJavaClass().getName()));
			handler = Beans.getReference(CheckerHandler.class);
			handler.start(Beans.getReference(type.getJavaClass()));
			handlers.add(handler);
		}
	}
	
	public void shuttingDown(@Observes final BeforeShutdown event){
		logger.info(getBundle(BootstrapConstants.BUNDLE_NAME).getString("bootstrap-checkers-stopping"));
		for (CheckerHandler handler : handlers) {
			handler.stop();
		}
	}
	
	
	protected ResourceBundle getBundle(String resource) {
		if (this.bundle == null) {
			this.bundle = ResourceBundleProducer.create(resource, Locale.getDefault());
		}

		return this.bundle;
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}
	
}