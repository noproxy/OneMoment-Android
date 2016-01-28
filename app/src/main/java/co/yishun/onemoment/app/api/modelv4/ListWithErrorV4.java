package co.yishun.onemoment.app.api.modelv4;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Jinge on 2016/1/23.
 */
public class ListWithErrorV4<E> extends ApiModel implements ListErrorProvider<E> {
    List<E> mList;

    public ListWithErrorV4(List<E> mList) {
        super();
        this.mList = mList;
    }

    @Override public void add(int location, E object) {
        mList.add(location, object);
    }

    @Override public boolean add(E object) {
        return mList.add(object);
    }

    @Override public boolean addAll(int location, Collection<? extends E> collection) {
        return mList.addAll(location, collection);
    }

    @Override public boolean addAll(Collection<? extends E> collection) {
        return mList.addAll(collection);
    }

    @Override public void clear() {
        mList.clear();
    }

    @Override public boolean contains(Object object) {
        return mList.contains(object);
    }

    @Override public boolean containsAll(Collection<?> collection) {
        return mList.containsAll(collection);
    }

    @Override public boolean equals(Object object) {
        return mList.equals(object);
    }

    @Override public E get(int location) {
        return mList.get(location);
    }

    @Override public int hashCode() {
        return mList.hashCode();
    }

    @Override public int indexOf(Object object) {
        return mList.indexOf(object);
    }

    @Override public boolean isEmpty() {
        return mList.isEmpty();
    }

    @NonNull @Override public Iterator<E> iterator() {
        return mList.iterator();
    }

    @Override public int lastIndexOf(Object object) {
        return mList.lastIndexOf(object);
    }

    @Override public ListIterator<E> listIterator() {
        return mList.listIterator();
    }

    @NonNull @Override public ListIterator<E> listIterator(int location) {
        return mList.listIterator(location);
    }

    @Override public E remove(int location) {
        return mList.remove(location);
    }

    @Override public boolean remove(Object object) {
        return mList.remove(object);
    }

    @Override public boolean removeAll(Collection<?> collection) {
        return mList.removeAll(collection);
    }

    @Override public boolean retainAll(Collection<?> collection) {
        return mList.retainAll(collection);
    }

    @Override public E set(int location, E object) {
        return mList.set(location, object);
    }

    @Override public int size() {
        return mList.size();
    }

    @NonNull @Override public List<E> subList(int start, int end) {
        return mList.subList(start, end);
    }

    @NonNull @Override public Object[] toArray() {
        return mList.toArray();
    }

    @NonNull @Override public <T> T[] toArray(T[] array) {
        return mList.toArray(array);
    }
}
