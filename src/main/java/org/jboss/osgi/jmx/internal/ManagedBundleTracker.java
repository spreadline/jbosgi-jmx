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

import org.jboss.logging.Logger;
import org.jboss.osgi.spi.management.ManagedBundle;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;

/**
 * Track and register/unregister bundles with the MBeanServer.
 * 
 * @author thomas.diesler@jboss.com
 * @since 29-Oct-2009
 */
public class ManagedBundleTracker extends BundleTracker
{
   // Provide logging
   private static final Logger log = Logger.getLogger(ManagedBundleTracker.class);

   private BundleContext systemContext;
   private MBeanServer mbeanServer;

   public ManagedBundleTracker(BundleContext context, MBeanServer mbeanServer)
   {
      super(context, Bundle.INSTALLED | Bundle.RESOLVED | Bundle.ACTIVE | Bundle.UNINSTALLED, null);
      this.mbeanServer = mbeanServer;
      this.systemContext = context;
   }

   @Override
   public Object addingBundle(Bundle bundle, BundleEvent event)
   {
      Object retObject = super.addingBundle(bundle, event);
      register(bundle);
      return retObject;
   }

   @Override
   public void modifiedBundle(Bundle bundle, BundleEvent event, Object object)
   {
      if (event != null && event.getType() == BundleEvent.UNINSTALLED)
         unregister(bundle);
   }

   private ManagedBundle register(Bundle bundle)
   {
      if (log.isTraceEnabled())
         log.trace("register(" + bundle + ")");

      try
      {
         ManagedBundle mb = new ManagedBundle(systemContext, bundle);
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

   private void unregister(Bundle bundle)
   {
      if (log.isTraceEnabled())
         log.trace("unregister(" + bundle + ")");

      try
      {
         ObjectName oname = ManagedBundle.getObjectName(bundle);
         if (mbeanServer.isRegistered(oname))
         {
            log.debug("Unregister managed bundle: " + oname);
            mbeanServer.unregisterMBean(oname);
         }

      }
      catch (JMException ex)
      {
         log.error("Cannot register managed bundle", ex);
      }
   }
}