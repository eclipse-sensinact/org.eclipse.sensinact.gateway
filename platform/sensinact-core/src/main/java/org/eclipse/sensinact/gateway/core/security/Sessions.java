/*
 * Copyright (c) 2017 CEA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    CEA - initial API and implementation
 */
package org.eclipse.sensinact.gateway.core.security;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Map;



/**
 * A service handling a Collection of {@link Session}s
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
public class Sessions 
{

	//********************************************************************//
	//						NESTED DECLARATIONS		    				  //
	//********************************************************************//
	
	/**
	 * 
	 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
	 */
	public static enum KeyExtractorType
	{
		TOKEN,
		PUBLIC
	}
	
	/**
	 * 
	 * @param <E>
	 * 
	 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
	 */
	public static class KeyExtractor<E extends Enum<KeyExtractorType>>
	{
		private E e;
		private Object v;

		/**
		 * @param e
		 * @param v
		 */
		public KeyExtractor(E e, Object v)
		{
			this.e = e;
			this.v = v;
		}
		
		/**
		 * @inheritDoc
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object o)
		{
			if(v==null 
					|| o == null 
					|| !SessionKey.class.isAssignableFrom(
					o.getClass()))
			{
				return false;
			}
			SessionKey k = (SessionKey)o;
			switch((KeyExtractorType)e)
			{
				case PUBLIC:
					return v.equals(k.getPublicKey());
				case TOKEN:
					return v.equals(k.getToken());
				default:
					return false;
			}
		}
		
		/** 
		 * @inheritDoc
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		public int hashCode()
		{
			return v.hashCode();
		}
	}

	/**
	 * The entries in this hash table extend WeakReference, using its main ref
	 * field as the key.
	 */
	private static class Entry extends WeakReference<Object> 
	implements Map.Entry<Session,SessionKey> 
	{
	    SessionKey value;
	    int hash;
	    Entry next;
	
	    /**
	     * Creates new entry.
	     */
	    Entry(Object key, SessionKey value,
	          ReferenceQueue<Object> queue,
	          int hash, Entry next) {
	        super(key, queue);
	        this.value = value;
	        this.hash  = hash;
	        this.next  = next;
	    }
	
	    public Session getKey() {
	        return (Session) get();
	    }
	
	    public SessionKey getValue() {
	        return value;
	    }
	
	    public SessionKey setValue(SessionKey newValue) {
	        SessionKey oldValue = value;
	        value = newValue;
	        return oldValue;
	    }
	
	    public boolean equals(Object o) {
	        if (!(o instanceof Map.Entry))
	            return false;
	        Map.Entry<?,?> e = (Map.Entry<?,?>)o;
	        Session k1 = getKey();
	        Object k2 = e.getKey();
	        if (k1 == k2 || (k1 != null && k1.equals(k2))) {
	            SessionKey v1 = getValue();
	            Object v2 = e.getValue();
	            if (v1 == v2 || (v1 != null && v1.equals(v2)))
	                return true;
	        }
	        return false;
	    }
	
	    public int hashCode() {
	        Session k = getKey();
	        SessionKey v = getValue();
	        return ((k==null ? 0 : k.hashCode()) ^
	                (v==null ? 0 : v.hashCode()));
	    }
	
	    public String toString() {
	        return getKey() + "=" + getValue();
	    }
	}

	//********************************************************************//
	//						ABSTRACT DECLARATIONS						  //
	//********************************************************************//
	

	//********************************************************************//
	//						STATIC DECLARATIONS		      				  //
	//********************************************************************//
	
	/**
	 * The default initial capacity -- MUST be a power of two.
	 */
	private static final int DEFAULT_INITIAL_CAPACITY = 128;
	
	/**
	 * The maximum capacity, used if a higher value is implicitly specified
	 * by either of the constructors with arguments.
	 * MUST be a power of two <= 1<<30.
	 */
	private static final int MAXIMUM_CAPACITY = 1 << 30;
	
	/**
	 * The load factor used when none specified in constructor.
	 */
	private static final float DEFAULT_LOAD_FACTOR = 0.75f;

	/**
	 * The default threshold of map capacity above which alternative hashing is
	 * used for String keys. Alternative hashing reduces the incidence of
	 * collisions due to weak hash code calculation for String keys.
	 * <p/>
	 * This value may be overridden by defining the system property
	 * {@code jdk.map.althashing.threshold}. A property value of {@code 1}
	 * forces alternative hashing to be used at all times whereas
	 * {@code -1} value ensures that alternative hashing is never used.
	 */
	static final int ALTERNATIVE_HASHING_THRESHOLD_DEFAULT = Integer.MAX_VALUE;


    /**
     * Utility method for SimpleEntry and SimpleImmutableEntry.
     * Test for equality, checking for nulls.
     */
    private static boolean eq(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

	/**
	 * Returns index for hash code h.
	 */
	private static int indexFor(int h, int length) {
	    return h & (length-1);
	}
	
	//********************************************************************//
	//						INSTANCE DECLARATIONS						  //
	//********************************************************************//
	
	/**
	 * The table, resized as necessary. Length MUST Always be a power of two.
	 */
	Entry[] table;
	
	/**
	 * The number of key-value mappings contained in this weak hash map.
	 */
	private int size;
	
	/**
	 * The next size value at which to resize (capacity * load factor).
	 */
	private int threshold;
	
	/**
	 * The load factor for the hash table.
	 */
	private final float loadFactor;
	
	/**
	 * Reference queue for cleared WeakEntries
	 */
	private final ReferenceQueue<Object> queue = new ReferenceQueue<>();

	/**
	 * Constructs a new, empty <tt>Sessions</tt> with the given initial
	 * capacity and the given load factor.
	 *
	 * @param  initialCapacity The initial capacity of the <tt>Sessions</tt>
	 * @param  loadFactor      The load factor of the <tt>Sessions</tt>
	 * @throws IllegalArgumentException if the initial capacity is negative,
	 *         or if the load factor is nonpositive.
	 */
	private Sessions(int initialCapacity, float loadFactor) {
	    if (initialCapacity < 0)
	        throw new IllegalArgumentException("Illegal Initial Capacity: "+
	                                           initialCapacity);
	    if (initialCapacity > MAXIMUM_CAPACITY)
	        initialCapacity = MAXIMUM_CAPACITY;
	
	    if (loadFactor <= 0 || Float.isNaN(loadFactor))
	        throw new IllegalArgumentException("Illegal Load factor: "+
	                                           loadFactor);
	    int capacity = 1;
	    while (capacity < initialCapacity)
	        capacity <<= 1;
	    table = newTable(capacity);
	    this.loadFactor = loadFactor;
	    threshold = (int)(capacity * loadFactor);
	}

	/**
	 * Constructs a new, empty <tt>Sessions</tt> with the default initial
	 * capacity (16) and load factor (0.75).
	 */
	public Sessions() {
	    this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
	}
	
	private Entry[] newTable(int n) {
	    return (Entry[]) new Entry[n];
	}
	
    /**
	 * Retrieve object hash code and applies a supplemental hash function to the
	 * result hash, which defends against poor quality hash functions.  This is
	 * critical because HashMap uses power-of-two length hash tables, that
	 * otherwise encounter collisions for hashCodes that do not differ
	 * in lower bits.
	 */
	int hash(Object k)
	{
	    int h= k.hashCode();
	    
	    // This function ensures that hashCodes that differ only by
	    // constant multiples at each bit position have a bounded
	    // number of collisions (approximately 8 at default load factor).
	    h ^= (h >>> 20) ^ (h >>> 12);
	    return h ^ (h >>> 7) ^ (h >>> 4);
	}
	
	
	/**
	 * Expunges stale entries from the table.
	 */
	private void expungeStaleEntries() {
	    for (Object x; (x = queue.poll()) != null; )
	    {
	        synchronized (queue) 
	        {
	            Entry e = (Entry) x;
	            int i = indexFor(e.hash, table.length);
	
	            Entry prev = table[i];
	            Entry p = prev;
	            while (p != null)
	            {
	                Entry next = p.next;
	                if (p == e) 
	                {
	                    if (prev == e)
	                        table[i] = next;
	                    else
	                        prev.next = next;
	                    // Must not null out e.next;
	                    // stale entries may be in use by a HashIterator
	                    e.value = null; // Help GC
	                    size--;
	                    break;
	                }
	                prev = p;
	                p = next;
	            }
	        }
	    }
	}
	
	/**
	 * Returns the table after first expunging stale entries.
	 */
	private Entry[] getTable() {
	    expungeStaleEntries();
	    return table;
	}
	
	/**
	 * Returns the number of key-value mappings in this map.
	 * This result is a snapshot, and may not reflect unprocessed
	 * entries that will be removed before next attempted access
	 * because they are no longer referenced.
	 */
	private int size() 
	{
	    if (size == 0)
	        return 0;
	    expungeStaleEntries();
	    return size;
	}
	
	/**
	 * Returns <tt>true</tt> if this map contains no key-value mappings.
	 * This result is a snapshot, and may not reflect unprocessed
	 * entries that will be removed before next attempted access
	 * because they are no longer referenced.
	 */
	public boolean isEmpty()
	{
	    return size() == 0;
	}
	
	/**
	 * Returns the value to which the specified key is mapped,
	 * or {@code null} if this map contains no mapping for the key.
	 *
	 * <p>More formally, if this map contains a mapping from a key
	 * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
	 * key.equals(k))}, then this method returns {@code v}; otherwise
	 * it returns {@code null}.  (There can be at most one such mapping.)
	 *
	 * <p>A return value of {@code null} does not <i>necessarily</i>
	 * indicate that the map contains no mapping for the key; it's also
	 * possible that the map explicitly maps the key to {@code null}.
	 * The {@link #containsKey containsKey} operation may be used to
	 * distinguish these two cases.
	 *
	 * @see #put(Object, Object)
	 */
	public SessionKey get(Object key) 
	{
	    Object k = key;
	    int h = hash(k);
	    Entry[] tab = getTable();
	    int index = indexFor(h, tab.length);
	    Entry e = tab[index];
	    while (e != null) 
	    {
	        if (e.hash == h 
	        	&& ((KeyExtractor.class.isAssignableFrom(key.getClass()) 
	        			&& eq(k, e.getValue()))
	        	|| (Session.class.isAssignableFrom(key.getClass()) 
	        			&& eq(k, e.getKey()))))
	        {
	            return e.value;
	        }
	        e = e.next;
	    }
	    return null;
	}
	
	private Session getSession(KeyExtractor k)
	{
	    int h = hash(k);
	    Entry[] tab = getTable();
	    int index = indexFor(h, tab.length);
	    Entry e = tab[index];
	    while (e != null) 
	    {
	        if (e.hash == h && eq(k, e.getValue()))
	        {
	            return e.getKey();
	        }
	        e = e.next;
	    }
	    return null;
	}
	
	public Session getSessionFromToken(String token)
	{
	    return getSession(new KeyExtractor<KeyExtractorType>(
	    		KeyExtractorType.TOKEN, token));
	}

	public Session getSessionFromPublicKey(String publicKey)
	{
	    return getSession(new KeyExtractor<KeyExtractorType>(
	    		KeyExtractorType.PUBLIC, publicKey));
	}
	
	/**
	 * Returns <tt>true</tt> if this map contains a mapping for the
	 * specified key.
	 *
	 * @param  key   The key whose presence in this map is to be tested
	 * @return <tt>true</tt> if there is a mapping for <tt>key</tt>;
	 *         <tt>false</tt> otherwise
	 */
	public boolean containsKey(Object key) 
	{
	    return getEntry(key) != null;
	}
	
	/**
	 * Returns the entry associated with the specified key in this map.
	 * Returns null if the map contains no mapping for this key.
	 */
	Entry getEntry(Object key) 
	{
	    Object k = key;
	    int h = hash(k);
	    Entry[] tab = getTable();
	    int index = indexFor(h, tab.length);
	    Entry e = tab[index];

	    while (e != null) 
	    {
	        if (e.hash == h 
	        	&& ((KeyExtractor.class.isAssignableFrom(key.getClass()) 
	        			&& eq(k, e.getValue()))
	        	|| (Session.class.isAssignableFrom(key.getClass()) 
	        			&& eq(k, e.getKey()))))
	        {
	            break;
	        }
	        e = e.next;
	    }
	    return e;
	}
	
	/**
	 * Associates the specified value with the specified key in this map.
	 * If the map previously contained a mapping for this key, the old
	 * value is replaced.
	 *
	 * @param key key with which the specified value is to be associated.
	 * @param value value to be associated with the specified key.
	 * @return the previous value associated with <tt>key</tt>, or
	 *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
	 *         (A <tt>null</tt> return can also indicate that the map
	 *    
	 *         previously associated <tt>null</tt> with <tt>key</tt>.)
	 */
	public void put(SessionKey key, Session value) 
	{
	    if(key == null)
	    {
	    	return;
	    }
	    KeyExtractorType[] values = KeyExtractorType.values();
	    int index = 0;
	    int length = values==null?0:values.length;
	    for(;index < length; index++)
	    {
	    	int h = 0;
		    boolean found = false;
	    	KeyExtractorType type = values[index];
	    	switch(type)
	    	{
				case PUBLIC:
					h = hash(key.getPublicKey());
					break;
				case TOKEN:
					h = hash(key.getToken());
					break;
				default: 
					h= hash(key);
					break;
	    	}		    
		    Entry[] tab = getTable();
		    int i = indexFor(h, tab.length);
		    for (Entry e = tab[i]; e != null; e = e.next) 
		    {
		        if (h == e.hash && eq(value.getId(), 
		        		e.getKey().getId())) 
		        {
		            e.value = key;
		            found = true;
		            break;
		        }
		    }
		    if(found)
		    {
		    	found=false;
		    	continue;
		    }
		    Entry e = tab[i];
		    tab[i] = new Entry(value, key, queue, h, e);
		    if (++size >= threshold)
		        resize(tab.length * 2);
	    }
	}
	
	/**
	 * Rehashes the contents of this map into a new array with a
	 * larger capacity.  This method is called automatically when the
	 * number of keys in this map reaches its threshold.
	 *
	 * If current capacity is MAXIMUM_CAPACITY, this method does not
	 * resize the map, but sets threshold to Integer.MAX_VALUE.
	 * This has the effect of preventing future calls.
	 *
	 * @param newCapacity the new capacity, MUST be a power of two;
	 *        must be greater than current capacity unless current
	 *        capacity is MAXIMUM_CAPACITY (in which case value
	 *        is irrelevant).
	 */
	void resize(int newCapacity) 
	{
	    Entry[] oldTable = getTable();
	    int oldCapacity = oldTable.length;
	    if (oldCapacity == MAXIMUM_CAPACITY) 
	    {
	        threshold = Integer.MAX_VALUE;
	        return;
	    }	
	    Entry[] newTable = newTable(newCapacity);
	    transfer(oldTable, newTable);
	    table = newTable;	
	    /*
	     * If ignoring null elements and processing ref queue caused massive
	     * shrinkage, then restore old table.  This should be rare, but avoids
	     * unbounded expansion of garbage-filled tables.
	     */
	    if (size >= threshold / 2) 
	    {
	        threshold = (int)(newCapacity * loadFactor);
	    } else 
	    {
	        expungeStaleEntries();
	        transfer(newTable, oldTable);
	        table = oldTable;
	    }
	}
	
	/** Transfers all entries from src to dest tables */
	private void transfer(Entry[] src, Entry[] dest) 
	{
	    for (int j = 0; j < src.length; ++j) {
	        Entry e = src[j];
	        src[j] = null;
	        while (e != null) {
	            Entry next = e.next;
	            Object key = e.get();
	            if (key == null) {
	                e.next = null;  // Help GC
	                e.value = null; //  "   "
	                size--;
	            } else 
	            {
	                int i = indexFor(e.hash, dest.length);
	                e.next = dest[i];
	                dest[i] = e;
	            }
	            e = next;
	        }
	    }
	}
	
	/**
	 * Removes the mapping for a key from this map if it is present.
	 * 
	 * @param key SessionKeyImpl whose mapping is to be removed from the map
	 * @return the previous value associated with <tt>key</tt>, or
	 *         <tt>null</tt> if there was no mapping for <tt>key</tt>
	 */
	public void remove(Session key) 
	{
		SessionKey skey = null;
		if(key == null || (skey=get(key))==null)
		{
			return;
		}
		KeyExtractorType[] values = KeyExtractorType.values();
	    int index = 0;
	    int length = values==null?0:values.length;

	    for(;index < length; index++)
	    {
	    	int h = 0;
	    	KeyExtractorType type = values[index];
	    	switch(type)
	    	{
				case PUBLIC:
					h = hash(skey.getPublicKey());
					break;
				case TOKEN:
					h = hash(skey.getToken());
					break;
				default: 
					h= hash(key);
					break;
	    	}
		    Entry[] tab = getTable();
		    int i = indexFor(h, tab.length);
		    Entry prev = tab[i];
		    Entry e = prev;
		
		    while (e != null) 
		    {
		        Entry next = e.next;
		        if (h == e.hash && eq(key, e.get()))
		        {
		            size--;
		            if (prev == e)
		                tab[i] = next;
		            else
		                prev.next = next;
		            break;
		        }
		        prev = e;
		        e = next;
		    }
	    }
	}
	
	/**
	 * Removes all of the mappings from this map.
	 * The map will be empty after this call returns.
	 */
	public void clear() 
	{
	    // clear out ref queue. We don't need to expunge entries
	    // since table is getting cleared.
	    while (queue.poll() != null)
	        ;
	
	    //modCount++;
	    Arrays.fill(table, null);
	    size = 0;
	
	    // Allocation of array may have caused GC, which may have caused
	    // additional entries to go stale.  Removing these entries from the
	    // reference queue will make them eligible for reclamation.
	    while (queue.poll() != null)
	        ;
	}
}
