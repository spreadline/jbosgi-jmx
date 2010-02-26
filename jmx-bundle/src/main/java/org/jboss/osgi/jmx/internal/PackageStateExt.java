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

import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;
import javax.management.openmbean.TabularData;

import org.jboss.osgi.jmx.ObjectNameFactory;
import org.jboss.osgi.jmx.PackageStateMBeanExt;
import org.osgi.framework.BundleContext;
import org.osgi.jmx.framework.BundleStateMBean;

/**
 * An extension to {@link BundleStateMBean}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 23-Feb-2010
 */
public class PackageStateExt extends AbstractState implements PackageStateMBeanExt
{
   public PackageStateExt(BundleContext context, MBeanServer mbeanServer)
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
      return new StandardMBean(this, PackageStateMBeanExt.class);
   }

   public long getExportingBundle(String arg0, String arg1) throws IOException
   {
      return getPackageStateMBean().getExportingBundle(arg0, arg1);
   }

   public long[] getImportingBundles(String arg0, String arg1) throws IOException
   {
      return getPackageStateMBean().getImportingBundles(arg0, arg1);
   }

   public boolean isRemovalPending(String arg0, String arg1) throws IOException
   {
      return getPackageStateMBean().isRemovalPending(arg0, arg1);
   }

   public TabularData listPackages() throws IOException
   {
      return getPackageStateMBean().listPackages();
   }
}