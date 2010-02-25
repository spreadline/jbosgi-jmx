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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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

import org.jboss.osgi.jmx.BundleStateMBeanExt;
import org.jboss.osgi.spi.management.ObjectNameFactory;
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
      CompositeData bundleData = bundleList.get(new Object[] { (Long)bundleId });
      if (bundleData == null)
         throw new IllegalArgumentException("No such bundle: " + bundleId);
      return bundleData;
   }

   @Override
   public String getDataFile(long bundleId, String filename) throws IOException
   {
      BundleContext context = assertBundleContext(bundleId);
      File dataFile = context.getDataFile(filename);
      return dataFile != null ? dataFile.getCanonicalPath() : null;
   }

   @Override
   public String getEntry(long bundleId, String path) throws IOException
   {
      Bundle bundle = assertBundle(bundleId);
      URL entry = bundle.getEntry(path);
      return entry != null ? entry.toExternalForm() : null;
   }

   @Override
   public String getResource(long bundleId, String name) throws IOException
   {
      Bundle bundle = assertBundle(bundleId);
      URL resource = bundle.getResource(name);
      return resource != null ? resource.toExternalForm() : null;
   }

   @Override
   @SuppressWarnings("unchecked")
   public TabularData getHeaders(long bundleId, String locale) throws IOException
   {
      Bundle bundle = assertBundle(bundleId);
      List<Header> headers = new ArrayList<Header>();
      Dictionary<String, String> bundleHeaders = bundle.getHeaders(locale);
      Enumeration<String> keys = bundleHeaders.keys();
      while (keys.hasMoreElements())
      {
         String key = keys.nextElement();
         headers.add(new Header(key, bundleHeaders.get(key)));
      }
      TabularData headerTable = new TabularDataSupport(HEADERS_TYPE);
      for (Header header : headers)
      {
         headerTable.put(header.toCompositeData());
      }
      return headerTable;
   }

   @Override
   public CompositeData getProperty(long bundleId, String key) throws IOException
   {
      BundleContext bundleContext = assertBundleContext(bundleId);
      String value = bundleContext.getProperty(key);
      if (value == null)
         return null;
      
      String type = value.getClass().getSimpleName();
      
      Map<String, Object> items = new HashMap<String, Object>();
      items.put(JmxConstants.KEY, key);
      items.put(JmxConstants.VALUE, value);
      items.put(JmxConstants.TYPE, type);
      try
      {
         return new CompositeDataSupport(JmxConstants.PROPERTY_TYPE, items);
      }
      catch (OpenDataException ex)
      {
         throw new JMRuntimeException("Failed to create CompositeData for property [" + key + ":" + value + "] - " + ex.getMessage());
      }
   }

   @Override
   public long loadClass(long bundleId, String name) throws ClassNotFoundException, IOException
   {
      Bundle bundle = assertBundle(bundleId);
      Class<?> clazz = bundle.loadClass(name);
      ServiceReference sref = context.getServiceReference(PackageAdmin.class.getName());
      PackageAdmin service = (PackageAdmin)context.getService(sref);
      Bundle exporter = service.getBundle(clazz);
      return exporter != null ? exporter.getBundleId() : 0;
   }

   public String[] getExportedPackages(long arg0) throws IOException
   {
      return getBundleStateMBean().getExportedPackages(arg0);
   }

   public long[] getFragments(long arg0) throws IOException
   {
      return getBundleStateMBean().getFragments(arg0);
   }

   public TabularData getHeaders(long arg0) throws IOException
   {
      return getBundleStateMBean().getHeaders(arg0);
   }

   public long[] getHosts(long arg0) throws IOException
   {
      return getBundleStateMBean().getHosts(arg0);
   }

   public String[] getImportedPackages(long arg0) throws IOException
   {
      return getBundleStateMBean().getImportedPackages(arg0);
   }

   public long getLastModified(long arg0) throws IOException
   {
      return getBundleStateMBean().getLastModified(arg0);
   }

   public String getLocation(long arg0) throws IOException
   {
      return getBundleStateMBean().getLocation(arg0);
   }

   public long[] getRegisteredServices(long arg0) throws IOException
   {
      return getBundleStateMBean().getRegisteredServices(arg0);
   }

   public long[] getRequiredBundles(long arg0) throws IOException
   {
      return getBundleStateMBean().getRequiredBundles(arg0);
   }

   public long[] getRequiringBundles(long arg0) throws IOException
   {
      return getBundleStateMBean().getRequiringBundles(arg0);
   }

   public long[] getServicesInUse(long arg0) throws IOException
   {
      return getBundleStateMBean().getServicesInUse(arg0);
   }

   public int getStartLevel(long arg0) throws IOException
   {
      return getBundleStateMBean().getStartLevel(arg0);
   }

   public String getState(long arg0) throws IOException
   {
      return getBundleStateMBean().getState(arg0);
   }

   public String getSymbolicName(long arg0) throws IOException
   {
      return getBundleStateMBean().getSymbolicName(arg0);
   }

   public String getVersion(long arg0) throws IOException
   {
      return getBundleStateMBean().getVersion(arg0);
   }

   public boolean isFragment(long arg0) throws IOException
   {
      return getBundleStateMBean().isFragment(arg0);
   }

   public boolean isPersistentlyStarted(long arg0) throws IOException
   {
      return getBundleStateMBean().isPersistentlyStarted(arg0);
   }

   public boolean isRemovalPending(long arg0) throws IOException
   {
      return getBundleStateMBean().isRemovalPending(arg0);
   }

   public boolean isRequired(long arg0) throws IOException
   {
      return getBundleStateMBean().isRequired(arg0);
   }

   public TabularData listBundles() throws IOException
   {
      return getBundleStateMBean().listBundles();
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