package nl.pim16aap2.horses;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

class GaitsTest
{
    @Test
    void testParser()
    {
        Assertions.assertArrayEquals(new int[]{0, 1, 2, 6, 100}, new Gaits(List.of(-10, 6, 1, 2, 0, 100)).getGaits());
    }

    @Test
    void testGetHigherGait()
    {
        final Gaits gaits = new Gaits(List.of(0, 1, 2, 3, 5, 6, 7, 12));
        Assertions.assertEquals(12, gaits.getHigherGait(7));
        Assertions.assertEquals(5, gaits.getHigherGait(4));
        Assertions.assertEquals(0, gaits.getHigherGait(-10));
        Assertions.assertEquals(12, gaits.getHigherGait(12));
        Assertions.assertEquals(12, gaits.getHigherGait(120));

        Assertions.assertEquals(42, new Gaits(Collections.emptyList()).getHigherGait(42));
        Assertions.assertEquals(12, new Gaits(List.of(12)).getHigherGait(42));
    }

    @Test
    void testGetLowerGait()
    {
        final Gaits gaits = new Gaits(List.of(0, 1, 2, 3, 5, 6, 7, 12));
        Assertions.assertEquals(6, gaits.getLowerGait(7));
        Assertions.assertEquals(3, gaits.getLowerGait(4));
        Assertions.assertEquals(0, gaits.getLowerGait(-10));
        Assertions.assertEquals(7, gaits.getLowerGait(12));
        Assertions.assertEquals(12, gaits.getLowerGait(120));

        Assertions.assertEquals(42, new Gaits(Collections.emptyList()).getLowerGait(42));
        Assertions.assertEquals(12, new Gaits(List.of(12)).getLowerGait(42));
    }

    @Test
    void testFindNextIndex()
    {
        final Gaits gaits = new Gaits(List.of(0, 1, 2, 3, 5, 6, 7));
        Assertions.assertEquals(-1, gaits.findNextIndex(7));
        Assertions.assertEquals(6, gaits.findNextIndex(6));
        Assertions.assertEquals(1, gaits.findNextIndex(0));
        Assertions.assertEquals(4, gaits.findNextIndex(4));
        Assertions.assertEquals(0, gaits.findNextIndex(-10));
    }

    @Test
    void testFindPreviousIndex()
    {
        final Gaits gaits = new Gaits(List.of(0, 1, 2, 3, 5, 6, 7));
        Assertions.assertEquals(-1, gaits.findPreviousIndex(0));
        Assertions.assertEquals(0, gaits.findPreviousIndex(1));
        Assertions.assertEquals(5, gaits.findPreviousIndex(7));
        Assertions.assertEquals(3, gaits.findPreviousIndex(4));
        Assertions.assertEquals(6, gaits.findPreviousIndex(99));
    }
}
