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
package org.jboss.osgi.jmx.internal;



import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.jboss.logging.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * A service that registers an MBeanServer
 * 
 * @author thomas.diesler@jboss.com
 * @since 24-Apr-2009
 */
public class MBeanServerService
{
   // Provide logging
   private static final Logger log = Logger.getLogger(MBeanServerService.class);

   private BundleContext context;

   public MBeanServerService(BundleContext context)
   {
      this.context = context;
   }

   public MBeanServer registerMBeanServer()
   {
      // Check if there is an MBeanServer service already
      ServiceReference sref = context.getServiceReference(MBeanServer.class.getName());
      if (sref != null)
      {
         MBeanServer mbeanServer = (MBeanServer)context.getService(sref);
         log.debug("Found MBeanServer fom service: " + mbeanServer.getDefaultDomain());
         return mbeanServer;
      }

      // Get or create th MBeanServer
      MBeanServer mbeanServer = getMBeanServer();

      // Register the MBeanServer 
      context.registerService(MBeanServer.class.getName(), mbeanServer, null);
      log.debug("MBeanServer registered");

      return mbeanServer;
   }

   private MBeanServer getMBeanServer()
   {
      MBeanServer mbeanServer = null;
      
      ArrayList<MBeanServer> serverArr = MBeanServerFactory.findMBeanServer(null);
      if (serverArr.size() > 1)
         log.warn("Multiple MBeanServer instances: " + serverArr);
   
      if (serverArr.size() > 0)
      {
         mbeanServer = serverArr.get(0);
         log.debug("Found MBeanServer: " + mbeanServer);
      }
   
      if (mbeanServer == null)
      {
         log.debug("No MBeanServer, create one ...");
         mbeanServer = MBeanServerFactory.createMBeanServer();
         log.debug("Created MBeanServer: " + mbeanServer);
      }
      
      return mbeanServer;
   }
}