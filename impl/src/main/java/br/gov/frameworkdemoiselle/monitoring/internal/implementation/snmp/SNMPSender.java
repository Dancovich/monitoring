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

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * Sender class used when delivering SNMP trap messages.
 * 
 * @author SERPRO
 */
public class SNMPSender extends Thread {

	private Logger logger = LoggerProducer.create(SNMPSender.class);
	
	private final ResourceBundle bundle;
	
	private final CommunityTarget target;
	private final BlockingQueue<PDU> queue;
	
	private boolean stopping = false;
	
	public SNMPSender(CommunityTarget target, BlockingQueue<PDU> queue, final ResourceBundle bundle) {
		this.target = target;
		this.queue = queue;
		this.bundle = bundle;
	}

    public void stopping() {
        stopping = true;
        interrupt();
    }

    public void run() {
    	
        while (!stopping) {
            try {
                final PDU pdu = queue.take();
                send(pdu);
            } catch (InterruptedException e) {
                if (!stopping) {
                    logger.warn(bundle.getString("snmp-sender-ignoring-exception"), e);
                }
            } catch (Exception e) {
            	logger.warn(bundle.getString("snmp-sender-ignoring-exception"), e);
            }
        }

        while (queue.size() > 0) {
            final PDU pdu = queue.remove();
            try {
                send(pdu);
            } catch (Exception e) {
            	logger.warn(bundle.getString("snmp-sender-ignoring-exception"), e);
            }
        }
    }
    
    private void send(final PDU pdu) {
    	
    	final long start = System.currentTimeMillis();
    	
		DefaultUdpTransportMapping udpTransportMap = null;
		try {
			udpTransportMap = new DefaultUdpTransportMapping();
		} catch (IOException e) {
			logger.error(bundle.getString("snmp-sender-udp-transport-failed"), e);
		}
		
		Snmp snmp = new Snmp(udpTransportMap);
		try {
			logger.trace(bundle.getString("snmp-sender-sending-pdu", pdu));
			snmp.send(pdu, target);
		} catch (IOException e) {
			logger.error(bundle.getString("snmp-sender-message-undelivered"), e);
		}
    	
		final long elapsed = System.currentTimeMillis() - start;
		logger.trace(bundle.getString("snmp-sender-message-sent", elapsed));
    }

}
