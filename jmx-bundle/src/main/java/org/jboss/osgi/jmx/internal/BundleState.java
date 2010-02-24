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
import java.util.Dictionary;

import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.jboss.logging.Logger;
import org.jboss.osgi.jmx.BundleStateMBeanExt;
import org.jboss.osgi.spi.management.ObjectNameFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.jmx.framework.BundleStateMBean;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * An extension to {@link BundleStateMBean}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Feb-2010
 */
public class BundleState extends AbstractStateMBean implements BundleStateMBeanExt
{
   public BundleState(BundleContext context, MBeanServer mbeanServer)
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
      Bundle bundle = assertBundle(bundleId);
      File dataFile = bundle.getBundleContext().getDataFile(filename);
      return dataFile.getCanonicalPath();
   }

   @Override
   public String getEntry(long bundleId, String path) throws IOException
   {
      Bundle bundle = assertBundle(bundleId);
      URL entry = bundle.getEntry(path);
      return entry.toExternalForm();
   }

   @Override
   public TabularData getHeaders(long bundleId, String locale) throws IOException
   {
      Bundle bundle = assertBundle(bundleId);
      Dictionary<String, String> headers = bundle.getHeaders(locale);
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public String getResource(long bundleId, String name) throws IOException
   {
      Bundle bundle = assertBundle(bundleId);
      URL entry = bundle.getResource(name);
      return entry.toExternalForm();
   }

   @Override
   public long loadClass(long bundleId, String name) throws ClassNotFoundException, IOException
   {
      Bundle bundle = assertBundle(bundleId);
      Class<?> clazz = bundle.loadClass(name);
      ServiceReference sref = context.getServiceReference(PackageAdmin.class.getName());
      PackageAdmin service = (PackageAdmin)context.getService(sref);
      Bundle exporter = service.getBundle(clazz);
      if (exporter == null)
         return 0;
      
      return exporter.getBundleId();
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
}