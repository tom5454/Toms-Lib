package com.tom.lib.debug;

import java.io.InvalidObjectException;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;

public class CMESet<E>  extends AbstractSet<E>
implements Set<E>, Cloneable, java.io.Serializable {
	static final long serialVersionUID = -5024744406713321676L;

	private transient CMEMap<E,Object> map;

	// Dummy value to associate with an Object in the backing Map
	private static final Object PRESENT = new Object();

	/**
	 * Constructs a new, empty set; the backing <tt>CMEMap</tt> instance has
	 * default initial capacity (16) and load factor (0.75).
	 */
	public CMESet() {
		map = new CMEMap<>();
	}

	/**
	 * Constructs a new set containing the elements in the specified
	 * collection.  The <tt>CMEMap</tt> is created with default load factor
	 * (0.75) and an initial capacity sufficient to contain the elements in
	 * the specified collection.
	 *
	 * @param c the collection whose elements are to be placed into this set
	 * @throws NullPointerException if the specified collection is null
	 */
	public CMESet(Collection<? extends E> c) {
		map = new CMEMap<>(Math.max((int) (c.size()/.75f) + 1, 16));
		addAll(c);
	}

	/**
	 * Constructs a new, empty set; the backing <tt>CMEMap</tt> instance has
	 * the specified initial capacity and the specified load factor.
	 *
	 * @param      initialCapacity   the initial capacity of the hash map
	 * @param      loadFactor        the load factor of the hash map
	 * @throws     IllegalArgumentException if the initial capacity is less
	 *             than zero, or if the load factor is nonpositive
	 */
	public CMESet(int initialCapacity, float loadFactor) {
		map = new CMEMap<>(initialCapacity, loadFactor);
	}

	/**
	 * Constructs a new, empty set; the backing <tt>CMEMap</tt> instance has
	 * the specified initial capacity and default load factor (0.75).
	 *
	 * @param      initialCapacity   the initial capacity of the hash table
	 * @throws     IllegalArgumentException if the initial capacity is less
	 *             than zero
	 */
	public CMESet(int initialCapacity) {
		map = new CMEMap<>(initialCapacity);
	}

	/**
	 * Returns an iterator over the elements in this set.  The elements
	 * are returned in no particular order.
	 *
	 * @return an Iterator over the elements in this set
	 * @see ConcurrentModificationException
	 */
	@Override
	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	/**
	 * Returns the number of elements in this set (its cardinality).
	 *
	 * @return the number of elements in this set (its cardinality)
	 */
	@Override
	public int size() {
		return map.size();
	}

	/**
	 * Returns <tt>true</tt> if this set contains no elements.
	 *
	 * @return <tt>true</tt> if this set contains no elements
	 */
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * Returns <tt>true</tt> if this set contains the specified element.
	 * More formally, returns <tt>true</tt> if and only if this set
	 * contains an element <tt>e</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
	 *
	 * @param o element whose presence in this set is to be tested
	 * @return <tt>true</tt> if this set contains the specified element
	 */
	@Override
	public boolean contains(Object o) {
		return map.containsKey(o);
	}

	/**
	 * Adds the specified element to this set if it is not already present.
	 * More formally, adds the specified element <tt>e</tt> to this set if
	 * this set contains no element <tt>e2</tt> such that
	 * <tt>(e==null&nbsp;?&nbsp;e2==null&nbsp;:&nbsp;e.equals(e2))</tt>.
	 * If this set already contains the element, the call leaves the set
	 * unchanged and returns <tt>false</tt>.
	 *
	 * @param e element to be added to this set
	 * @return <tt>true</tt> if this set did not already contain the specified
	 * element
	 */
	@Override
	public boolean add(E e) {
		return map.put(e, PRESENT)==null;
	}

	/**
	 * Removes the specified element from this set if it is present.
	 * More formally, removes an element <tt>e</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>,
	 * if this set contains such an element.  Returns <tt>true</tt> if
	 * this set contained the element (or equivalently, if this set
	 * changed as a result of the call).  (This set will not contain the
	 * element once the call returns.)
	 *
	 * @param o object to be removed from this set, if present
	 * @return <tt>true</tt> if the set contained the specified element
	 */
	@Override
	public boolean remove(Object o) {
		return map.remove(o)==PRESENT;
	}

	/**
	 * Removes all of the elements from this set.
	 * The set will be empty after this call returns.
	 */
	@Override
	public void clear() {
		map.clear();
	}

	/**
	 * Returns a shallow copy of this <tt>CMESet</tt> instance: the elements
	 * themselves are not cloned.
	 *
	 * @return a shallow copy of this set
	 */
	@Override
	@SuppressWarnings("unchecked")
	public Object clone() {
		try {
			CMESet<E> newSet = (CMESet<E>) super.clone();
			newSet.map = (CMEMap<E, Object>) map.clone();
			return newSet;
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e);
		}
	}

	/**
	 * Save the state of this <tt>CMESet</tt> instance to a stream (that is,
	 * serialize it).
	 *
	 * @serialData The capacity of the backing <tt>CMEMap</tt> instance
	 *             (int), and its load factor (float) are emitted, followed by
	 *             the size of the set (the number of elements it contains)
	 *             (int), followed by all of its elements (each an Object) in
	 *             no particular order.
	 */
	private void writeObject(java.io.ObjectOutputStream s)
			throws java.io.IOException {
		// Write out any hidden serialization magic
		s.defaultWriteObject();

		// Write out CMEMap capacity and load factor
		s.writeInt(map.capacity());
		s.writeFloat(map.loadFactor());

		// Write out size
		s.writeInt(map.size());

		// Write out all elements in the proper order.
		for (E e : map.keySet())
			s.writeObject(e);
	}

	/**
	 * Reconstitute the <tt>CMESet</tt> instance from a stream (that is,
	 * deserialize it).
	 */
	private void readObject(java.io.ObjectInputStream s)
			throws java.io.IOException, ClassNotFoundException {
		// Read in any hidden serialization magic
		s.defaultReadObject();

		// Read capacity and verify non-negative.
		int capacity = s.readInt();
		if (capacity < 0) {
			throw new InvalidObjectException("Illegal capacity: " +
					capacity);
		}

		// Read load factor and verify positive and non NaN.
		float loadFactor = s.readFloat();
		if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
			throw new InvalidObjectException("Illegal load factor: " +
					loadFactor);
		}

		// Read size and verify non-negative.
		int size = s.readInt();
		if (size < 0) {
			throw new InvalidObjectException("Illegal size: " +
					size);
		}

		// Set the capacity according to the size and load factor ensuring that
		// the CMEMap is at least 25% full but clamping to maximum capacity.
		capacity = (int) Math.min(size * Math.min(1 / loadFactor, 4.0f),
				CMEMap.MAXIMUM_CAPACITY);

		// Read in all elements in the proper order.
		for (int i=0; i<size; i++) {
			@SuppressWarnings("unchecked")
			E e = (E) s.readObject();
			map.put(e, PRESENT);
		}
	}

	/**
	 * Creates a <em><a href="Spliterator.html#binding">late-binding</a></em>
	 * and <em>fail-fast</em> {@link Spliterator} over the elements in this
	 * set.
	 *
	 * <p>The {@code Spliterator} reports {@link Spliterator#SIZED} and
	 * {@link Spliterator#DISTINCT}.  Overriding implementations should document
	 * the reporting of additional characteristic values.
	 *
	 * @return a {@code Spliterator} over the elements in this set
	 * @since 1.8
	 */
	@Override
	public Spliterator<E> spliterator() {
		return new CMEMap.KeySpliterator<>(map, 0, -1, 0, 0);
	}
}
