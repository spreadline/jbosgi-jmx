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
import java.util.Arrays;

import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import javax.management.openmbean.CompositeData;

import org.jboss.logging.Logger;
import org.jboss.osgi.jmx.FrameworkMBeanExt;
import org.jboss.osgi.jmx.ObjectNameFactory;
import org.osgi.framework.BundleContext;
import org.osgi.jmx.framework.FrameworkMBean;

/**
 * An extension to {@link FrameworkMBean}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Feb-2009
 */
public class FrameworkStateExt extends AbstractState implements FrameworkMBeanExt
{
   // Provide logging
   private static final Logger log = Logger.getLogger(FrameworkStateExt.class);
   
   public FrameworkStateExt(BundleContext context, MBeanServer mbeanServer)
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
      return new StandardMBean(this, FrameworkMBeanExt.class);
   }

   @Override
   public void refreshBundles(long[] bundleIds) throws IOException
   {
      if (log.isTraceEnabled())
         log.trace("refreshBundles " + (bundleIds != null ? Arrays.asList(bundleIds) : null));
      getFrameworkMBean().refreshBundles(bundleIds);
   }

   @Override
   public void refreshBundle(long bundleId) throws IOException
   {
      if (log.isTraceEnabled())
         log.trace("refreshBundle " + bundleId);
      getFrameworkMBean().refreshBundle(bundleId);
   }

   @Override
   public boolean resolveBundles(long[] bundleIds) throws IOException
   {
      boolean result = getFrameworkMBean().resolveBundles(bundleIds);
      if (log.isTraceEnabled())
         log.trace("resolveBundles " + (bundleIds != null ? Arrays.asList(bundleIds) : null) + " => " + result);
      return result;
   }

   @Override
   public boolean resolveBundle(long bundleId) throws IOException
   {
      boolean result = getFrameworkMBean().resolveBundle(bundleId);
      if (log.isTraceEnabled())
         log.trace("resolveBundle " + bundleId + " => " + result);
      return result;
   }

   @Override
   public int getFrameworkStartLevel() throws IOException
   {
      int result = getFrameworkMBean().getFrameworkStartLevel();
      if (log.isTraceEnabled())
         log.trace("getFrameworkStartLevel => " + result);
      return result;
   }

   @Override
   public int getInitialBundleStartLevel() throws IOException
   {
      int result = getFrameworkMBean().getInitialBundleStartLevel();
      if (log.isTraceEnabled())
         log.trace("getInitialBundleStartLevel => " + result);
      return result;
   }

   @Override
   public long installBundleFromURL(String location, String url) throws IOException
   {
      long result = getFrameworkMBean().installBundleFromURL(location, url);
      if (log.isTraceEnabled())
         log.trace("installBundleFromURL [location=" + location + ",url=" + url + "] => " + result);
      return result;
   }

   @Override
   public long installBundle(String location) throws IOException
   {
      long result = getFrameworkMBean().installBundle(location);
      if (log.isTraceEnabled())
         log.trace("installBundle [location=" + location + "] => " + result);
      return result;
   }

   @Override
   public CompositeData installBundlesFromURL(String[] locations, String[] urls) throws IOException
   {
      CompositeData result = getFrameworkMBean().installBundlesFromURL(locations, urls);
      if (log.isTraceEnabled())
         log.trace("installBundlesFromURL [locations=" + Arrays.asList(locations) + ",urls=" + Arrays.asList(urls) + "] => " + result);
      return result;
   }

   @Override
   public CompositeData installBundles(String[] locations) throws IOException
   {
      CompositeData result = getFrameworkMBean().installBundles(locations);
      if (log.isTraceEnabled())
         log.trace("installBundles [locations=" + Arrays.asList(locations) + "] => " + result);
      return result;
   }

   @Override
   public void updateBundleFromURL(long bundleId, String url) throws IOException
   {
      if (log.isTraceEnabled())
         log.trace("updateBundleFromURL [bundleId=" + bundleId + ",url=" + url + "]");
      getFrameworkMBean().updateBundleFromURL(bundleId, url);
   }

   @Override
   public void updateBundle(long bundleId) throws IOException
   {
      if (log.isTraceEnabled())
         log.trace("updateBundle: " + bundleId);
      getFrameworkMBean().updateBundle(bundleId);
   }

   @Override
   public CompositeData updateBundlesFromURL(long[] bundleIds, String[] urls) throws IOException
   {
      CompositeData result = getFrameworkMBean().updateBundlesFromURL(bundleIds, urls);
      if (log.isTraceEnabled())
         log.trace("updateBundlesFromURL [bundleId=" + Arrays.asList(bundleIds) + ",url=" + Arrays.asList(urls) + "] => " + result);
      return result;
   }

   @Override
   public CompositeData updateBundles(long[] bundleIds) throws IOException
   {
      CompositeData result = getFrameworkMBean().updateBundles(bundleIds);
      if (log.isTraceEnabled())
         log.trace("updateBundles [bundleId=" + Arrays.asList(bundleIds) + "] => " + result);
      return result;
   }

   @Override
   public void uninstallBundle(long bundleId) throws IOException
   {
      if (log.isTraceEnabled())
         log.trace("uninstallBundle: " + bundleId);
      getFrameworkMBean().uninstallBundle(bundleId);
   }

   @Override
   public CompositeData uninstallBundles(long[] bundleIds) throws IOException
   {
      CompositeData result = getFrameworkMBean().uninstallBundles(bundleIds);
      if (log.isTraceEnabled())
         log.trace("uninstallBundles: " + (bundleIds != null ? Arrays.asList(bundleIds) : null) + " => " + result);
      return result;
   }

   @Override
   public void restartFramework() throws IOException
   {
      if (log.isTraceEnabled())
         log.trace("restartFramework");
      getFrameworkMBean().restartFramework();
   }

   @Override
   public void setBundleStartLevel(long bundleId, int newlevel) throws IOException
   {
      if (log.isTraceEnabled())
         log.trace("setBundleStartLevel [bundleid=" + bundleId + ",level=" + newlevel +"]");
      getFrameworkMBean().setBundleStartLevel(bundleId, newlevel);
   }

   @Override
   public CompositeData setBundleStartLevels(long[] bundleIds, int[] newlevels) throws IOException
   {
      CompositeData result = getFrameworkMBean().setBundleStartLevels(bundleIds, newlevels);
      if (log.isTraceEnabled())
         log.trace("setBundleStartLevels [bundleids=" + Arrays.asList(bundleIds) + ",level=" + Arrays.asList(newlevels) +"]");
      return result;
   }

   @Override
   public void setFrameworkStartLevel(int newlevel) throws IOException
   {
      if (log.isTraceEnabled())
         log.trace("setFrameworkStartLevel [level=" + newlevel +"]");
      getFrameworkMBean().setFrameworkStartLevel(newlevel);
   }

   @Override
   public void setInitialBundleStartLevel(int newlevel) throws IOException
   {
      if (log.isTraceEnabled())
         log.trace("setInitialBundleStartLevel [level=" + newlevel +"]");
      getFrameworkMBean().setInitialBundleStartLevel(newlevel);
   }

   @Override
   public void shutdownFramework() throws IOException
   {
      if (log.isTraceEnabled())
         log.trace("shutdownFramework");
      getFrameworkMBean().shutdownFramework();
   }

   @Override
   public void startBundle(long bundleId) throws IOException
   {
      if (log.isTraceEnabled())
         log.trace("startBundle: " + bundleId);
      getFrameworkMBean().startBundle(bundleId);
   }

   @Override
   public CompositeData startBundles(long[] bundleIds) throws IOException
   {
      CompositeData result = getFrameworkMBean().startBundles(bundleIds);
      if (log.isTraceEnabled())
         log.trace("startBundles: " + (bundleIds != null ? Arrays.asList(bundleIds) : null) + " => " + result);
      return result;
   }

   @Override
   public void stopBundle(long bundleId) throws IOException
   {
      if (log.isTraceEnabled())
         log.trace("stopBundle: " + bundleId);
      getFrameworkMBean().stopBundle(bundleId);
   }

   @Override
   public CompositeData stopBundles(long[] bundleIds) throws IOException
   {
      CompositeData result = getFrameworkMBean().stopBundles(bundleIds);
      if (log.isTraceEnabled())
         log.trace("stopBundles: " + (bundleIds != null ? Arrays.asList(bundleIds) : null) + " => " + result);
      return result;
   }

   @Override
   public void updateFramework() throws IOException
   {
      getFrameworkMBean().updateFramework();
   }
}