package sn.microbank.dao;

import java.util.List;

public class PagedResult<T> {

    private final List<T> items;
    private final long total;
    private final int page;
    private final int size;

    public PagedResult(List<T> items, long total, int page, int size) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public List<T> getItems() {
        return items;
    }

    public long getTotal() {
        return total;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotalPages() {
        return Math.max(1, (int) Math.ceil((double) total / size));
    }

    public boolean isHasPrevious() {
        return page > 0;
    }

    public boolean isHasNext() {
        return page < getTotalPages() - 1;
    }
}
