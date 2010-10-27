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


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;

import org.jboss.logging.Logger;
import org.jboss.osgi.jmx.ObjectNameFactory;
import org.jboss.osgi.jmx.ServiceStateMBeanExt;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.jmx.framework.ServiceStateMBean;

/**
 * An extension to {@link ServiceStateMBean}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Feb-2010
 */
public class ServiceStateExt extends AbstractState implements ServiceStateMBeanExt
{
   // Provide logging
   private static final Logger log = Logger.getLogger(ServiceStateExt.class);
   
   public ServiceStateExt(BundleContext context, MBeanServer mbeanServer)
   {
      super(context, mbeanServer);
   }

   @Override
   ObjectName getObjectName()
   {
      return ObjectNameFactory.create(OBJECTNAME);
   }

   @Override
   StandardMBean getStandardMBean() throws NotCompliantMBeanException
   {
      return new StandardMBean(this, ServiceStateMBeanExt.class);
   }

   @Override
   public CompositeData getService(String clazz) throws IOException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("getService: " + clazz);
      
      ServiceReference sref = context.getServiceReference(clazz);
      if (sref == null)
      {
         if (trace)
            log.trace("Cannot find service");
         return null;
      }

      CompositeDataSupport compData = getCompositeData(sref);
      if (trace)
         log.trace("Found service: " + compData);
      
      return compData;
   }

   @Override
   public TabularData getServices(String clazz, String filter) throws IOException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
         log.trace("getServices [clazz=" + clazz + ",filter=" + filter + "]");
      
      ServiceReference[] srefs;
      try
      {
         srefs = context.getServiceReferences(clazz, filter);
      }
      catch (InvalidSyntaxException e)
      {
         throw new IllegalArgumentException("Invalid filter syntax: " + filter);
      }

      if (srefs == null)
      {
         if (trace)
            log.trace("Cannot find services");
         return null;
      }

      TabularDataSupport tabularData = new TabularDataSupport(SERVICES_TYPE);
      for (ServiceReference sref : srefs)
      {
         CompositeDataSupport compData = getCompositeData(sref);
         tabularData.put(compData.get(IDENTIFIER), compData);
         if (trace)
            log.trace("Added service: " + compData);
      }
      return tabularData;
   }

   private CompositeDataSupport getCompositeData(ServiceReference sref) throws IOException
   {
      Long serviceId = (Long)sref.getProperty(Constants.SERVICE_ID);

      // Get the array of using bundles identifiers
      List<Long> usingIdentifiers = new ArrayList<Long>();
      Bundle[] usingBundles = sref.getUsingBundles();
      if (usingBundles != null)
      {
         for (Bundle aux : usingBundles)
            usingIdentifiers.add(aux.getBundleId());
      }
      Long[] usingArr = usingIdentifiers.toArray(new Long[usingIdentifiers.size()]);

      Map<String, Object> items = new HashMap<String, Object>();
      items.put(BUNDLE_IDENTIFIER, sref.getBundle().getBundleId());
      items.put(IDENTIFIER, serviceId);
      items.put(OBJECT_CLASS, sref.getProperty(Constants.OBJECTCLASS));
      items.put(USING_BUNDLES, usingArr);

      // [TODO] Remove once ServiceType does not require this item any more
      //items.put(PROPERTIES, getProperties(serviceId));

      CompositeDataSupport compData;
      try
      {
         compData = new CompositeDataSupport(SERVICE_TYPE, items);
      }
      catch (OpenDataException ex)
      {
         throw new IllegalStateException(ex);
      }
      return compData;
   }

   @Override
   public long getBundleIdentifier(long serviceId) throws IOException
   {
      long bundleId = getServiceStateMBean().getBundleIdentifier(serviceId);
      if (log.isTraceEnabled())
         log.trace("getBundleIdentifier [serviceId=" + serviceId + "] => " + bundleId);
      return bundleId;
   }

   @Override
   public String[] getObjectClass(long serviceId) throws IOException
   {
      String[] objectClass = getServiceStateMBean().getObjectClass(serviceId);
      if (log.isTraceEnabled())
         log.trace("getObjectClass [serviceId=" + serviceId + "] => " + (objectClass != null ? Arrays.asList(objectClass) : null));
      return objectClass;
   }

   @Override
   public TabularData getProperties(long serviceId) throws IOException
   {
      TabularData properties = getServiceStateMBean().getProperties(serviceId);
      if (log.isTraceEnabled())
         log.trace("getProperties [serviceId=" + serviceId + "] => " + properties);
      return properties;
   }

   @Override
   public long[] getUsingBundles(long serviceId) throws IOException
   {
      long[] usingBundles = getServiceStateMBean().getUsingBundles(serviceId);
      if (log.isTraceEnabled())
         log.trace("getUsingBundles [serviceId=" + serviceId + "] => " + (usingBundles != null ? Arrays.asList(usingBundles) : null));
      return usingBundles;
   }

   @Override
   public TabularData listServices() throws IOException
   {
      TabularData services = getServiceStateMBean().listServices();
      if (log.isTraceEnabled())
         log.trace("listServices: " + services);
      return services;
   }
}