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
package br.gov.frameworkdemoiselle.monitoring.internal.implementation.zabbix;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.monitoring.exception.AgentException;
import br.gov.frameworkdemoiselle.monitoring.internal.configuration.zabbix.ZabbixAgentConfig;
import br.gov.frameworkdemoiselle.monitoring.stereotype.Agent;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * The <b>Zabbix agent</b> implementation.
 * 
 * @author SERPRO
 */
@Agent
public class ZabbixAgent implements Runnable {

	@Inject
	private Logger logger;

	@Inject
	private ZabbixAgentConfig config;

	@Inject
	@Name("demoiselle-monitoring-bundle")
	private ResourceBundle bundle;	

	private InetAddress address;
	private int port;
	private Thread daemon;

	private ServerSocket serverSocket = null;

	private volatile boolean stopping = false;

	@PostConstruct
	public void init() {
		logger.info(bundle.getString("agent-zabbix-creating"));
		
		String listenAddress = config.getListenAddress();
		InetAddress resolved = null;
		if (listenAddress != null && !"*".equals(listenAddress)) {
			try {
				resolved = InetAddress.getByName(listenAddress);
			} catch (UnknownHostException e) {
				throw new AgentException(
						bundle.getString("agent-zabbix-listening-address-error", listenAddress), e);
			}
		}
		this.address = resolved;

		this.port = config.getAgentPort();
		this.daemon = new Thread(this, "ZabbixAgent");
	}
	
	public void startup() {
		logger.info(bundle.getString("agent-zabbix-starting-daemon",
				(this.address != null ? this.address : "0.0.0.0"), this.port));
		daemon.setDaemon(true);
		daemon.start();
	}

	public void run() {
		logger.debug(bundle.getString("agent-zabbix-instantiating-execution"));
		final ExecutorService handlers = new ThreadPoolExecutor(1, 5, 60L,
				TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

		try {
			// 0 means 'use default backlog'
			serverSocket = new ServerSocket(port, 0, address);

			while (!stopping) {
				final Socket accepted = serverSocket.accept();
				logger.debug(bundle.getString("agent-zabbix-accepted-connection",
						accepted.getInetAddress().getHostAddress()));
				handlers.execute(new QueryHandler(accepted, config.getProtocol(), bundle));
			}
			
		} catch (IOException e) {
			if (!stopping) {
				logger.error(bundle.getString("agent-zabbix-io-error", e.getMessage()));
			}
		} finally {
			try {
				if (serverSocket != null) {
					serverSocket.close();
					serverSocket = null;
				}
			} catch (IOException e) {
				// ignore, we're going down anyway...
			}

			try {
				handlers.shutdown();
				handlers.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// ignore, we're going down anyway...
			}
		}
	}

	public void shutdown() {
		logger.info(bundle.getString("agent-zabbix-shutting-down"));
		stopping = true;

		try {
			if (serverSocket != null) {
				serverSocket.close();
				serverSocket = null;
			}
		} catch (IOException e) {
			// ignore, we're going down anyway...
		}

		try {
			daemon.join();
		} catch (InterruptedException e) {
			// ignore, we're going down anyway...
		}

		logger.info(bundle.getString("agent-zabbix-done"));
	}

}
