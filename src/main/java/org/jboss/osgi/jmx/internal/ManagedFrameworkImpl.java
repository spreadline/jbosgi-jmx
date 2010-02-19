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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.StandardMBean;

import org.jboss.logging.Logger;
import org.jboss.osgi.spi.management.ManagedFrameworkMBean;
import org.jboss.osgi.spi.management.ManagedServiceReference;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * The managed view of an OSGi Framework
 * 
 * @author thomas.diesler@jboss.com
 * @since 04-Mar-2009
 */
public class ManagedFrameworkImpl implements ManagedFrameworkMBean
{
   // Provide logging
   private static final Logger log = Logger.getLogger(ManagedFrameworkImpl.class);

   private MBeanServer mbeanServer;
   private BundleContext context;
   private ManagedBundleTracker bundleTracker;

   public ManagedFrameworkImpl(BundleContext context, MBeanServer mbeanServer)
   {
      if (context == null)
         throw new IllegalArgumentException("Null BundleContext");
      if (mbeanServer == null)
         throw new IllegalArgumentException("Null MBeanServer");

      if (context.getBundle().getBundleId() != 0)
         throw new IllegalArgumentException("Not the system bundle context: " + context);

      this.context = context;
      this.mbeanServer = mbeanServer;
      this.bundleTracker = new ManagedBundleTracker(context, mbeanServer);
   }

   @Override
   public ManagedServiceReference getServiceReference(String clazz)
   {
      ServiceReference sref = context.getServiceReference(clazz);
      if (sref == null)
         return null;

      Map<String, Object> props = new HashMap<String, Object>();
      for (String key : sref.getPropertyKeys())
      {
         props.put(key, sref.getProperty(key));
      }

      ManagedServiceReference msref = new ManagedServiceReference(props);
      if (log.isTraceEnabled())
         log.trace("getServiceReference(" + clazz + ") => " + msref);
      
      return msref;
   }

   @Override
   public ManagedServiceReference[] getServiceReferences(String clazz, String filter)
   {
      List<ManagedServiceReference> foundRefs = new ArrayList<ManagedServiceReference>();

      ServiceReference[] srefs;
      try
      {
         srefs = context.getServiceReferences(clazz, filter);
      }
      catch (InvalidSyntaxException e)
      {
         throw new IllegalArgumentException("Invalid filter syntax: " + filter);
      }

      if (srefs != null)
      {
         for (ServiceReference sref : srefs)
         {
            Map<String, Object> props = new HashMap<String, Object>();
            for (String key : sref.getPropertyKeys())
               props.put(key, sref.getProperty(key));

            foundRefs.add(new ManagedServiceReference(props));
         }
      }

      ManagedServiceReference[] msrefs = null;
      if (foundRefs.size() > 0)
         msrefs = foundRefs.toArray(new ManagedServiceReference[foundRefs.size()]);

      if (log.isTraceEnabled())
         log.trace("getServiceReferences(" + clazz + "," + filter +") => " + msrefs);
      
      return msrefs;
   }

   @Override
   public void refreshAllPackages()
   {
      if (log.isTraceEnabled())
         log.trace("refreshPackages(null)");
      
      ServiceReference sref = context.getServiceReference(PackageAdmin.class.getName());
      PackageAdmin service = (PackageAdmin)context.getService(sref);
      service.refreshPackages(null);
   }

   @Override
   public boolean resolveAllBundles()
   {
      if (log.isTraceEnabled())
         log.trace("resolveBundles(null)");
      
      ServiceReference sref = context.getServiceReference(PackageAdmin.class.getName());
      PackageAdmin service = (PackageAdmin)context.getService(sref);
      return service.resolveBundles(null);
   }

   void start()
   {
      // Start tracking the bundles
      bundleTracker.open();

      try
      {
         if (mbeanServer != null)
         {
            StandardMBean mbean = new StandardMBean(this, ManagedFrameworkMBean.class);
            mbeanServer.registerMBean(mbean, ManagedFrameworkMBean.MBEAN_MANAGED_FRAMEWORK);
         }
      }
      catch (JMException ex)
      {
         log.warn("Cannot register: " + ManagedFrameworkMBean.MBEAN_MANAGED_FRAMEWORK);
      }
   }

   void stop()
   {
      try
      {
         if (mbeanServer != null && mbeanServer.isRegistered(MBEAN_MANAGED_FRAMEWORK))
            mbeanServer.unregisterMBean(ManagedFrameworkMBean.MBEAN_MANAGED_FRAMEWORK);
      }
      catch (JMException ex)
      {
         log.warn("Cannot register: " + ManagedFrameworkMBean.MBEAN_MANAGED_FRAMEWORK);
      }
   }
}