/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-impl/impl/src/java/org/etudes/coursemap/impl/CourseMapServiceImpl.java $
 * $Id: CourseMapServiceImpl.java 9692 2014-12-26 21:57:29Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2010, 2011, 2012, 2013, 2014 Etudes, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.etudes.coursemap.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.coursemap.api.CourseMapItem;
import org.etudes.coursemap.api.CourseMapItemAccessStatus;
import org.etudes.coursemap.api.CourseMapItemPerformStatus;
import org.etudes.coursemap.api.CourseMapItemProvider;
import org.etudes.coursemap.api.CourseMapItemScoreStatus;
import org.etudes.coursemap.api.CourseMapItemType;
import org.etudes.coursemap.api.CourseMapMap;
import org.etudes.coursemap.api.CourseMapService;
import org.etudes.util.api.AccessAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * CourseMapServiceImpl implements CourseMapService
 */
public class CourseMapServiceImpl implements CourseMapService, EntityTransferrer, EntityProducer
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(CourseMapServiceImpl.class);

	/** Thread local key for a thread-cached map. */
	protected final static String MAP = "CourseMapServiceImpl.map.";

	/** Dependency (optional, self-injected): AccessAdvisor. */
	protected transient CourseMapAccessAdvisor accessAdvisor = null;

	/** Dependency: EntityManager. */
	protected EntityManager entityManager = null;

	/** Our registered providers. */
	protected Set<CourseMapItemProvider> providers = new HashSet<CourseMapItemProvider>();

	/** Dependency: SecurityService */
	protected SecurityService securityService = null;

	/** Dependency: SessionManager */
	protected SessionManager sessionManager = null;

	/** Dependency: SiteService. */
	protected SiteService siteService = null;

	/** Dependency: SqlService */
	protected SqlService sqlService = null;

	/** Storage handler. */
	protected CourseMapStorage storage = null;

	/** Storage option map key for the option to use. */
	protected String storageKey = null;

	/** Map of registered PoolStorage options. */
	protected Map<String, CourseMapStorage> storgeOptions;

	/** Dependency: ThreadLocalManager. */
	protected ThreadLocalManager threadLocalManager = null;

	/**
	 * {@inheritDoc}
	 */
	public Boolean allowEditMap(String context, String userId)
	{
		if (context == null) throw new IllegalArgumentException();
		if (userId == null) return Boolean.FALSE;

		if (M_log.isDebugEnabled()) M_log.debug("allowEditMap: " + context + ": " + userId);

		// check permission - user must have "site.upd" in the context
		boolean ok = checkSecurity(userId, "site.upd", context);

		return ok;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean allowGetMap(String context, String userId)
	{
		if (context == null) throw new IllegalArgumentException();
		if (userId == null) return Boolean.FALSE;

		if (M_log.isDebugEnabled()) M_log.debug("allowGetMap: " + context + ": " + userId);

		// check permission - user must have "site.visit" in the context
		boolean ok = checkSecurity(userId, "site.visit", context);

		// and cannot be an evaluator - use Mneme's permission
		if (ok)
		{
			ok = !checkSecurity(userId, "mneme.course.eval", context);
		}

		return ok;
	}

	/**
	 * {@inheritDoc}
	 */
	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
	{
		return null;
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public Entity getEntity(Reference ref)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Collection getEntityAuthzGroups(Reference ref, String userId)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityDescription(Reference ref)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEntityUrl(Reference ref)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public HttpAccess getHttpAccess()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLabel()
	{
		// TODO: localize
		return "Course Map";
	}

	/**
	 * {@inheritDoc}
	 */
	public CourseMapMap getMap(String context, String userId)
	{
		if (context == null) throw new IllegalArgumentException();
		if (userId == null) userId = sessionManager.getCurrentSessionUserId();

		// if cached for the thread, return it
		String key = MAP + context + userId;
		CourseMapMap map = (CourseMapMap) this.threadLocalManager.get(key);
		if (map != null) return map;

		// if our access advisor is running, block it for the map creation.
		if (this.accessAdvisor != null) this.accessAdvisor.block();

		// get ordering information, blocker status, headers
		map = this.storage.getMap(context, userId);

		// get the items from the providers
		List<CourseMapItem> items = new ArrayList<CourseMapItem>();
		for (CourseMapItemProvider provider : this.providers)
		{
			// get the items from this provider
			items.addAll(provider.getListItems(context, userId, true));
		}

		// merge all items
		map.mergeItems(items);

		// prepare the map for use - all items are in place
		((CourseMapMapImpl) map).init();

		// check for guest
		if (this.isGuest(context, userId)) ((CourseMapMapImpl) map).setIsGuest();

		// cache it for the thread
		this.threadLocalManager.set(key, map);

		// restore the access advisor
		if (this.accessAdvisor != null) this.accessAdvisor.restore();

		// return the ordered items
		return map;
	}

	/**
	 * {@inheritDoc}
	 */
	public CourseMapMap getMapEdit(String context, String userId)
	{
		if (context == null) throw new IllegalArgumentException();

		CourseMapMap map = readAndPopulateEditMap(context, userId);

		// save the map if changed
		saveMap(map);

		// return the ordered items
		return map;
	}

	/**
	 * {@inheritDoc}
	 */
	public CourseMapMap getUnfilteredMap(String context, String userId)
	{
		if (context == null) throw new IllegalArgumentException();
		if (userId == null) userId = sessionManager.getCurrentSessionUserId();

		// Note: unfiltered map is NOT cached for the thread.

		// if our access advisor is running, block it for the map creation.
		if (this.accessAdvisor != null) this.accessAdvisor.block();

		// get ordering information, blocker status, headers
		CourseMapMap map = this.storage.getMap(context, userId);

		// get the items from the providers
		List<CourseMapItem> items = new ArrayList<CourseMapItem>();
		for (CourseMapItemProvider provider : this.providers)
		{
			// get the items from this provider
			items.addAll(provider.getListItems(context, userId, false));
		}

		// merge all items
		map.mergeItems(items);

		// prepare the map for use - all items are in place
		((CourseMapMapImpl) map).init();

		// restore the access advisor
		if (this.accessAdvisor != null) this.accessAdvisor.restore();

		// return the ordered items
		return map;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			// storage - as configured
			if (this.storageKey != null)
			{
				// if set to "SQL", replace with the current SQL vendor
				if ("SQL".equals(this.storageKey))
				{
					this.storageKey = sqlService.getVendor();
				}

				this.storage = this.storgeOptions.get(this.storageKey);
			}

			// use "default" if needed
			if (this.storage == null)
			{
				this.storage = this.storgeOptions.get("default");
			}

			if (storage == null) M_log.warn("no storage set: " + this.storageKey);

			storage.init();

			// check if there is an access advisor - if not, that's ok. Only remember it if it is ours.
			Object advisor = ComponentManager.get(AccessAdvisor.class);
			if ((advisor != null) && (advisor instanceof CourseMapAccessAdvisor))
			{
				this.accessAdvisor = (CourseMapAccessAdvisor) advisor;
			}

			// entity producer registration (note: there is no reference root since we do no entities)
			this.entityManager.registerEntityProducer(this, "/coursemap-NEVER");

			M_log.info("init(): storage: " + this.storage);
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean isGuest(String context, String userId)
	{
		if (context == null) throw new IllegalArgumentException();
		if (userId == null) return Boolean.FALSE;

		if (M_log.isDebugEnabled()) M_log.debug("isGuest: " + context + ": " + userId);

		// admin is not guest
		if (this.securityService.isSuperUser()) return Boolean.FALSE;

		// check permission - user must have "mneme.guest" in the context
		boolean ok = checkSecurity(userId, "mneme.guest", context);

		return ok;
	}

	/**
	 * {@inheritDoc}
	 */
	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport)
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] myToolIds()
	{
		String[] toolIds =
		{ "sakai.coursemap" };
		return toolIds;
	}

	/**
	 * {@inheritDoc}
	 */
	public CourseMapItem newEditItem(String id, CourseMapItemType type, String title, Date open, Date due, Date close, Boolean datesReadOnly,
			Integer countRequired, Float points, String toolId, String editLink, CourseMapItemAccessStatus accessStatus)
	{
		return new CourseMapItemImpl(id, type, title, open, due, close, datesReadOnly, countRequired, points, toolId, editLink, accessStatus);
	}

	/**
	 * {@inheritDoc}
	 */
	public CourseMapItem newListItem(String id, CourseMapItemType type, String title, Date open, Date due, Date close, Float score,
			CourseMapItemScoreStatus scoreStatus, Date finished, Integer count, Integer countRequired, Float points, Boolean masteryLevelQualified,
			String toolId, String performLink, String reviewLink, CourseMapItemAccessStatus accessStatus, CourseMapItemPerformStatus performStatus,
			Boolean evaluationNotReviewed, Boolean nonUser, Boolean empty, Boolean mayPerformAgain, String editLink)
	{
		return new CourseMapItemImpl(id, type, title, open, due, close, score, scoreStatus, finished, count, countRequired, points,
				masteryLevelQualified, toolId, performLink, reviewLink, accessStatus, performStatus, evaluationNotReviewed, nonUser, empty,
				mayPerformAgain, editLink);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void registerProvider(CourseMapItemProvider provider)
	{
		this.providers.add(provider);
	}

	/**
	 * {@inheritDoc}
	 */
	public void saveMap(CourseMapMap map)
	{
		// reorder items if needed
		map.applyNewOrder();

		// check if any dates need saving
		for (CourseMapItem item : map.getItems())
		{
			if (((CourseMapItemImpl) item).getChanged())
			{
				// ask the providers to save the item
				for (CourseMapItemProvider provider : this.providers)
				{
					if (provider.ownsItem(map.getContext(), item.getId(), item.getType()))
					{
						provider.updateItem(map.getContext(), map.getUserId(), item);
					}
				}
			}

			((CourseMapItemImpl) item).clearChanged();
		}

		// see if the map's information or ordering needs saving (mastery, headers, order)
		if (((CourseMapMapImpl) map).getChanged())
		{
			this.storage.saveMap((CourseMapMapImpl) map);

			((CourseMapMapImpl) map).clearChanged();
		}
	}

	/**
	 * Dependency: EntityManager.
	 * 
	 * @param service
	 *        The EntityManager.
	 */
	public void setEntityManager(EntityManager service)
	{
		entityManager = service;
	}

	/**
	 * Dependency: SecurityService.
	 * 
	 * @param service
	 *        The SecurityService.
	 */
	public void setSecurityService(SecurityService service)
	{
		this.securityService = service;
	}

	/**
	 * Dependency: SessionManager.
	 * 
	 * @param service
	 *        The SessionManager.
	 */
	public void setSessionManager(SessionManager service)
	{
		this.sessionManager = service;
	}

	/**
	 * Dependency: SiteService
	 * 
	 * @param service
	 *        The SiteService.
	 */
	public void setSiteService(SiteService service)
	{
		this.siteService = service;
	}

	/**
	 * Dependency: SqlService.
	 * 
	 * @param service
	 *        The SqlService.
	 */
	public void setSqlService(SqlService service)
	{
		sqlService = service;
	}

	/**
	 * Set the storage class options.
	 * 
	 * @param options
	 *        The PoolStorage options.
	 */
	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	public void setStorage(Map options)
	{
		this.storgeOptions = options;
	}

	/**
	 * Set the storage option key to use, selecting which PoolStorage to use.
	 * 
	 * @param key
	 *        The storage option key.
	 */
	public void setStorageKey(String key)
	{
		this.storageKey = key;
	}

	/**
	 * Dependency: ThreadLocalManager.
	 * 
	 * @param service
	 *        The SqlService.
	 */
	public void setThreadLocalManager(ThreadLocalManager service)
	{
		threadLocalManager = service;
	}

	/**
	 * {@inheritDoc}
	 */
	public void transferCopyEntities(String fromContext, String toContext, List ids)
	{
		// get the source map
		CourseMapMap source = readAndPopulateEditMap(fromContext, null);

		// get the destination map
		CourseMapMap dest = readAndPopulateEditMap(toContext, null);

		// set mastery level
		dest.setMasteryPercent(source.getMasteryPercent());

		// set clear blocks on close
		dest.setClearBlockOnClose(source.getClearBlockOnClose());

		// run through the source items, backwards
		List<CourseMapItem> sourceItems = source.getItems();
		for (int i = sourceItems.size() - 1; i >= 0; i--)
		{
			CourseMapItem sourceItem = sourceItems.get(i);

			// find a matching item (having the same title and type/application code) in the dest map
			CourseMapItem matchingItemInDest = dest.getItem(sourceItem.getTitle(), sourceItem.getType().getAppCode());
			if (matchingItemInDest != null)
			{
				// promote to be the new top
				matchingItemInDest.setMapPositioning(dest.getItems().get(0).getMapId());

				// match blocker setting
				matchingItemInDest.setBlocker(sourceItem.getBlocker());
			}

			// if not found, and the source item is a header, add it to the top
			else if (sourceItem.getType() == CourseMapItemType.header)
			{
				// add it up top
				CourseMapItem newHeader = dest.addHeaderBefore(null);
				newHeader.setTitle(sourceItem.getTitle());
			}
		}

		// save the dest map
		saveMap(dest);
	}

	/**
	 * {@inheritDoc}
	 */
	public void transferCopyEntities(String fromContext, String toContext, List ids, boolean cleanup)
	{
		transferCopyEntities(fromContext, toContext, ids);
	}

	/**
	 * {@inheritDoc}
	 */
	public void unregisterProvider(CourseMapItemProvider provider)
	{
		this.providers.remove(provider);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean willArchiveMerge()
	{
		return false;
	}

	/**
	 * Check the security for this user doing this function within this context.
	 * 
	 * @param userId
	 *        the user id.
	 * @param function
	 *        the function.
	 * @param context
	 *        The context.
	 * @param ref
	 *        The entity reference.
	 * @return true if the user has permission, false if not.
	 */
	protected boolean checkSecurity(String userId, String function, String context)
	{
		// check for super user
		if (securityService.isSuperUser(userId)) return true;

		// check for the user / function / context-as-site-authz
		// use the site ref for the security service (used to cache the security calls in the security service)
		String siteRef = siteService.siteReference(context);

		// form the azGroups for a context-as-implemented-by-site
		Collection<String> azGroups = new ArrayList<String>(2);
		azGroups.add(siteRef);
		azGroups.add("!site.helper");

		boolean rv = securityService.unlock(userId, function, siteRef, azGroups);
		return rv;
	}

	protected CourseMapMap readAndPopulateEditMap(String context, String userId)
	{
		if (userId == null) userId = sessionManager.getCurrentSessionUserId();

		// Note: edit map is NOT cached for the thread.

		// if our access advisor is running, block it for the map creation.
		if (this.accessAdvisor != null) this.accessAdvisor.block();

		// get ordering information, blocker status, headers
		CourseMapMap map = this.storage.getMap(context, userId);

		// get the items from the providers
		List<CourseMapItem> items = new ArrayList<CourseMapItem>();
		for (CourseMapItemProvider provider : this.providers)
		{
			// get the items from this provider
			items.addAll(provider.getEditItems(context, userId));
		}

		// merge all items
		map.mergeItems(items);

		// prepare the map for use - all items are in place
		((CourseMapMapImpl) map).init();

		// restore the access advisor
		if (this.accessAdvisor != null) this.accessAdvisor.restore();

		return map;
	}
}
