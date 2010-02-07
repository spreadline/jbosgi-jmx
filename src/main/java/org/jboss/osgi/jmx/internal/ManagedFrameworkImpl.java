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

import static org.jboss.osgi.spi.OSGiConstants.DOMAIN_NAME;
import static org.jboss.osgi.spi.management.ManagedBundle.PROPERTY_ID;
import static org.jboss.osgi.spi.management.ManagedBundle.PROPERTY_SYMBOLIC_NAME;
import static org.jboss.osgi.spi.management.ManagedBundle.PROPERTY_VERSION;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.StandardMBean;

import org.jboss.osgi.spi.management.ManagedBundle;
import org.jboss.osgi.spi.management.ManagedFrameworkMBean;
import org.jboss.osgi.spi.management.ManagedServiceReference;
import org.jboss.osgi.spi.management.ObjectNameFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The managed view of an OSGi Framework
 * 
 * @author thomas.diesler@jboss.com
 * @since 04-Mar-2009
 */
public class ManagedFrameworkImpl implements ManagedFrameworkMBean
{
   // Provide logging
   final Logger log = LoggerFactory.getLogger(ManagedFrameworkImpl.class);

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

   public BundleContext getBundleContext()
   {
      return context;
   }

   @SuppressWarnings("unchecked")
   public ObjectName getBundle(String symbolicName, String version)
   {
      ObjectName oname = null;

      String namestr = DOMAIN_NAME + ":" + PROPERTY_SYMBOLIC_NAME + "=" + symbolicName + "," + PROPERTY_VERSION + "=" + version + ",*";
      Set<ObjectName> names = mbeanServer.queryNames(ObjectNameFactory.create(namestr), null);

      if (names.size() > 0)
      {
         if (names.size() > 1)
            log.warn("Multiple bundles found: " + names);

         // Use the bundle with the highest id
         for (ObjectName aux : names)
         {
            if (oname == null)
               oname = aux;

            Integer bestId = new Integer(oname.getKeyProperty(PROPERTY_ID));
            Integer auxId = new Integer(aux.getKeyProperty(PROPERTY_ID));
            if (bestId < auxId)
               oname = aux;
         }
      }

      if (log.isTraceEnabled())
         log.trace("getBundle(" + symbolicName + "," + version + ") => " + oname);

      return oname;
   }

   @SuppressWarnings("unchecked")
   public ObjectName getBundle(long bundleId)
   {
      ObjectName oname = null;

      ObjectName pattern = ObjectNameFactory.create(DOMAIN_NAME + ":" + PROPERTY_ID + "=" + bundleId + ",*");
      Set<ObjectName> names = mbeanServer.queryNames(pattern, null);

      if (names.size() > 0)
         oname = names.iterator().next();

      if (log.isTraceEnabled())
         log.trace("getBundle(" + bundleId + ") => " + oname);
      
      return oname;
   }

   @SuppressWarnings("unchecked")
   public Set<ObjectName> getBundles()
   {
      // [JBAS-6571] JMX filtering does not work with wildcards
      // ObjectName pattern = ObjectNameFactory.create(Constants.DOMAIN_NAME + ":name=*,*");
      // Set<ObjectName> names = mbeanServer.queryNames(pattern, null);

      ObjectName pattern = ObjectNameFactory.create(DOMAIN_NAME + ":*");
      Set<ObjectName> names = mbeanServer.queryNames(pattern, new IsBundleQueryExp());
      
      if (log.isTraceEnabled())
         log.trace("getBundles() => " + names);
      
      return names;
   }

   public ManagedServiceReference getServiceReference(String clazz)
   {
      ServiceReference sref = getBundleContext().getServiceReference(clazz);
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

   public ManagedServiceReference[] getServiceReferences(String clazz, String filter)
   {
      List<ManagedServiceReference> foundRefs = new ArrayList<ManagedServiceReference>();

      ServiceReference[] srefs;
      try
      {
         srefs = getBundleContext().getServiceReferences(clazz, filter);
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

   public void refreshPackages(ObjectName[] objectNames)
   {
      if (log.isTraceEnabled())
         log.trace("refreshPackages(" + Arrays.asList(objectNames) +")");
      
      Bundle[] bundleArr = getBundles(objectNames);
      ServiceReference sref = getBundleContext().getServiceReference(PackageAdmin.class.getName());
      PackageAdmin service = (PackageAdmin)getBundleContext().getService(sref);
      service.refreshPackages(bundleArr);
   }

   public boolean resolveBundles(ObjectName[] objectNames)
   {
      if (log.isTraceEnabled())
         log.trace("resolveBundles(" + Arrays.asList(objectNames) +")");
      
      Bundle[] bundleArr = getBundles(objectNames);
      ServiceReference sref = getBundleContext().getServiceReference(PackageAdmin.class.getName());
      PackageAdmin service = (PackageAdmin)getBundleContext().getService(sref);
      return service.resolveBundles(bundleArr);
   }

   private Bundle[] getBundles(ObjectName[] objectNames)
   {
      Bundle[] bundleArr = null;
      if (objectNames != null)
      {
         List<String> symbolicNames = new ArrayList<String>();
         for (ObjectName oname : objectNames)
            symbolicNames.add(oname.getKeyProperty(PROPERTY_SYMBOLIC_NAME));

         Set<Bundle> bundleSet = new HashSet<Bundle>();
         for (Bundle bundle : getBundleContext().getBundles())
         {
            if (symbolicNames.contains(bundle.getSymbolicName()))
               bundleSet.add(bundle);
         }
         bundleArr = new Bundle[bundleSet.size()];
         bundleSet.toArray(bundleArr);
      }
      return bundleArr;
   }

   public void start()
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

   public void stop()
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

   // Accept names like "jboss.osgi:id=*"
   static class IsBundleQueryExp implements QueryExp
   {
      private static final long serialVersionUID = 1L;

      public boolean apply(ObjectName name)
      {
         return name.getKeyProperty(ManagedBundle.PROPERTY_SYMBOLIC_NAME) != null;
      }

      public void setMBeanServer(MBeanServer server)
      {
      }
   }
}