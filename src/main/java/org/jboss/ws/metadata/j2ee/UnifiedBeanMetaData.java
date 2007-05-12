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
package org.jboss.ws.metadata.j2ee;

//$Id$

/**
 * The container independent common meta data class for the entity, message-driven and session beans. 
 *
 * @author Thomas.Diesler@jboss.org
 * @since 05-May-2006
 */
public class UnifiedBeanMetaData
{
   /** The ejb-name element specifies an enterprise bean's name. */
   private String ejbName;
   /** The ejb-class element contains the fully-qualified name of the enterprise bean's class. */
   private String ejbClass;
   /** The home element contains the fully-qualified name of the enterprise
    bean's home interface. */
   private String homeClass;
   /** The local-home element contains the fully-qualified name of the
    enterprise bean's local home interface. */
   private String localHomeClass;
   /** The service-endpoint element contains the fully-qualified name of the beans service endpoint interface (SEI) */
   protected String seiName;
   /** The JNDI name under with the home interface should be bound */
   private String jndiName;
   /** The JNDI name under with the local home interface should be bound */
   private String localJndiName;
   /** The jboss port-component binding for a ejb webservice */
   protected UnifiedEjbPortComponentMetaData portComponent;

   public String getEjbName()
   {
      return ejbName;
   }

   public void setEjbName(String ejbName)
   {
      this.ejbName = ejbName;
   }

   public String getEjbClass()
   {
      return ejbClass;
   }

   public void setEjbClass(String ejbClass)
   {
      this.ejbClass = ejbClass;
   }

   public UnifiedEjbPortComponentMetaData getPortComponent()
   {
      return portComponent;
   }

   public void setPortComponent(UnifiedEjbPortComponentMetaData portComponent)
   {
      this.portComponent = portComponent;
   }

   public String getServiceEndpointInterface()
   {
      return seiName;
   }

   public void setServiceEndpointInterface(String seiName)
   {
      this.seiName = seiName;
   }

   public String getContainerObjectNameJndiName()
   {
      return getHome() != null ? getJndiName() : getLocalJndiName();
   }

   public String getHome()
   {
      return homeClass;
   }

   public void setHome(String homeClass)
   {
      this.homeClass = homeClass;
   }

   public String getJndiName()
   {
      return jndiName;
   }

   public void setJndiName(String jndiName)
   {
      this.jndiName = jndiName;
   }

   public String getLocalHome()
   {
      return localHomeClass;
   }

   public void setLocalHome(String localHomeClass)
   {
      this.localHomeClass = localHomeClass;
   }

   public String getLocalJndiName()
   {
      return localJndiName;
   }

   public void setLocalJndiName(String localJndiName)
   {
      this.localJndiName = localJndiName;
   }

}
