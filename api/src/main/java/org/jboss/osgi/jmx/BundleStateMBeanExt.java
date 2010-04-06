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
package org.jboss.osgi.jmx;

//$Id$

import java.io.IOException;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.osgi.jmx.JmxConstants;
import org.osgi.jmx.framework.BundleStateMBean;

/**
 * An extension to {@link BundleStateMBean}.
 * 
 * @author thomas.diesler@jboss.com
 * @since 24-Feb-2010
 */
public interface BundleStateMBeanExt extends BundleStateMBean
{
   /** The default object name: jboss.osgi:service=jmx,type=BundleState */
   String OBJECTNAME = "jboss.osgi:service=jmx,type=BundleState";
   
   /**
    * Answer the bundle state for a single bundle. 
    * Composite Data that is type by {@link BundleStateMBean#BUNDLE_TYPE}.
    * 
    * https://www.osgi.org/members/bugzilla/show_bug.cgi?id=1598
    *    
    * @param bundleId the bundle identifier
    * @return The composite bundle information
    * @throws IOException if the operation fails 
    * @throws IllegalArgumentException if the bundle indicated does not exist
    */
   CompositeData getBundle(long bundleId) throws IOException;

   /**
    * Answer the headers for the bundle uniquely identified by the bundle id. 
    * The Tabular Data is typed by the {@link BundleStateMBean#HEADERS_TYPE}.
    * 
    * https://www.osgi.org/members/bugzilla/show_bug.cgi?id=1599
    * 
    * @param bundleId the unique identifier of the bundle
    * @param locale The locale name into which the header values are to be localized.
    * @throws IOException if the operation fails 
    * @throws IllegalArgumentException if the bundle indicated does not exist
    */
   TabularData getHeaders(long bundleId, String locale) throws IOException;

   /**
    * Answer the map of properties associated with this bundle
    * 
    * @see JmxConstants#PROPERTIES_TYPE for the details of the CompositeType
    * 
    * https://www.osgi.org/members/bugzilla/show_bug.cgi?id=1600
    * 
    * @param bundleId the unique identifier of the bundle
    * @param key The name of the requested property. 
    * @return the property data, or null if the property is undefined. 
    * @throws IOException if the operation fails
    * @throws IllegalArgumentException if the bundle indicated does not exist
    */
   CompositeData getProperty(long bundleId, String key) throws IOException;
   
   /**
    * Loads the specified class using the class loader of the bundle with the given identifier.
    * 
    * https://www.osgi.org/members/bugzilla/show_bug.cgi?id=1601
    * 
    * @param bundleId the unique identifier of the bundle
    * @param name The name of the class to load
    * @return The bundle id of the bundle that loaded the class.
    * @throws ClassNotFoundException If no such class can be found or if the given bundle is a fragment bundle
    */
   long loadClass(long bundleId, String name) throws ClassNotFoundException, IOException;

   /**
    * Returns a string encoded URL to the entry at the specified path in the given bundle.
    * 
    * https://www.osgi.org/members/bugzilla/show_bug.cgi?id=1601
    * 
    * @param bundleId the unique identifier of the bundle
    * @param path The path name of the entry
    * @return A URL to the entry, or null if no entry could be found
    */
   String getEntry(long bundleId, String path) throws IOException;

   /**
    * Find the specified resource from the given bundle's class loader.
    *  
    * https://www.osgi.org/members/bugzilla/show_bug.cgi?id=1601
    * 
    * @param bundleId the unique identifier of the bundle
    * @param name The name of the resource.
    * @return A string encoded URL to the named resource, or null if the resource could not be found
    */
   String getResource(long bundleId, String name) throws IOException;

   /**
    * Get the string encoded file path in the persistent storage area provided for the given bundle.
    * 
    * https://www.osgi.org/members/bugzilla/show_bug.cgi?id=1601
    * 
    * @param bundleId the unique identifier of the bundle
    * @param filename A relative name to the file to be accessed
    */
   String getDataFile(long bundleId, String filename) throws IOException;
}