/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.osgi.jmx;

//$Id$

/**
 * The JMX service contants
 * 
 * @author thomas.diesler@jboss.com
 * @since 24-Apr-2009
 */
public interface JMXConstantsExt
{
   /** The property that sets the host that the RMIAdaptor binds to: org.jboss.osgi.rmi.host */
   String REMOTE_RMI_HOST = "org.jboss.osgi.rmi.host";
   /** The property that sets the port that the RMI Registry binds to: org.jboss.osgi.rmi.port */
   String REMOTE_RMI_PORT = "org.jboss.osgi.rmi.port";
   
   /** The default host that the RMIAdaptor binds to: 127.0.0.1 */
   String DEFAULT_REMOTE_RMI_HOST = "127.0.0.1";
   /** The default port that the RMI Registry binds to: 1090 */
   String DEFAULT_REMOTE_RMI_PORT = "1090";
}