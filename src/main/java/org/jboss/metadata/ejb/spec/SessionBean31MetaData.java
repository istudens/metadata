/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.metadata.ejb.spec;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.ConcurrencyManagementType;
import javax.ejb.LockType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.jboss.metadata.common.ejb.ITimeoutTarget;
import org.jboss.metadata.javaee.spec.EmptyMetaData;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 * @version $Revision: $
 */
@XmlType(name = "session-beanType", propOrder =
{"descriptionGroup", "ejbName", "mappedName", "home", "remote", "localHome", "local", "businessLocals",
      "businessRemotes", "localBean", "serviceEndpoint", "ejbClass", "sessionType", "timeoutMethod", "initOnStartup",
      "concurrencyManagementType", "concurrentMethods", "initMethods", "removeMethods", "asyncMethods", "transactionType",
      "aroundInvokes", "environmentRefsGroup", "postActivates", "prePassivates", "securityRoleRefs", "securityIdentity"})
//@JBossXmlType(modelGroup = JBossXmlConstants.MODEL_GROUP_UNORDERED_SEQUENCE)
public class SessionBean31MetaData extends SessionBeanMetaData implements ITimeoutTarget // FIXME: AbstractProcessor.processClass doesn't take super interfaces into account
{
   private static final long serialVersionUID = 1L;

   private AsyncMethodsMetaData asyncMethods;

   /**
    * For &lt;local-bean&gt;
    */
   private EmptyMetaData localBean;

   /**
    * init-on-startup
    */
   private Boolean initOnStartup;

   /**
    * Concurrent methods against each {@link NamedMethodMetaData}
    */
   private Map<NamedMethodMetaData, ConcurrentMethodMetaData> concurrentMethods;

   /**
    * The lock type that is set at the bean level
    */
   private LockType beanLevelLockType;

   /**
    * Bean level access timeout
    */
   private AccessTimeoutMetaData beanLevelAccessTimeout;

   /**
    * Concurrency management type of the bean
    */
   private ConcurrencyManagementType concurrencyManagementType;

   /**
    * Returns the init-on-startup value of the session bean metadata.
    * Returns null if none is defined.
    * @return
    */
   public Boolean isInitOnStartup()
   {
      return initOnStartup;
   }

   @XmlElement(name = "init-on-startup", required = false)
   public void setInitOnStartup(Boolean initOnStartup)
   {
      this.initOnStartup = initOnStartup;
   }

   public AsyncMethodsMetaData getAsyncMethods()
   {
      return asyncMethods;
   }

   @XmlElement(name = "async-method", required = false)
   public void setAsyncMethods(AsyncMethodsMetaData asyncMethods)
   {
      if (asyncMethods == null)
         throw new IllegalArgumentException("asyncMethods is null");

      this.asyncMethods = asyncMethods;
   }

   /**
    *  
    * @return Returns {@link EmptyMetaData} if the bean represents a no-interface
    * bean. Else returns null. 
    * Use the {@link #isNoInterfaceBean()} API which is more intuitive.
    *   
    * @see SessionBean31MetaData#isNoInterfaceBean()
    */
   public EmptyMetaData getLocalBean()
   {
      return this.localBean;
   }

   /**
    * Set the metadata to represent whether this bean
    * exposes an no-interface view
    * @param isNoInterfaceBean True if the bean exposes a no-interface
    *                           view. Else set to false. 
    */
   @XmlElement(name = "local-bean", required = false)
   public void setLocalBean(EmptyMetaData localBean)
   {
      this.localBean = localBean;
   }

   /**
    * @return Returns true if this bean exposes a no-interface view.
    * Else returns false. This is similar to {@link #getLocalBean()}, but
    * is more intuitive
    * 
    */
   public boolean isNoInterfaceBean()
   {
      return this.localBean == null ? false : true;
   }

   /**
    * Sets the no-interface information in the metadata  
    * @param isNoInterfaceBean True if this is a no-interface bean, false otherwise
    */
   public void setNoInterfaceBean(boolean isNoInterfaceBean)
   {
      this.localBean = isNoInterfaceBean ? new EmptyMetaData() : null;
   }

   /**
    * Returns true if this is a singleton session bean. Else returns false
    */
   public boolean isSingleton()
   {
      if (this.getSessionType() == null)
         return false;
      return this.getSessionType() == SessionType.Singleton;
   }

   /**
    * Sets the concurrency management type of this bean
    * @param concurrencyManagementType The concurrency management type
    * @throws If the passed <code>concurrencyManagementType</code> is null
    */
   @XmlElement(name = "concurrency-management-type", required = false)
   @XmlJavaTypeAdapter(ConcurrencyManagementTypeAdapter.class)
   public void setConcurrencyManagementType(ConcurrencyManagementType concurrencyManagementType)
   {
      if (concurrencyManagementType == null)
      {
         throw new IllegalArgumentException("Concurrency management type cannot be null");
      }
      this.concurrencyManagementType = concurrencyManagementType;
   }

   /**
    * Returns the concurrency management type of this bean
    * @return
    */
   public ConcurrencyManagementType getConcurrencyManagementType()
   {
      return this.concurrencyManagementType;
   }

   /**
    * Sets the concurrent methods of this bean
    * @param concurrentMethods
    * @throws IllegalArgumentException If the passed <code>concurrentMethods</code> is null
    */
   @XmlElement(name = "concurrent-method", required = false)
   @XmlJavaTypeAdapter (ConcurrentMethodsCollectionToMapAdapter.class)
   public void setConcurrentMethods(Map<NamedMethodMetaData, ConcurrentMethodMetaData> concurrentMethods)
   {
      this.concurrentMethods = concurrentMethods;
   }

   /**
    * Returns a {@link Map} whose key represents a {@link NamedMethodMetaData} and whose value
    * represents {@link ConcurrentMethodMetaData} of this bean. Returns an empty {@link Map} if
    * there are no concurrent methods for this bean
    * @return
    */
   public Map<NamedMethodMetaData, ConcurrentMethodMetaData> getConcurrentMethods()
   {
      if (this.concurrentMethods == null)
      {
         return Collections.EMPTY_MAP;
      }
      return this.concurrentMethods;
   }


   /**
    * Sets the lock type applicable at the bean level
    * @param lockType {@link LockType}
    */
   public void setLockType(LockType lockType)
   {
      this.beanLevelLockType = lockType;
   }

   /**
    * Returns the lock type applicable at the bean level
    * @return
    */
   public LockType getLockType()
   {
      return this.beanLevelLockType;
   }

   /**
    * Sets the bean level access timeout metadata
    * @param accessTimeout {@link AccessTimeoutMetaData}
    */
   public void setAccessTimeout(AccessTimeoutMetaData accessTimeout)
   {
      this.beanLevelAccessTimeout = accessTimeout;
   }

   /**
    * Returns the access timeout metadata applicable at bean level
    * 
    * @return
    */
   public AccessTimeoutMetaData getAccessTimeout()
   {
      return this.beanLevelAccessTimeout;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void merge(EnterpriseBeanMetaData eoverride, EnterpriseBeanMetaData eoriginal)
   {
      super.merge(eoverride, eoriginal);
      SessionBean31MetaData override = (SessionBean31MetaData) eoverride;
      SessionBean31MetaData original = (SessionBean31MetaData) eoriginal;
      if (asyncMethods == null)
         asyncMethods = new AsyncMethodsMetaData();
      if (override != null && override.asyncMethods != null)
         asyncMethods.addAll(override.asyncMethods);
      if (original != null && original.asyncMethods != null)
         asyncMethods.addAll(original.asyncMethods);

      // merge rest of the metadata

      if (override != null)
      {
         if (override.localBean != null)
         {
            this.localBean = override.localBean;
         }
         if (override.initOnStartup != null)
         {
            this.initOnStartup = override.initOnStartup;
         }
         if (override.concurrencyManagementType != null)
         {
            this.concurrencyManagementType = override.concurrencyManagementType;
         }
         if (override.concurrentMethods != null)
         {
            if (this.concurrentMethods == null)
            {
               this.concurrentMethods = new HashMap<NamedMethodMetaData, ConcurrentMethodMetaData>();
            }
            this.concurrentMethods.putAll(override.concurrentMethods);
         }
         if (override.beanLevelLockType != null)
         {
            this.beanLevelLockType = override.beanLevelLockType;
         }
         if (override.beanLevelAccessTimeout != null)
         {
            this.beanLevelAccessTimeout = override.beanLevelAccessTimeout;
         }
      }
      else if (original != null)
      {
         if (original.localBean != null)
         {
            this.localBean = original.localBean;
         }
         if (original.initOnStartup != null)
         {
            this.initOnStartup = original.initOnStartup;
         }
         if (original.concurrencyManagementType != null)
         {
            this.concurrencyManagementType = original.concurrencyManagementType;
         }
         if (original.concurrentMethods != null)
         {
            if (this.concurrentMethods == null)
            {
               this.concurrentMethods = new HashMap<NamedMethodMetaData, ConcurrentMethodMetaData>();
            }
            this.concurrentMethods.putAll(original.concurrentMethods);
         }
         if (original.beanLevelLockType != null)
         {
            this.beanLevelLockType = original.beanLevelLockType;
         }
         if (original.beanLevelAccessTimeout != null)
         {
            this.beanLevelAccessTimeout = original.beanLevelAccessTimeout;
         }
      }
   }
}
