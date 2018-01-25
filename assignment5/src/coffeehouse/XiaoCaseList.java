package coffeehouse;

import java.util.concurrent.atomic.AtomicReference;

public class XiaoCaseList<T> {
	private final Node<T> dummy = new Node<T>(null, null);
	private final AtomicReference<Node<T>> first = new AtomicReference<Node<T>>(dummy);
	private final AtomicReference<Node<T>> last = new AtomicReference<Node<T>>(dummy);
	
	private static class Node <T> {
		final T object;
		final AtomicReference<Node<T>> next;

		public Node(T object, Node<T> next) {
			this.object = object;
			this.next = new AtomicReference<Node<T>>(next);
		}
		public T get() {
			return object;
		}
	}

	
	public boolean add(T object) {
		Node<T> newNode = new Node<T>(object, null);
		while (true) {
			Node<T> tail = last.get();
			Node<T> next = tail.next.get();
		    if (tail == last.get()) {
		    	if (next != null) last.compareAndSet(tail, next);
		    	else if (tail.next.compareAndSet(next, newNode)) {
	    			last.compareAndSet(tail, newNode);
	    			return true; 
		    	}
		    }
		}	
	}
	
	public T remove() {
		while (true) {
			Node<T> head = first.get();
			Node<T> tail = last.get();;
			Node<T> next = head.next.get();
		    if (head == first.get()) {
		    	if (head == tail) {
		    		if (next == null) return null;
		    		else last.compareAndSet(tail, next);
		    	}
		    	else if (first.compareAndSet(head, next)) {
		    		T object = next.get();
		    		if(object != null) {
		    			next = null;
		    			return object;
		    		}
		    	}
		    }
		}	
	}
	
	public boolean isEmpty() {
		Node<T> head = first.get();
		Node<T> tail = last.get();;
		Node<T> next = head.next.get();
		if(head == first.get()) {
			if(head == tail) {
				if (next == null) return true;
			}
		}
		return false;
	}
}
