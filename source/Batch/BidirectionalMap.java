package Batch;

import java.util.HashMap;

public class BidirectionalMap<A, B> {
    public static final int SUCCESS = 0;
    public static final int EXISTS_FRONT = 1;
    public static final int EXISTS_BACK = 2;
    public static final int EXISTS_BOTH = 3;
    public static final int NULL_FRONT = 4;
    public static final int NULL_BACK = 8;
    public static final int NULL_BOTH = 12;
    public static final int FAIL_FRONT = 5;
    public static final int FAIL_BACK = 10;

    final HashMap<A, B> frontToBack = new HashMap<>();
    final HashMap<B, A> backToFront = new HashMap<>();

    public int maybeAdd(A front, B back) {
        int status = SUCCESS;

        if (front == null)
            status |= NULL_FRONT;
        else if (frontToBack.containsKey(front))
            status |= EXISTS_FRONT;

        if (back == null)
            status |= NULL_BACK;
        else if (backToFront.containsKey(back))
            status |= EXISTS_BACK;

        if (status != SUCCESS)
            return status;

        frontToBack.put(front, back);
        backToFront.put(back, front);
        return SUCCESS;
    }

    public A getFront(B back) {
        return backToFront.get(back);
    }

    public B getBack(A front) {
        return frontToBack.get(front);
    }
}
