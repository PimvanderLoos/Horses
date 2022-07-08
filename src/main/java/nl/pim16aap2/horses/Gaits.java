package nl.pim16aap2.horses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Gaits
{
    private final int[] gaitsArr;
    private final List<Integer> gaitsList;

    public Gaits(List<Integer> values)
    {
        gaitsArr = toArray(new ArrayList<>(values));
        gaitsList = Collections.unmodifiableList(asList(gaitsArr));
    }

    public int getHigherGait(int currentValue)
    {
        if (gaitsArr.length == 0)
            return currentValue;
        if (gaitsArr.length == 1)
            return gaitsArr[0];

        final int nextIdx = findNextIndex(currentValue);
        return nextIdx == -1 ? gaitsArr[gaitsArr.length - 1] : gaitsArr[nextIdx];
    }

    public int getLowerGait(int currentValue)
    {
        if (gaitsArr.length == 0)
            return currentValue;
        if (gaitsArr.length == 1)
            return gaitsArr[0];

        final int nextIdx = findPreviousIndex(currentValue);
        return nextIdx == -1 ? gaitsArr[0] : gaitsArr[nextIdx];
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
        return findNextIndex(target, -1, 0, gaitsArr.length);
    }

    private int findNextIndex(int target, int ans, int start, int length)
    {
        if (length == 0)
            return ans;
        if (length == 1)
            return gaitsArr[start] > target ? start : ans;

        final int half = length / 2;
        final int mid = start + half;

        if (gaitsArr[mid] <= target)
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
        return findPreviousIndex(target, -1, 0, gaitsArr.length);
    }

    private int findPreviousIndex(int target, int ans, int start, int length)
    {
        if (length == 0)
            return ans;
        if (length == 1)
            return gaitsArr[start] < target ? start : ans;

        final int half = length / 2;
        final int mid = start + half;

        if (gaitsArr[mid] >= target)
            return findPreviousIndex(target, ans, start, half);
        return findPreviousIndex(target, mid, mid, length - half);
    }

    int[] getGaits()
    {
        final int[] ret = new int[gaitsArr.length];
        System.arraycopy(gaitsArr, 0, ret, 0, gaitsArr.length);
        return ret;
    }

    public List<Integer> gaitsAsList()
    {
        return gaitsList;
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

    @SuppressWarnings("PMD.UseVarargs")
    private static List<Integer> asList(int[] values)
    {
        final ArrayList<Integer> ret = new ArrayList<>(values.length);
        for (final int value : values)
            ret.add(value);
        return ret;
    }
}
