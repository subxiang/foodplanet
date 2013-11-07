package com.darkart.foodmap.server;

import com.darkart.foodmap.EMF;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.datanucleus.query.JPACursorHelper;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Named;
import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityManager;
import javax.persistence.Query;

@Api(name = "foodendpoint", namespace = @ApiNamespace(ownerDomain = "darkart.com", ownerName = "darkart.com", packagePath = "foodmap.server"))
public class FoodEndpoint {

	/**
	 * This method lists all the entities inserted in datastore.
	 * It uses HTTP GET method and paging support.
	 *
	 * @return A CollectionResponse class containing the list of all entities
	 * persisted and a cursor to the next page.
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	@ApiMethod(name = "listFood")
	public CollectionResponse<Food> listFood(
			@Nullable @Named("cursor") String cursorString,
			@Nullable @Named("limit") Integer limit) {

		EntityManager mgr = null;
		Cursor cursor = null;
		List<Food> execute = null;

		try {
			mgr = getEntityManager();
			Query query = mgr.createQuery("select from Food as Food");
			if (cursorString != null && cursorString != "") {
				cursor = Cursor.fromWebSafeString(cursorString);
				query.setHint(JPACursorHelper.CURSOR_HINT, cursor);
			}

			if (limit != null) {
				query.setFirstResult(0);
				query.setMaxResults(limit);
			}

			execute = (List<Food>) query.getResultList();
			cursor = JPACursorHelper.getCursor(execute);
			if (cursor != null)
				cursorString = cursor.toWebSafeString();

			// Tight loop for fetching all entities from datastore and accomodate
			// for lazy fetch.
			for (Food obj : execute)
				;
		} finally {
			mgr.close();
		}

		return CollectionResponse.<Food> builder().setItems(execute)
				.setNextPageToken(cursorString).build();
	}

	/**
	 * This method gets the entity having primary key id. It uses HTTP GET method.
	 *
	 * @param id the primary key of the java bean.
	 * @return The entity with primary key id.
	 */
	@ApiMethod(name = "getFood")
	public Food getFood(@Named("id") String id) {
		EntityManager mgr = getEntityManager();
		Food food = null;
		try {
			food = mgr.find(Food.class, id);
		} finally {
			mgr.close();
		}
		return food;
	}

	/**
	 * This inserts a new entity into App Engine datastore. If the entity already
	 * exists in the datastore, an exception is thrown.
	 * It uses HTTP POST method.
	 *
	 * @param food the entity to be inserted.
	 * @return The inserted entity.
	 */
	@ApiMethod(name = "insertFood")
	public Food insertFood(Food food) {
		EntityManager mgr = getEntityManager();
		try {
			if (containsFood(food)) {
				throw new EntityExistsException("Object already exists");
			}
			mgr.persist(food);
		} finally {
			mgr.close();
		}
		return food;
	}

	/**
	 * This method is used for updating an existing entity. If the entity does not
	 * exist in the datastore, an exception is thrown.
	 * It uses HTTP PUT method.
	 *
	 * @param food the entity to be updated.
	 * @return The updated entity.
	 */
	@ApiMethod(name = "updateFood")
	public Food updateFood(Food food) {
		EntityManager mgr = getEntityManager();
		try {
			if (!containsFood(food)) {
				throw new EntityNotFoundException("Object does not exist");
			}
			mgr.persist(food);
		} finally {
			mgr.close();
		}
		return food;
	}

	/**
	 * This method removes the entity with primary key id.
	 * It uses HTTP DELETE method.
	 *
	 * @param id the primary key of the entity to be deleted.
	 */
	@ApiMethod(name = "removeFood")
	public void removeFood(@Named("id") String id) {
		EntityManager mgr = getEntityManager();
		try {
			Food food = mgr.find(Food.class, id);
			mgr.remove(food);
		} finally {
			mgr.close();
		}
	}

	private boolean containsFood(Food food) {
		EntityManager mgr = getEntityManager();
		boolean contains = true;
		try {
			Food item = mgr.find(Food.class, food.getName());
			if (item == null) {
				contains = false;
			}
		} finally {
			mgr.close();
		}
		return contains;
	}

	private static EntityManager getEntityManager() {
		return EMF.get().createEntityManager();
	}

}
