package nl.pim16aap2.horses;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Gaits
{
    private final int[] gaits;

    public Gaits(List<Integer> values)
    {
        gaits = toArray(new ArrayList<>(values));
    }

    public int getHigherGait(int currentValue)
    {
        if (gaits.length == 0)
            return currentValue;
        if (gaits.length == 1)
            return gaits[0];

        final int nextIdx = findNextIndex(currentValue);
        return nextIdx == -1 ? gaits[gaits.length - 1] : gaits[nextIdx];
    }

    public int getLowerGait(int currentValue)
    {
        if (gaits.length == 0)
            return currentValue;
        if (gaits.length == 1)
            return gaits[0];

        final int nextIdx = findPreviousIndex(currentValue);
        return nextIdx == -1 ? gaits[0] : gaits[nextIdx];
    }

    /**
     * Returns index of the lowest value that is higher than the provided target value.
     *
     * @param target
     *     The target value.
     * @return The index of the lowest value in the array that is greater than the target value. If no such value
     * exists, -1 is returned.
     */
    int findNextIndex(int target)
    {
        return findNextIndex(target, -1, 0, gaits.length);
    }

    private int findNextIndex(int target, int ans, int start, int length)
    {
        if (length == 0)
            return ans;
        if (length == 1)
            return gaits[start] > target ? start : ans;

        final int half = length / 2;
        final int mid = start + half;

        if (gaits[mid] <= target)
            return findNextIndex(target, ans, mid + 1, length - half - 1);
        return findNextIndex(target, mid, start, half);
    }

    /**
     * Returns index of the highest value that is less than the provided target value.
     *
     * @param target
     *     The target value.
     * @return The index of the highest value in the array that is less than the target value. If no such value exists,
     * -1 is returned.
     */
    int findPreviousIndex(int target)
    {
        return findPreviousIndex(target, -1, 0, gaits.length);
    }

    private int findPreviousIndex(int target, int ans, int start, int length)
    {
        if (length == 0)
            return ans;
        if (length == 1)
            return gaits[start] < target ? start : ans;

        final int half = length / 2;
        final int mid = start + half;

        if (gaits[mid] >= target)
            return findPreviousIndex(target, ans, start, half);
        return findPreviousIndex(target, mid, mid, length - half);
    }

    int[] getGaits()
    {
        final int[] ret = new int[gaits.length];
        System.arraycopy(gaits, 0, ret, 0, gaits.length);
        return ret;
    }

    private static int[] toArray(List<Integer> values)
    {
        final List<Integer> filtered =
            values.stream().filter(Objects::nonNull).filter(val -> val >= 0).sorted().toList();
        final int[] arr = new int[filtered.size()];
        int idx = 0;
        for (final Integer value : filtered)
            arr[idx++] = value;
        return arr;
    }
}
