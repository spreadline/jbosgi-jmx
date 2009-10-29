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

import org.jboss.osgi.spi.management.ManagedBundle;
import org.jboss.osgi.spi.management.ManagedBundleService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A service that registers an MBeanServer
 * 
 * @author thomas.diesler@jboss.com
 * @since 24-Apr-2009
 */
public class ManagedBundleServiceImpl implements ManagedBundleService
{
   // Provide logging
   private Logger log = LoggerFactory.getLogger(ManagedBundleServiceImpl.class);
   
   private MBeanServer mbeanServer;

   public ManagedBundleServiceImpl(BundleContext context, MBeanServer mbeanServer)
   {
      this.mbeanServer = mbeanServer;

      // Start tracking bundles
      ManagedBundleTracker bundleTracker = new ManagedBundleTracker(context);
      bundleTracker.open();
   }

   public ManagedBundle register(Bundle bundle)
   {
      try
      {
         ManagedBundle mb = new ManagedBundle(bundle);
         ObjectName oname = mb.getObjectName();

         if (mbeanServer.isRegistered(oname) == false)
         {
            log.debug("Register managed bundle: " + oname);
            mbeanServer.registerMBean(mb, oname);
         }

         return mb;
      }
      catch (JMException ex)
      {
         log.error("Cannot register managed bundle", ex);
         return null;
      }
   }

   public void unregister(Bundle bundle)
   {
      try
      {
         ManagedBundle mb = new ManagedBundle(bundle);
         ObjectName oname = mb.getObjectName();

         log.debug("Unregister managed bundle: " + oname);
         if (mbeanServer.isRegistered(oname))
            mbeanServer.unregisterMBean(oname);

      }
      catch (JMException ex)
      {
         log.error("Cannot register managed bundle", ex);
      }
   }

   class ManagedBundleTracker extends BundleTracker
   {
      ManagedBundleTracker(BundleContext context)
      {
         super(context, Bundle.INSTALLED | Bundle.RESOLVED | Bundle.ACTIVE | Bundle.UNINSTALLED, null);
      }

      @Override
      public Object addingBundle(Bundle bundle, BundleEvent event)
      {
         Object retObject = super.addingBundle(bundle, event);
         register(bundle);
         return retObject;
      }

      @Override
      public void removedBundle(Bundle bundle, BundleEvent event, Object object)
      {
         unregister(bundle);
         super.removedBundle(bundle, event, object);
      }
   }
}