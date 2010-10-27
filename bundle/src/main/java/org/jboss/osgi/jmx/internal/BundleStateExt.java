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


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.JMRuntimeException;
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
import org.jboss.osgi.jmx.BundleStateMBeanExt;
import org.jboss.osgi.jmx.ObjectNameFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.jmx.JmxConstants;
import org.osgi.jmx.framework.BundleStateMBean;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * An extension to {@link BundleStateMBean}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Feb-2010
 */
public class BundleStateExt extends AbstractState implements BundleStateMBeanExt
{
   // Provide logging
   private static final Logger log = Logger.getLogger(BundleStateExt.class);
   
   public BundleStateExt(BundleContext context, MBeanServer mbeanServer)
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
      return new StandardMBean(this, BundleStateMBeanExt.class);
   }

   @Override
   public CompositeData getBundle(long bundleId) throws IOException
   {
      TabularData bundleList = listBundles();
      CompositeData compData = bundleList.get(new Object[] { (Long)bundleId });
      if (compData == null)
         throw new IllegalArgumentException("No such bundle: " + bundleId);
      if (log.isTraceEnabled())
         log.trace("getBundle: " + bundleId + " => " + compData);
      return compData;
   }

   @Override
   public String getDataFile(long bundleId, String filename) throws IOException
   {
      BundleContext context = assertBundleContext(bundleId);
      File dataFile = context.getDataFile(filename);
      String result = dataFile != null ? dataFile.getCanonicalPath() : null;
      if (log.isTraceEnabled())
         log.trace("getDataFile [bundleId=" + bundleId + ",filename=" + filename + "] => " + result);
      return result;
   }

   @Override
   public String getEntry(long bundleId, String path) throws IOException
   {
      Bundle bundle = assertBundle(bundleId);
      URL entry = bundle.getEntry(path);
      String result = entry != null ? entry.toExternalForm() : null;
      if (log.isTraceEnabled())
         log.trace("getEntry [bundleId=" + bundleId + ",path=" + path + "] => " + result);
      return result;
   }

   @Override
   public String getResource(long bundleId, String name) throws IOException
   {
      Bundle bundle = assertBundle(bundleId);
      URL resource = bundle.getResource(name);
      String result = resource != null ? resource.toExternalForm() : null;
      if (log.isTraceEnabled())
         log.trace("getResource [bundleId=" + bundleId + ",name=" + name + "] => " + result);
      return result;
   }

   @Override
   @SuppressWarnings("unchecked")
   public TabularData getHeaders(long bundleId, String locale) throws IOException
   {
      Bundle bundle = assertBundle(bundleId);
      List<Header> result = new ArrayList<Header>();
      Dictionary<String, String> bundleHeaders = bundle.getHeaders(locale);
      Enumeration<String> keys = bundleHeaders.keys();
      while (keys.hasMoreElements())
      {
         String key = keys.nextElement();
         result.add(new Header(key, bundleHeaders.get(key)));
      }
      TabularData headerTable = new TabularDataSupport(HEADERS_TYPE);
      for (Header header : result)
      {
         headerTable.put(header.toCompositeData());
      }
      if (log.isTraceEnabled())
         log.trace("getHeaders [bundleId=" + bundleId + ",locale=" + locale + "] => " + result);
      return headerTable;
   }

   @Override
   public CompositeData getProperty(long bundleId, String key) throws IOException
   {
      CompositeData result = null;
      BundleContext bundleContext = assertBundleContext(bundleId);
      String value = bundleContext.getProperty(key);
      if (value != null)
      {
         String type = value.getClass().getSimpleName();
         
         Map<String, Object> items = new HashMap<String, Object>();
         items.put(JmxConstants.KEY, key);
         items.put(JmxConstants.VALUE, value);
         items.put(JmxConstants.TYPE, type);
         try
         {
            result = new CompositeDataSupport(JmxConstants.PROPERTY_TYPE, items);
         }
         catch (OpenDataException ex)
         {
            throw new JMRuntimeException("Failed to create CompositeData for property [" + key + ":" + value + "] - " + ex.getMessage());
         }
      }
      if (log.isTraceEnabled())
         log.trace("getProperty [bundleId=" + bundleId + ",key=" + key + "] => " + result);
      return result;
   }

   @Override
   public long loadClass(long bundleId, String name) throws ClassNotFoundException, IOException
   {
      Bundle bundle = assertBundle(bundleId);
      Class<?> clazz = bundle.loadClass(name);
      ServiceReference sref = context.getServiceReference(PackageAdmin.class.getName());
      PackageAdmin service = (PackageAdmin)context.getService(sref);
      Bundle exporter = service.getBundle(clazz);
      long result = exporter != null ? exporter.getBundleId() : 0;
      if (log.isTraceEnabled())
         log.trace("loadClass [bundleId=" + bundleId + ",name=" + name + "] => " + result);
      return result;
   }

   public String[] getExportedPackages(long bundleId) throws IOException
   {
      String[] result = getBundleStateMBean().getExportedPackages(bundleId);
      if (log.isTraceEnabled())
         log.trace("getExportedPackages [bundleId=" + bundleId + "] => " + result);
      return result;
   }

   public long[] getFragments(long bundleId) throws IOException
   {
      long[] result = getBundleStateMBean().getFragments(bundleId);
      if (log.isTraceEnabled())
         log.trace("getFragments [bundleId=" + bundleId + "] => " + (result != null ? Arrays.asList(result) : null));
      return result;
   }

   public TabularData getHeaders(long bundleId) throws IOException
   {
      TabularData result = getBundleStateMBean().getHeaders(bundleId);
      if (log.isTraceEnabled())
         log.trace("getHeaders [bundleId=" + bundleId + "] => " + result);
      return result;
   }

   public long[] getHosts(long bundleId) throws IOException
   {
      long[] result = getBundleStateMBean().getHosts(bundleId);
      if (log.isTraceEnabled())
         log.trace("getHosts [bundleId=" + bundleId + "] => " + (result != null ? Arrays.asList(result) : null));
      return result;
   }

   public String[] getImportedPackages(long bundleId) throws IOException
   {
      String[] result = getBundleStateMBean().getImportedPackages(bundleId);
      if (log.isTraceEnabled())
         log.trace("getImportedPackages [bundleId=" + bundleId + "] => " + (result != null ? Arrays.asList(result) : null));
      return result;
   }

   public long getLastModified(long bundleId) throws IOException
   {
      long result = getBundleStateMBean().getLastModified(bundleId);
      if (log.isTraceEnabled())
         log.trace("getLastModified [bundleId=" + bundleId + "] => " + new Date(result));
      return result;
   }

   public String getLocation(long bundleId) throws IOException
   {
      String result = getBundleStateMBean().getLocation(bundleId);
      if (log.isTraceEnabled())
         log.trace("getLocation [bundleId=" + bundleId + "] => " + result);
      return result;
   }

   public long[] getRegisteredServices(long bundleId) throws IOException
   {
      long[] result = getBundleStateMBean().getRegisteredServices(bundleId);
      if (log.isTraceEnabled())
         log.trace("getRegisteredServices [bundleId=" + bundleId + "] => " + (result != null ? Arrays.asList(result) : null));
      return result;
   }

   public long[] getRequiredBundles(long bundleId) throws IOException
   {
      long[] result = getBundleStateMBean().getRequiredBundles(bundleId);
      if (log.isTraceEnabled())
         log.trace("getRequiredBundles [bundleId=" + bundleId + "] => " + (result != null ? Arrays.asList(result) : null));
      return result;
   }

   public long[] getRequiringBundles(long bundleId) throws IOException
   {
      long[] result = getBundleStateMBean().getRequiringBundles(bundleId);
      if (log.isTraceEnabled())
         log.trace("getRequiringBundles [bundleId=" + bundleId + "] => " + (result != null ? Arrays.asList(result) : null));
      return result;
   }

   public long[] getServicesInUse(long bundleId) throws IOException
   {
      long[] result = getBundleStateMBean().getServicesInUse(bundleId);
      if (log.isTraceEnabled())
         log.trace("getServicesInUse [bundleId=" + bundleId + "] => " + (result != null ? Arrays.asList(result) : null));
      return result;
   }

   public int getStartLevel(long bundleId) throws IOException
   {
      int result = getBundleStateMBean().getStartLevel(bundleId);
      if (log.isTraceEnabled())
         log.trace("getStartLevel [bundleId=" + bundleId + "] => " + result);
      return result;
   }

   public String getState(long bundleId) throws IOException
   {
      String result = getBundleStateMBean().getState(bundleId);
      if (log.isTraceEnabled())
         log.trace("getState [bundleId=" + bundleId + "] => " + result);
      return result;
   }

   public String getSymbolicName(long bundleId) throws IOException
   {
      String result = getBundleStateMBean().getSymbolicName(bundleId);
      if (log.isTraceEnabled())
         log.trace("getSymbolicName [bundleId=" + bundleId + "] => " + result);
      return result;
   }

   public String getVersion(long bundleId) throws IOException
   {
      String result = getBundleStateMBean().getVersion(bundleId);
      if (log.isTraceEnabled())
         log.trace("getVersion [bundleId=" + bundleId + "] => " + result);
      return result;
   }

   public boolean isFragment(long bundleId) throws IOException
   {
      boolean result = getBundleStateMBean().isFragment(bundleId);
      if (log.isTraceEnabled())
         log.trace("isFragment [bundleId=" + bundleId + "] => " + result);
      return result;
   }

   public boolean isPersistentlyStarted(long bundleId) throws IOException
   {
      boolean result = getBundleStateMBean().isPersistentlyStarted(bundleId);
      if (log.isTraceEnabled())
         log.trace("isPersistentlyStarted [bundleId=" + bundleId + "] => " + result);
      return result;
   }

   public boolean isRemovalPending(long bundleId) throws IOException
   {
      boolean result = getBundleStateMBean().isRemovalPending(bundleId);
      if (log.isTraceEnabled())
         log.trace("isRemovalPending [bundleId=" + bundleId + "] => " + result);
      return result;
   }

   public boolean isRequired(long bundleId) throws IOException
   {
      boolean result = getBundleStateMBean().isRequired(bundleId);
      if (log.isTraceEnabled())
         log.trace("isRequired [bundleId=" + bundleId + "] => " + result);
      return result;
   }

   public TabularData listBundles() throws IOException
   {
      TabularData result = getBundleStateMBean().listBundles();
      if (log.isTraceEnabled())
         log.trace("TabularData => " + result);
      return result;
   }

   private Bundle assertBundle(long bundleId)
   {
      Bundle bundle = context.getBundle(bundleId);
      if (bundle == null)
         throw new IllegalArgumentException("No such bundle: " + bundleId);
      return bundle;
   }

   private BundleContext assertBundleContext(long bundleId)
   {
      Bundle bundle = assertBundle(bundleId);
      BundleContext bundleContext = bundle.getBundleContext();
      return bundleContext;
   }

   /*
    * Represents key/value pair in BundleData headers
    */
   static class Header
   {

      private String key;
      private String value;

      String getKey()
      {
         return key;
      }

      String getValue()
      {
         return value;
      }

      private Header()
      {
         super();
      }

      Header(String key, String value)
      {
         this.key = key;
         this.value = value;
      }

      CompositeData toCompositeData() throws JMRuntimeException
      {
         CompositeData result = null;
         Map<String, Object> items = new HashMap<String, Object>();
         items.put(KEY, key);
         items.put(VALUE, value);
         try
         {
            result = new CompositeDataSupport(HEADER_TYPE, items);
         }
         catch (OpenDataException e)
         {
            throw new JMRuntimeException("Failed to create CompositeData for header [" + key + ":" + value + "] - " + e.getMessage());
         }
         return result;
      }

      static Header from(CompositeData compositeData)
      {
         if (compositeData == null)
         {
            throw new IllegalArgumentException("Argument compositeData cannot be null");
         }
         if (!compositeData.getCompositeType().equals(HEADER_TYPE))
         {
            throw new IllegalArgumentException("Invalid CompositeType [" + compositeData.getCompositeType() + "]");
         }
         Header header = new Header();
         header.key = (String)compositeData.get(KEY);
         header.value = (String)compositeData.get(VALUE);
         return header;
      }
   }
}