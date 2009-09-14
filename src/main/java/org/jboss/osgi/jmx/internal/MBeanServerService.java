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

//$Id$

import java.util.ArrayList;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.jboss.osgi.common.log.LogServiceTracker;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * A service that registers an MBeanServer
 * 
 * @author thomas.diesler@jboss.com
 * @since 24-Apr-2009
 */
public class MBeanServerService
{
   private BundleContext context;
   private LogService log;

   public MBeanServerService(BundleContext context)
   {
      log = new LogServiceTracker(context);
      this.context = context;
   }

   @SuppressWarnings("unchecked")
   public MBeanServer registerMBeanServer()
   {
      MBeanServer mbeanServer = null;

      // Check if there is an MBeanServer service already
      ServiceReference sref = context.getServiceReference(MBeanServer.class.getName());
      if (sref != null)
      {
         mbeanServer = (MBeanServer)context.getService(sref);
         log.log(LogService.LOG_DEBUG, "Found MBeanServer fom service: " + mbeanServer.getDefaultDomain());
         return mbeanServer;
      }

      ArrayList<MBeanServer> serverArr = MBeanServerFactory.findMBeanServer(null);
      if (serverArr.size() > 1)
         throw new IllegalStateException("Multiple MBeanServer instances not supported");

      if (serverArr.size() == 1)
      {
         mbeanServer = serverArr.get(0);
         log.log(LogService.LOG_DEBUG, "Found MBeanServer: " + mbeanServer.getDefaultDomain());
      }

      if (mbeanServer == null)
      {
         log.log(LogService.LOG_DEBUG, "No MBeanServer, create one ...");
         mbeanServer = MBeanServerFactory.createMBeanServer();
      }

      // Register the MBeanServer 
      context.registerService(MBeanServer.class.getName(), mbeanServer, null);
      log.log(LogService.LOG_DEBUG, "MBeanServer registered");
      
      return mbeanServer;
   }
}