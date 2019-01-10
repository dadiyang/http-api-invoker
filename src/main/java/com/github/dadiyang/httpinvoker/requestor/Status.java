package com.github.dadiyang.httpinvoker.requestor;

/**
 * enum for http status classification
 *
 * @author dadiyang
 * date 2019/1/10
 */
public enum Status {
    /**
     * 20x
     */
    OK(200, 299),
    /**
     * 30x
     */
    REDIRECT(300, 399),
    /**
     * 40x
     */
    NOT_FOUND(400, 499),
    /**
     * 50x
     */
    SERVER_ERROR(500, 599);
    private int from;
    private int to;

    Status(int from, int to) {
        this.from = from;
        this.to = to;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }
}
