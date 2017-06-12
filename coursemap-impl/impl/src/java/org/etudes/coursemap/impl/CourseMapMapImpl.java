/**********************************************************************************
 * $URL: https://source.etudes.org/svn/apps/coursemap/trunk/coursemap-impl/impl/src/java/org/etudes/coursemap/impl/CourseMapMapImpl.java $
 * $Id: CourseMapMapImpl.java 9692 2014-12-26 21:57:29Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2010, 2011, 2012, 2014 Etudes, Inc.
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
import java.util.Iterator;
import java.util.List;

import org.etudes.coursemap.api.CourseMapItem;
import org.etudes.coursemap.api.CourseMapItemAccessStatus;
import org.etudes.coursemap.api.CourseMapItemProgressStatus;
import org.etudes.coursemap.api.CourseMapItemType;
import org.etudes.coursemap.api.CourseMapMap;
import org.sakaiproject.util.StringUtil;

/**
 * CourseMapServiceImpl implements CourseMapService
 */
public class CourseMapMapImpl implements CourseMapMap
{
	/** Clear Block on Close setting. */
	protected Boolean clearBlockOnClose = Boolean.FALSE;

	/** The context for which this map was prepared. */
	protected transient String context = null;

	/** If the user is a guest in the site. */
	protected transient Boolean guest = Boolean.FALSE;

	/** The ordered items. **/
	protected List<CourseMapItem> items = new ArrayList<CourseMapItem>();

	/** Mastery level - percent between 0 and 100, or null for not set. */
	protected Integer masteryPercent = null;

	/** Set with the new item index order for a map reordering. */
	protected transient String newOrder = null;

	/** Set when the map is all populated and ready to use. */
	protected transient boolean ready = false;

	/** The user id for which this map was prepared. */
	protected transient String userId = null;

	/** Keep track of changes. */
	transient boolean changed = false;

	/**
	 * Construct
	 * 
	 * @param contetx
	 *        The context.
	 */
	public CourseMapMapImpl(String context, String userId)
	{
		this.context = context;
		this.userId = userId;
	}

	/**
	 * {@inheritDoc}
	 */
	public void acceptAllPositioned()
	{
		for (CourseMapItem item : this.items)
		{
			item.setPositioned(Boolean.TRUE);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public CourseMapItem addHeaderBefore(CourseMapItem item)
	{
		// Note: this is bad - won't support concurrent editing (adding headers) from different UIs. Really needs to be some sort of DB mediated thing... -ggolden
		// id - scan the existing headers for the biggest id
		long biggestId = 0;
		for (CourseMapItem header : this.items)
		{
			if (header.getType() == CourseMapItemType.header)
			{
				try
				{
					long headerId = Long.parseLong(header.getId(), 16);
					if (headerId > biggestId) biggestId = headerId;
				}
				catch (NumberFormatException e)
				{
				}
			}
		}

		// use the next in sequence
		// a new id - use the current time for the id
		String id = Long.toHexString(biggestId + 1);

		// create the item
		CourseMapItem header = new CourseMapItemImpl(id, null);

		// where to put the header
		int pos = 0;
		if (item != null)
		{
			pos = this.items.indexOf(item);
			if (pos == -1) pos = 0;
		}

		// put in the map
		this.items.add(pos, header);
		((CourseMapItemImpl) header).map = this;

		// the map has changed
		this.setChanged();

		// return the header
		return header;
	}

	/**
	 * {@inheritDoc}
	 */
	public void applyNewOrder()
	{
		// if no new order set, nothing to do
		if (this.newOrder == null) return;

		// parse the numbers
		String[] indexStrs = StringUtil.split(this.newOrder, " ");
		int size = indexStrs.length;

		// if we don't have one for each item, something has gone wrong
		if (size != this.items.size()) return;

		int[] indexes = new int[size];
		for (int i = 0; i < size; i++)
		{
			try
			{
				indexes[i] = Integer.parseInt(indexStrs[i]);
			}
			catch (NumberFormatException e)
			{
				// this is not good
				return;
			}
		}

		// if the new order is just the old order (numbers from 0..size-1), then nothing to do
		boolean changed = false;
		for (int i = 0; i < size; i++)
		{
			if (indexes[i] != i)
			{
				changed = true;
				break;
			}
		}
		if (!changed) return;

		// ok, we have work to do
		this.setChanged();

		// form a new list that has items in indexes order from the old list
		List<CourseMapItem> newItems = new ArrayList<CourseMapItem>();
		for (int i = 0; i < size; i++)
		{
			newItems.add(this.items.get(indexes[i]));
		}
		this.items = newItems;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getClearBlockOnClose()
	{
		return this.clearBlockOnClose;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getContext()
	{
		return this.context;
	}

	/**
	 * {@inheritDoc}
	 */
	public Boolean getFullyPositioned()
	{
		for (CourseMapItem item : this.items)
		{
			if (!item.getPositioned()) return Boolean.FALSE;
		}

		return Boolean.TRUE;
	}

	/**
	 * {@inheritDoc}
	 */
	public CourseMapItem getItem(String id)
	{
		for (CourseMapItem item : this.items)
		{
			if (item.getMapId().equals(id)) return item;
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public CourseMapItem getItem(String title, Integer appCode)
	{
		for (CourseMapItem item : this.items)
		{
			if (item.getType().getAppCode().equals(appCode) && item.getTitle().equals(title)) return item;
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public CourseMapItem getItemBlocked(String id, CourseMapItemType type)
	{
		CourseMapItem rv = null;
		for (CourseMapItem i : this.items)
		{
			// is this the item?
			if (i.getType().getAppCode().equals(type.getAppCode()) && i.getId().equals(id))
			{
				break;
			}

			// is this a valid blocker?
			if ((i.getAccessStatus() != CourseMapItemAccessStatus.invalid) && i.getEffectiveBlocker())
			{
				// give up if not mastered
				if (!i.getMastered())
				{
					rv = i;
					break;
				}
			}
		}

		// if found, the item was not blocked
		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<CourseMapItem> getItems()
	{
		return this.items;
	}

	/**
	 * {@inheritDoc}
	 */
	public Float getMasteryLevel()
	{
		if (this.masteryPercent == null) return null;

		// convert from integer 0..100 to float 0..1
		return Float.valueOf((float) this.masteryPercent / 100f);
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getMasteryPercent()
	{
		if (this.masteryPercent == null) return null;
		return this.masteryPercent;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMasteryPercentDisplay()
	{
		if (this.masteryPercent == null) return "0%";
		return this.masteryPercent.toString() + "%";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNewOrder()
	{
		return this.newOrder;
	}

	/**
	 * {@inheritDoc}
	 */
	public Integer getNumItemsMissed()
	{
		int count = 0;

		for (CourseMapItem item : this.items)
		{
			// skip survey items to hide their activity
			if (item.getType() == CourseMapItemType.survey) continue;
			if (item.getType() == CourseMapItemType.fce) continue;

			if ((item.getProgressStatus() == CourseMapItemProgressStatus.missed)
					|| (item.getProgressStatus() == CourseMapItemProgressStatus.missedNoSub)) count++;
		}

		return Integer.valueOf(count);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserId()
	{
		return this.userId;
	}

	/**
	 * {@inheritDoc}
	 */
	public void mergeItems(List<CourseMapItem> mergeItems)
	{
		// for the second pass
		List<CourseMapItem> pass2 = new ArrayList<CourseMapItem>();

		// run through all the items for the first pass
		for (CourseMapItem mergeItem : mergeItems)
		{
			// find the item in the map items by id and type's application code
			CourseMapItem found = null;
			for (CourseMapItem i : this.items)
			{
				if (i.getType().getAppCode().equals(mergeItem.getType().getAppCode()) && i.getId().equals(mergeItem.getId()))
				{
					// set the map item to match
					// Note: the map already knows about this item, so is not changed
					((CourseMapItemImpl) i).set(mergeItem);

					// if the item's open date is different from the stored open date, mark the item as not positioned
					if (Different.different(i.getOpen(), i.getPreviousOpen()))
					{
						i.setPositioned(Boolean.FALSE);
					}

					// Note: positioned or not, if we find the item we leave it in place
					found = i;
					break;
				}
			}

			// if not found, and the item has an open date, save for the next pass
			if (found == null)
			{
				// Note: this indicates a new item, so the map has changed
				setChanged();

				// new item, set the prevOpen to open
				((CourseMapItemImpl) mergeItem).initPreviousOpen();

				if (mergeItem.getOpen() != null)
				{
					pass2.add(mergeItem);
				}

				// if no date, add it to the map
				else
				{
					// should we insert it at the beginning?
					if (mergeItem.getType().getInsert())
					{
						this.items.add(0, mergeItem);
					}

					// otherwise append it to the end
					else
					{
						this.items.add(mergeItem);
					}
				}
			}
		}

		// process any left, with open dates, into their proper date position
		for (CourseMapItem mergeItem : pass2)
		{
			boolean found = false;
			for (CourseMapItem i : this.items)
			{
				if (i.getOpen() != null)
				{
					// if we find an item in the map after this one, insert before
					if (i.getOpen().after(mergeItem.getOpen()))
					{
						this.items.add(this.items.indexOf(i), mergeItem);

						found = true;
						break;
					}
				}
			}

			// if the mergeItem has a date after all the items, add it after the last dated item
			if (!found)
			{
				for (int index = this.items.size() - 1; index >= 0; index--)
				{
					if (this.items.get(index).getOpen() != null)
					{
						this.items.add(index + 1, mergeItem);

						found = true;
						break;
					}
				}
			}

			// if still not found, (i.e. all items in the map have no open date) add it right after the last item marked for front-insertion
			if (!found)
			{
				for (int index = this.items.size() - 1; index >= 0; index--)
				{
					if (this.items.get(index).getType().getInsert())
					{
						this.items.add(index + 1, mergeItem);

						found = true;
						break;
					}
				}
			}

			// if still not found, there are no dated items, and no front-insert items, so just add it in the front
			if (!found)
			{
				this.items.add(0, mergeItem);
			}
		}
	}

	/**
	 * {@inheritDoc} Note: this version has un-positioned items "float" - get re-inserted each time by the auto-rules
	 */
	public void mergeItemsFloaters(List<CourseMapItem> mergeItems)
	{
		// for the second pass
		List<CourseMapItem> pass2 = new ArrayList<CourseMapItem>();

		// run through all the items for the first pass
		for (CourseMapItem mergeItem : mergeItems)
		{
			// find the item in the map items by id and type's application code
			CourseMapItem found = null;
			CourseMapItem remove = null;
			for (CourseMapItem i : this.items)
			{
				if (i.getType().getAppCode().equals(mergeItem.getType().getAppCode()) && i.getId().equals(mergeItem.getId()))
				{
					// set the map item to match
					// Note: the map already knows about this item, so is not changed
					((CourseMapItemImpl) i).set(mergeItem);

					// if the item is positioned, keep it in position
					if (i.getPositioned())
					{
						found = i;
					}

					// otherwise if the item is not yet positioned, remove it from the map and process it as new (found remains null)
					else
					{
						// i now has mergeItem plus the stored settings
						mergeItem = i;

						// mark it for removal from the items
						remove = i;
					}
					break;
				}
			}

			// if we need to remove the item from out items, do it here outside the for iteration
			if (remove != null)
			{
				this.items.remove(remove);
			}

			// if not found, and the item has an open date, save for the next pass
			if (found == null)
			{
				// Note: this indicates a new item, so the map has changed
				setChanged();

				if (mergeItem.getOpen() != null)
				{
					pass2.add(mergeItem);
				}

				// if no date, add it to the map
				else
				{
					// should we insert it at the beginning?
					if (mergeItem.getType().getInsert())
					{
						this.items.add(0, mergeItem);
					}

					// otherwise append it to the end
					else
					{
						this.items.add(mergeItem);
					}
				}
			}
		}

		// process any left, with open dates, into their proper date position
		for (CourseMapItem mergeItem : pass2)
		{
			boolean found = false;
			for (CourseMapItem i : this.items)
			{
				if (i.getOpen() != null)
				{
					// if we find an item in the map after this one, insert before
					if (i.getOpen().after(mergeItem.getOpen()))
					{
						this.items.add(this.items.indexOf(i), mergeItem);

						found = true;
						break;
					}
				}
			}

			// if the mergeItem has a date after all the items, add it after the last dated item
			if (!found)
			{
				for (int index = this.items.size() - 1; index >= 0; index--)
				{
					if (this.items.get(index).getOpen() != null)
					{
						this.items.add(index + 1, mergeItem);

						found = true;
						break;
					}
				}
			}

			// if still not found, (i.e. all items in the map have no open date) add it right after the last item marked for front-insertion
			if (!found)
			{
				for (int index = this.items.size() - 1; index >= 0; index--)
				{
					if (this.items.get(index).getType().getInsert())
					{
						this.items.add(index + 1, mergeItem);

						found = true;
						break;
					}
				}
			}

			// if still not found, there are no dated items, and no front-insert items, so just add it in the front
			if (!found)
			{
				this.items.add(0, mergeItem);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void reInsertUnPositioned()
	{
		// collect the not positioned items
		List<CourseMapItem> unpositioned = new ArrayList<CourseMapItem>();
		for (CourseMapItem i : this.items)
		{
			if (!i.getPositioned())
			{
				unpositioned.add(i);
			}
		}

		// remove these from the map
		this.items.removeAll(unpositioned);

		// put them back in the map
		mergeItems(unpositioned);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeHeader(CourseMapItem header)
	{
		// remove the item
		this.items.remove(header);

		// the map has changed
		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setClearBlockOnClose(Boolean setting)
	{
		if (setting == null) setting = Boolean.FALSE;

		// set only if different
		if (!Different.different(setting, this.clearBlockOnClose)) return;

		this.clearBlockOnClose = setting;
		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMasteryPercent(Integer percent)
	{
		// auto-range
		if (percent != null)
		{
			if (percent < 0) percent = Integer.valueOf(0);
			if (percent > 100) percent = Integer.valueOf(100);
		}

		// set only if different
		if (!Different.different(percent, this.masteryPercent)) return;

		this.masteryPercent = percent;
		setChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setNewOrder(String newOrder)
	{
		this.newOrder = newOrder;
		// no change yet...
	}

	/**
	 * Clear the changed flags.
	 */
	protected void clearChanged()
	{
		this.changed = false;
	}

	/**
	 * @return TRUE if the item has changed, FALSE if not.
	 */
	protected boolean getChanged()
	{
		return this.changed;
	}

	/**
	 * @return TRUE if the map's user is a guest in the site, FALSE if not.
	 */
	protected Boolean getIsGuest()
	{
		return this.guest;
	}

	/**
	 * Get the map ready to use, once fully populated.
	 */
	protected void init()
	{
		if (this.ready) return;

		// trim items not populated
		for (Iterator<CourseMapItem> i = this.items.iterator(); i.hasNext();)
		{
			CourseMapItem item = i.next();

			// use a null title as an indicator - but don't drop any headers
			if ((item.getType() != CourseMapItemType.header) && (item.getTitle() == null))
			{
				i.remove();

				// Note: this item was in the map and is no longer, so the map has changed
				setChanged();
			}

			// let the item know it is in this map
			((CourseMapItemImpl) item).map = this;
		}

		this.ready = true;
	}

	/**
	 * Set the map has been changed.
	 */
	protected void setChanged()
	{
		this.changed = true;
	}

	/**
	 * Set the map as belonging to a guest.
	 */
	protected void setIsGuest()
	{
		this.guest = Boolean.TRUE;
	}
}
