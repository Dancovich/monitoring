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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.slf4j.Logger;

import br.gov.frameworkdemoiselle.internal.producer.LoggerProducer;
import br.gov.frameworkdemoiselle.monitoring.internal.implementation.jmx.MBeanHelper;
import br.gov.frameworkdemoiselle.util.ResourceBundle;

/**
 * A JMX query handler for Zabbix. The query handler reads the query from the socket, parses the
 * request and constructs and sends a response.
 * <p>
 * You can configure the protocol version to use and set it to either &quot;1.1&quot; or &quot;1.4&quot;.
 * 
 * @author Kees Jan Koster &lt;kjkoster@kjkoster.org&gt;
 * @author SERPRO
 */
public final class QueryHandler implements Runnable {
	
	private Logger logger = LoggerProducer.create(QueryHandler.class);
    
	private final ResourceBundle bundle;	

    private final Socket socket;
    private final StringBuilder hexdump = new StringBuilder();
    private final ProtocolVersion version;
    
    /**
     * The return value that Zabbix interprets as the agent not supporting the item.
     */
    private static final String NOTSUPPORTED = "ZBX_NOTSUPPORTED";

    /**
     * The return value when asked for the agent's version.
     */
    private static final String AGENT_VERSION = "Demoiselle Zabbix Agent 2.0";
    
    /**
     * Create a new query handler.
     * 
     * @param socket	The socket that was accepted.
     * @param version	The protocol version.
     * @param bundle	The resource bundle.
     */
    public QueryHandler(final Socket socket, final ProtocolVersion version, final ResourceBundle bundle) {
        this.socket = socket;
        this.version = version;
        this.bundle = bundle;
    }

	public void run() {
        try {
            logger.debug(bundle.getString("query-handler-started-worker"));
            try {
                do {
                    handleQuery();
                } while (socket.getInputStream().available() > 0);
            } finally {
                if (socket != null) {
                    socket.close();
                }
            }
            logger.debug(bundle.getString("query-handler-stopped-worker"));
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void handleQuery() throws Exception {
    	
        final String request = receive(socket.getInputStream());
        logger.debug(bundle.getString("query-handler-received-request", request));

        String response = response(request);
        
        // make sure we can send
        if (response == null) {
            response = "";
        }
        
        logger.debug(bundle.getString("query-handler-sending-response", response));
        send(response, socket.getOutputStream());
    }

    private String receive(final InputStream in) throws IOException {
        String line = "";
        int b = in.read();
        while (b != -1 && b != 0x0a) {
            line += (char) b;
            b = in.read();
        }
        return line;
    }

    private String response(final String query) throws Exception {
    	return makeRequest(query);
    }

    public String makeRequest(String query) {
    	
        final int lastOpen = query.lastIndexOf('[');
        final int lastClose = query.lastIndexOf(']');
        String attribute = null;
        if (lastOpen >= 0 && lastClose >= 0) {
            attribute = query.substring(lastOpen + 1, lastClose);
        }

        if (query.startsWith("jmx")) {
        	
            final int firstClose = query.lastIndexOf(']', lastOpen);
            final int firstOpen = query.indexOf('[');
            if (firstClose == -1 || firstOpen == -1 || attribute == null) {
                return NOTSUPPORTED;
            }

            final String objectName = query.substring(firstOpen + 1, firstClose);
            try {
            	Object ret = MBeanHelper.query(objectName, attribute);
                return (ret != null ? ret.toString() : null);
            } catch (Exception e) {
                logger.error(bundle.getString("query-handler-jmxquery-error", query), e);
                return NOTSUPPORTED;
            }
            
        } else {
        	query = query.replaceFirst("\\s+$", "");
        	
	        if (query.startsWith("system.property")) {
	            return querySystemProperty(attribute);
	            
	        } else if (query.startsWith("system.env")) {
	            return queryEnvironment(attribute);
	            
	        } else if (query.equals("agent.ping")) {
	            return "1";
	            
	        } else if (query.equals("agent.version")) {
	            return AGENT_VERSION;
	        }
        }
        
        return NOTSUPPORTED;
    }
    
    private String querySystemProperty(final String key) {
        logger.debug(bundle.getString("query-handler-system-property", key));
        return System.getProperty(key);
    }

    private String queryEnvironment(final String key) {
        logger.debug(bundle.getString("query-handler-environment-variable", key));
        return System.getenv(key);
    }

    private void send(final String response, final OutputStream outputStream) throws IOException {
    	
        final BufferedOutputStream out = new BufferedOutputStream(outputStream);

        if (isProtocol14()) {
        	
            // write magic marker
            write(out, (byte) 'Z');
            write(out, (byte) 'B');
            write(out, (byte) 'X');
            write(out, (byte) 'D');

            // write protocol version
            write(out, (byte) 0x01);

            // length as 64 bit integer, little endian format
            long length = response.length();
            for (int i = 0; i < 8; i++) {
                write(out, (byte) (length & 0xff));
                length >>= 8;
            }
        }

        // response itself
        for (int i = 0; i < response.length(); i++) {
            write(out, (byte) response.charAt(i));
        }

        out.flush();
        logger.trace(bundle.getString("query-handler-sent-bytes", hexdump));
    }

    private boolean isProtocol14() {
    	if (ProtocolVersion.V1_4.equals(version)) {
            return true;
    	} else if (ProtocolVersion.V1_1.equals(version)) {
    		return false;
    	}
        logger.warn(bundle.getString("query-handler-unsupported-protocol"));
        return true;
    }

    private void write(final BufferedOutputStream out, final byte b) throws IOException {
        final String hex = Integer.toHexString(b);
        if (hex.length() < 2) {
            hexdump.append("0");
        }
        hexdump.append(hex).append(" ");
        out.write(b);
    }
    
}
