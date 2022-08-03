package nl.pim16aap2.horses.baby;


import org.jetbrains.annotations.Nullable;

public record Parents(@Nullable Parent father, @Nullable Parent mother)
{
    public Parents withFather(@Nullable Parent newFather)
    {
        return new Parents(newFather, this.mother);
    }

    public Parents withMother(@Nullable Parent newMother)
    {
        return new Parents(this.father, newMother);
    }
}
