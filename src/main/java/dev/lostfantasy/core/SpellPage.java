package dev.lostfantasy.core;

/** Browsing the full catalog is independent of the four equipped slots. */
public final class SpellPage {
    public static final int ROWS = 4;
    private final int[] ids;
    private int page;

    public SpellPage(int[] ids) { this.ids = ids.clone(); }
    public int page() { return page; }
    public int pageCount() { return Math.max(1, (int) ((ids.length + (long) ROWS - 1) / ROWS)); }
    public int visibleCount() { return Math.min(ROWS, ids.length - page * ROWS); }
    public int spellIdAt(int row) {
        return row >= 0 && row < visibleCount() ? ids[page * ROWS + row] : -1;
    }
    public void move(int direction) {
        if (direction < 0 && page > 0) page--;
        if (direction > 0 && page + 1 < pageCount()) page++;
    }
}
