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
public interface Constants
{
   /** The property that sets the host that the RMIAdaptor binds to: org.jboss.osgi.jmx.host */
   String REMOTE_JMX_HOST = "org.jboss.osgi.jmx.host";
   /** The property that sets the port that the RMIAdaptor binds to: org.jboss.osgi.jmx.port */
   String REMOTE_JMX_RMI_PORT = "org.jboss.osgi.jmx.rmi.port";
   /** The property that sets the JNDI name the RMIAdaptor binds to: org.jboss.osgi.jmx.rmi.adaptor */
   String REMOTE_JMX_RMI_ADAPTOR = "org.jboss.osgi.jmx.rmi.adaptor";
}