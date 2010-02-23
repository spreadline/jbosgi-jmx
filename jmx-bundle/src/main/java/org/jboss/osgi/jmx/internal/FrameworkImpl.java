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

import java.io.IOException;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import javax.management.openmbean.CompositeData;

import org.jboss.logging.Logger;
import org.jboss.osgi.jmx.FrameworkMBeanExt;
import org.jboss.osgi.spi.management.MBeanProxy;
import org.jboss.osgi.spi.management.ObjectNameFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.jmx.framework.FrameworkMBean;
import org.osgi.service.packageadmin.PackageAdmin;

/**
 * An extension to {@link FrameworkMBean}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Feb-2009
 */
public class FrameworkImpl implements FrameworkMBeanExt
{
   // Provide logging
   private static final Logger log = Logger.getLogger(FrameworkImpl.class);

   private MBeanServer mbeanServer;
   private BundleContext context;
   private FrameworkMBean delegate;

   public FrameworkImpl(BundleContext context, MBeanServer mbeanServer)
   {
      if (context == null)
         throw new IllegalArgumentException("Null BundleContext");
      if (mbeanServer == null)
         throw new IllegalArgumentException("Null MBeanServer");

      if (context.getBundle().getBundleId() != 0)
         throw new IllegalArgumentException("Not the system bundle context: " + context);

      this.context = context;
      this.mbeanServer = mbeanServer;
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
      try
      {
         ObjectName objectName = ObjectNameFactory.create(FrameworkMBeanExt.OBJECTNAME);
         StandardMBean mbean = new StandardMBean(this, FrameworkMBeanExt.class);
         mbeanServer.registerMBean(mbean, objectName);
      }
      catch (JMException ex)
      {
         log.warn("Cannot register: " + FrameworkMBeanExt.OBJECTNAME);
      }
   }

   void stop()
   {
      try
      {
         ObjectName objectName = ObjectNameFactory.create(FrameworkMBeanExt.OBJECTNAME);
         if (mbeanServer.isRegistered(objectName))
            mbeanServer.unregisterMBean(objectName);
      }
      catch (JMException ex)
      {
         log.warn("Cannot register: " + FrameworkMBeanExt.OBJECTNAME);
      }
   }

   public int getFrameworkStartLevel() throws IOException
   {
      return getFrameworkMBean().getFrameworkStartLevel();
   }

   public int getInitialBundleStartLevel() throws IOException
   {
      return getFrameworkMBean().getInitialBundleStartLevel();
   }

   public long installBundle(String arg0, String arg1) throws IOException
   {
      return getFrameworkMBean().installBundle(arg0, arg1);
   }

   public long installBundle(String arg0) throws IOException
   {
      return getFrameworkMBean().installBundle(arg0);
   }

   public CompositeData installBundles(String[] arg0, String[] arg1) throws IOException
   {
      return getFrameworkMBean().installBundles(arg0, arg1);
   }

   public CompositeData installBundles(String[] arg0) throws IOException
   {
      return getFrameworkMBean().installBundles(arg0);
   }

   public void refreshPackages(long arg0) throws IOException
   {
      getFrameworkMBean().refreshPackages(arg0);
   }

   public CompositeData refreshPackages(long[] arg0) throws IOException
   {
      return getFrameworkMBean().refreshPackages(arg0);
   }

   public boolean resolveBundle(long arg0) throws IOException
   {
      return getFrameworkMBean().resolveBundle(arg0);
   }

   public boolean resolveBundles(long[] arg0) throws IOException
   {
      return getFrameworkMBean().resolveBundles(arg0);
   }

   public void restartFramework() throws IOException
   {
      getFrameworkMBean().restartFramework();
   }

   public void setBundleStartLevel(long arg0, int arg1) throws IOException
   {
      getFrameworkMBean().setBundleStartLevel(arg0, arg1);
   }

   public CompositeData setBundleStartLevels(long[] arg0, int[] arg1) throws IOException
   {
      return getFrameworkMBean().setBundleStartLevels(arg0, arg1);
   }

   public void setFrameworkStartLevel(int arg0) throws IOException
   {
      getFrameworkMBean().setFrameworkStartLevel(arg0);
   }

   public void setInitialBundleStartLevel(int arg0) throws IOException
   {
      getFrameworkMBean().setInitialBundleStartLevel(arg0);
   }

   public void shutdownFramework() throws IOException
   {
      getFrameworkMBean().shutdownFramework();
   }

   public void startBundle(long arg0) throws IOException
   {
      getFrameworkMBean().startBundle(arg0);
   }

   public CompositeData startBundles(long[] arg0) throws IOException
   {
      return getFrameworkMBean().startBundles(arg0);
   }

   public void stopBundle(long arg0) throws IOException
   {
      getFrameworkMBean().stopBundle(arg0);
   }

   public CompositeData stopBundles(long[] arg0) throws IOException
   {
      return getFrameworkMBean().stopBundles(arg0);
   }

   public void uninstallBundle(long arg0) throws IOException
   {
      getFrameworkMBean().uninstallBundle(arg0);
   }

   public CompositeData uninstallBundles(long[] arg0) throws IOException
   {
      return getFrameworkMBean().uninstallBundles(arg0);
   }

   public void updateBundle(long arg0, String arg1) throws IOException
   {
      getFrameworkMBean().updateBundle(arg0, arg1);
   }

   public void updateBundle(long arg0) throws IOException
   {
      getFrameworkMBean().updateBundle(arg0);
   }

   public CompositeData updateBundles(long[] arg0, String[] arg1) throws IOException
   {
      return getFrameworkMBean().updateBundles(arg0, arg1);
   }

   public CompositeData updateBundles(long[] arg0) throws IOException
   {
      return getFrameworkMBean().updateBundles(arg0);
   }

   public void updateFramework() throws IOException
   {
      getFrameworkMBean().updateFramework();
   }

   private FrameworkMBean getFrameworkMBean()
   {
      if (delegate == null)
      {
         ObjectName objectName = ObjectNameFactory.create(FrameworkMBean.OBJECTNAME);
         delegate = MBeanProxy.get(mbeanServer, objectName, FrameworkMBean.class);
      }
      return delegate;
   }
}