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

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.osgi.common.log.LogServiceTracker;
import org.jboss.osgi.spi.management.ManagedBundle;
import org.jboss.osgi.spi.management.ManagedBundleService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

/**
 * A service that registers an MBeanServer
 * 
 * @author thomas.diesler@jboss.com
 * @since 24-Apr-2009
 */
public class ManagedBundleServiceImpl implements ManagedBundleService
{
   private LogService log;
   private MBeanServer mbeanServer;
   
   public ManagedBundleServiceImpl(BundleContext context, MBeanServer mbeanServer)
   {
      this.log = new LogServiceTracker(context);
      this.mbeanServer = mbeanServer;
   }

   public ManagedBundle register(Bundle bundle)
   {
      try
      {
         ManagedBundle mb = new ManagedBundle(bundle);
         ObjectName oname = mb.getObjectName();
         
         log.log(LogService.LOG_DEBUG, "Register managed bundle: " + oname);
         mbeanServer.registerMBean(mb, oname);
         
         return mb;
      }
      catch (JMException ex)
      {
         log.log(LogService.LOG_ERROR, "Cannot register managed bundle", ex);
         return null;
      }
   }

   public void unregister(Bundle bundle)
   {
      try
      {
         ManagedBundle mb = new ManagedBundle(bundle);
         ObjectName oname = mb.getObjectName();
         
         log.log(LogService.LOG_DEBUG, "Unregister managed bundle: " + oname);
         if (mbeanServer.isRegistered(oname))
            mbeanServer.unregisterMBean(oname);
         
      }
      catch (JMException ex)
      {
         log.log(LogService.LOG_ERROR, "Cannot register managed bundle", ex);
      }
   }
}