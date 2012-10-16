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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * A daemon thread that waits for and forwards data items to a Zabbix server.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 * @author SERPRO
 */
public final class ZabbixSender extends Thread {
	
	private Logger logger = LoggerProducer.create(ZabbixSender.class);
	
	private final ResourceBundle bundle;

    private static final String ZBX_EOF = "ZBX_EOF";
    private static final String ZBX_GET_ACTIVE_CHECKS = "ZBX_GET_ACTIVE_CHECKS\n";

    private final BlockingQueue<Item> queue;
    private final InetAddress zabbixServer;
    private final int zabbixPort;
    private final String zabbixHost;
    private final String head;

    private static final String middle = "</key><data>";
    private static final String tail = "</data></req>";

    private final byte[] response = new byte[1024];

    private boolean stopping = false;

    private static final int TIMEOUT = 5 * 1000;

    /**
     * Create a new background sender.
     * 
     * @param queue			The queue to get data items from.
     * @param zabbixServer	The name or IP of the machine to send the data to.
     * @param zabbixPort	The port number on that machine.
     * @param host			The host name, as defined in the host definition in Zabbix.
     * @param bundle		The resource bundle.
     */
    public ZabbixSender(final BlockingQueue<Item> queue, final InetAddress zabbixServer,
    		final int zabbixPort, final String zabbixHost, final ResourceBundle bundle) {
    	
        super("Zabbix-Sender");
        setDaemon(true);

        this.queue = queue;

        this.zabbixServer = zabbixServer;
        this.zabbixPort = zabbixPort;
        this.zabbixHost = zabbixHost;
        this.bundle = bundle;

        this.head = "<req><host>" + encodeBase64(zabbixHost) + "</host><key>";
    }

    /**
     * Indicate that we are about to stop.
     */
    public void stopping() {
        stopping = true;
        interrupt();
    }

    public void run() {
    	
        while (!stopping) {
            try {
                final Item item = queue.take();
                send(item.getKey(), item.getValue());
            } catch (InterruptedException e) {
                if (!stopping) {
                    logger.warn(bundle.getString("zabbix-sender-ignoring-exception"), e);
                }
            } catch (Exception e) {
            	logger.warn(bundle.getString("zabbix-sender-ignoring-exception"), e);
            }
        }

        // drain the queue
        while (queue.size() > 0) {
            final Item item = queue.remove();
            try {
                send(item.getKey(), item.getValue());
            } catch (Exception e) {
            	logger.warn(bundle.getString("zabbix-sender-ignoring-exception"), e);
            }
        }
    }

    /**
     * @param key
     * @param value
     * @throws IOException
     */
    private void send(final String key, final String value) throws IOException {
    	
        final long start = System.currentTimeMillis();

        final StringBuilder message = new StringBuilder(head);
        message.append(encodeBase64(key));
        message.append(middle);
        message.append(encodeBase64(value == null ? "" : value));
        message.append(tail);

        logger.debug(bundle.getString("zabbix-sender-sending-message", this.zabbixHost, key, value));
        logger.trace(bundle.getString("zabbix-sender-detailed-message", message));
        
        Socket socket = null;
        OutputStreamWriter out = null;
        InputStream in = null;
        
        try {
            socket = new Socket(zabbixServer, zabbixPort);
            socket.setSoTimeout(TIMEOUT);

            out = new OutputStreamWriter(socket.getOutputStream());
            out.write(message.toString());
            out.flush();

            in = socket.getInputStream();
            final int read = in.read(response);
            
            final String resp = new String(response);
            logger.debug(bundle.getString("zabbix-sender-received-response", resp));
            if (read != 2 || response[0] != 'O' || response[1] != 'K') {
                logger.warn(bundle.getString("zabbix-sender-unexpected-response", key, resp));
            }
            
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
        }

        final long elapsed = System.currentTimeMillis() - start;
        logger.trace(bundle.getString("zabbix-sender-message-sent", elapsed));
    }

    /**
     * @param string
     * @return
     */
    private String encodeBase64(String string) {
    	return new String(Base64.encodeBase64(string.getBytes()));
    }
    
	/**
	 * Retrieves all active checks configured in the server.
	 * 
	 * @param hostname
	 * @return	List<ActiveCheck>
	 * @throws IOException
	 */
	public List<ActiveCheck> getActiveChecks(String hostname) throws IOException {

		List<ActiveCheck> list = new ArrayList<ActiveCheck>();

        Socket socket = null;
		OutputStream out = null;
		BufferedReader brin = null;
		
        try {
            socket = new Socket(zabbixServer, zabbixPort);
            socket.setSoTimeout(TIMEOUT);

    		out = socket.getOutputStream();
    		brin = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    		// send request to Zabbix server and wait for the list of items to be returned
    		out.write(createGetActiveChecksRequest(hostname));
    		
    		while (!socket.isClosed()) {
    			String line = brin.readLine();
    			
    			if (line == null)
    				break;
    			
    			// all active checks received
    			if (line.startsWith(ZBX_EOF))
    				break;
    			
    			list.add(parseActiveCheck(hostname, line));
    		}
    		
        } finally {
            if (brin != null) {
                brin.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
        }
		
		return list;
	}
	
	/**
	 * Create the request for the active checks
	 *  
	 * @param hostname to request active checks for
	 * @return byte array containing the bytes to send
	 */
	protected byte[] createGetActiveChecksRequest(String hostname) {
		
		StringBuffer request = new StringBuffer();
		request.append(ZBX_GET_ACTIVE_CHECKS);
		request.append(hostname);
		request.append('\n');
		
		return encodeString(request.toString());
	}

	/**
	 * Encodes data for transmission to the server.
	 * 
	 * This method encodes the data in the ASCII encoding, defaulting to
	 * the platform default encoding if that is somehow unavailable.
	 * 
	 * @param data
	 * @return byte[] containing the encoded data
	 */
	protected byte[] encodeString(String data) {
		try {
			return data.getBytes("ASCII");
		} catch (UnsupportedEncodingException e) {
			return data.getBytes();
		}
	}
	
	/**
	 * Tokenize the line and create an ActiveCheck object.
	 *  
	 * @param hostname
	 * @param line
	 */
	protected ActiveCheck parseActiveCheck(String hostname, String line) {
		
		int pos = line.lastIndexOf(':');
		long lastLogSize = Long.parseLong(line.substring(pos + 1));
		line = line.substring(0, pos);
		pos = line.lastIndexOf(':');
		int refreshInterval = Integer.parseInt(line.substring(pos + 1));
		String key = line.substring(0, pos);
		
		return new ActiveCheck(hostname, key, refreshInterval, lastLogSize);
	}

}
