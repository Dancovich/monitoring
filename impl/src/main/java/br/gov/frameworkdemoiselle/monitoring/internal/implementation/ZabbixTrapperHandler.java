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

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.interceptor.InvocationContext;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.monitoring.annotation.JMXQuery;
import br.gov.frameworkdemoiselle.monitoring.annotation.zabbix.HostName;
import br.gov.frameworkdemoiselle.monitoring.annotation.zabbix.ItemKey;
import br.gov.frameworkdemoiselle.monitoring.exception.TrapperException;
import br.gov.frameworkdemoiselle.monitoring.internal.configuration.zabbix.ZabbixTrapperConfig;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.zabbix.ActiveCheck;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.zabbix.Item;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.zabbix.ZabbixSender;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Handler intended to act over <b>custom Zabbix trappers</b>.
 * 
 * @author SERPRO
 */
public class ZabbixTrapperHandler {

	@Inject
	private Logger logger;

	@Inject
	private ZabbixTrapperConfig config;

	@Inject
	@Name("demoiselle-monitoring-bundle")
	private ResourceBundle bundle;	

    private ZabbixSender sender;
    private final BlockingQueue<Item> queue = new LinkedBlockingQueue<Item>();
    //private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private boolean started = false;

	private String host;
	
    private List<ActiveCheck> activeChecks = new ArrayList<ActiveCheck>();
    private Timer timer;

	@PostConstruct
	public void init() {
		logger.info(bundle.getString("trapper-zabbix-initializing"));
	}

	@PreDestroy
	public void term() {
		logger.info(bundle.getString("trapper-zabbix-terminating"));
		sender.interrupt();
	}
	
    /**
     * Create a new Zabbix trapper using default constructor.
     */
    public ZabbixTrapperHandler() {
    	super();
    }

    /**
     * Create a new Zabbix trapper using specified server name, port number, and host name.
     * 
     * @param server		The name or IP address of the machine that Zabbix runs on.
     * @param port			The port number for the Zabbix service.
     * @param host			The name of the host as defined in the hosts section in Zabbix.
     * @throws UnknownHostException	When the zabbix server name could not be resolved.
     */
    public ZabbixTrapperHandler(final String server, final int port, final String host) throws UnknownHostException {
    	this();
    	initialize(server, port, host);
    }

    /**
     * @param clz
     */
    public void initialize(final Class<?> clz) {
    	
    	final HostName hostName = clz.getAnnotation(HostName.class);
    	
		if (hostName == null) {
			this.host = config.getTrapperHost();
		} else {
			this.host = hostName.value();
		}

    	initialize(config.getTrapperServer(), config.getTrapperPort(), this.host);
    }

    /**
     * @param server
     * @param port
     * @param host
     */
    public void initialize(final String server, final int port, final String host) {
		logger.debug(bundle.getString("trapper-zabbix-initializing-server", server, port, host));
    	try {
			sender = new ZabbixSender(queue, InetAddress.getByName(server), port, host, bundle);
		} catch (UnknownHostException e) {
			throw new TrapperException(bundle.getString("trapper-zabbix-address-error", server), e);
		}
    }
    
	public void start() {
		if (!started) {
			logger.debug(bundle.getString("trapper-zabbix-starting"));
	        sender.start();
			if (config.isActiveChecks()) {
				refreshActiveChecks();
			}
			started = true;
		}
	}

    public void stop() {
		logger.debug(bundle.getString("trapper-zabbix-stopping"));
        sender.stopping();
        try {
            sender.join();
        } catch (InterruptedException e) {
            // ignore, we're done anyway...
        }
        sender = null;
    }

	private void checkStarted() {
		if (!started) {
			start();
		}
	}

    protected void refreshActiveChecks() {
    	try {
    		this.activeChecks = sender.getActiveChecks(config.getTrapperHost());
		} catch (IOException e) {
			throw new TrapperException(bundle.getString("trapper-zabbix-active-checks-error"), e);
		}
		runActiveChecks();
    }

	/**
	 * Schedule all active checks to run at their specified refresh interval. Disables
	 * all currently running active checks before scheduling the new ones.
	 */
	protected void runActiveChecks() {
		
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		
		timer = new Timer(true);
		final int refreshInterval = 60 * 1000;
		
		// schedule the refresh task before any others to prevent any exceptions
		// while adding the others to stop the agent refreshing the items
		timer.schedule(new TimerTask() {
			public void run() {
				runActiveChecks();
			}
		}, refreshInterval);

		// schedule all items as TimerTasks, each will run when it's time
		Iterator<ActiveCheck> it = activeChecks.iterator();
		while (it.hasNext()) {
			
			ActiveCheck check = (ActiveCheck) it.next();
			long delay = check.getRefreshInterval() * 1000;
			long period = delay;
			final String key = check.getKey();
			
			timer.schedule(new TimerTask() {
				public void run() {
					// FIXME: implementar isso aqui!
					final String value = null;
					//final String value = QueryHandler.makeRequest(key);
					final Item item = new Item(key, value);
					queue.offer(item);
				}
			}, delay, period);
			
			logger.debug(bundle.getString("trapper-zabbix-active-checks-scheduled",
					check.getKey(), check.getRefreshInterval()));
		}
	}

	/**
	 * Sends the trap message to the Zabbix server. To be used in a CDI context.
	 * 
	 * @param ctx	InvocationContext
	 */
	public void sendTrap(final InvocationContext ctx) {
		
		this.checkStarted();
		
		final Method method = ctx.getMethod();
		
		// TODO: pré-configurar essas anotações no handler durante o @PostConstruct
		final ItemKey itemKey = method.getAnnotation(ItemKey.class);
		final JMXQuery jmxQuery = method.getAnnotation(JMXQuery.class);
		
		final String keyString;
		if (itemKey != null) {
			keyString = itemKey.value();
		} else {
			keyString = config.getTrapperDefaultKey();
		}
		
		if (keyString == null) {
			throw new TrapperException(bundle.getString("trapper-zabbix-itemkey-absent-error"));
		}
		
		final String mbeanName = (jmxQuery != null ? jmxQuery.mbeanName() : null);
		final String mbeanAttribute = (jmxQuery != null ? jmxQuery.mbeanAttribute() : null);
		final boolean hasMBean = (mbeanName != null && !mbeanName.isEmpty());
		
		final Object[] values = ctx.getParameters();

		// FIXME: melhorar esse log
		/*
		logger.info("host = " + this.host + ", key = " + keyString +
				((jmxQuery != null) ? ", jmxQuery = " + jmxQuery.mbeanName()
				+ "[" + jmxQuery.mbeanAttribute() + "]" : "") +
				", values = " + Arrays.toString(values));
		*/

		int expectedArgsCount = countOccurrences(keyString, '*');
		int actualArgsCount = values.length;
		
		if (!hasMBean) {
			expectedArgsCount++;
		}
		
		if (expectedArgsCount != actualArgsCount) {
			throw new TrapperException(bundle.getString("trapper-zabbix-wrong-arguments-error",
					method.toString(), keyString, expectedArgsCount, actualArgsCount));
		}
		
		if (hasMBean && (mbeanAttribute == null || mbeanAttribute.isEmpty())) {
			throw new TrapperException(bundle.getString("trapper-zabbix-jmxquery-error"));
		}

		final String key, value;
		if (expectedArgsCount > 1) {
			int argPos = 0;
			final StringBuffer sb = new StringBuffer(keyString.length() + expectedArgsCount * 10);
			for (int i = 0; i < keyString.length(); i++) {
				char c = keyString.charAt(i);
				if (c == '*') {
					sb.append(values[argPos++]);
				} else {
					sb.append(c);
				}
			}
			key = sb.toString();
			value = values[argPos].toString();
		} else {
			key = keyString;
			value = (!hasMBean ? values[0].toString() : null);
		}
		
		final Item item;
		if (!hasMBean) {
			item = new Item(key, value);
		} else {
			item = new Item(key, mbeanName, mbeanAttribute);
		}
		
		queue.offer(item);
	}

	private static int countOccurrences(final String haystack, final char needle) {
		return countOccurrences(haystack, needle, 0);
	}

	private static int countOccurrences(final String haystack, final char needle, final int index) {
		if (index >= haystack.length()) {
			return 0;
		}
		int contribution = haystack.charAt(index) == needle ? 1 : 0;
		return contribution + countOccurrences(haystack, needle, index + 1);
	}

}
