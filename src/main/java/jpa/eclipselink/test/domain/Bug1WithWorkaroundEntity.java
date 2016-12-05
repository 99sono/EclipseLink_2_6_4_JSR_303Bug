package jpa.eclipselink.test.domain;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

/**
 * This entity has no JSR annotations. Eclipse link will not have the intelligence of notince that the parent class does
 * have and so the blowup will happen on the DB not null field itself.
 */
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DESCRIMINATOR", length = 32)
@DiscriminatorValue("Bug1BugFix")
@Entity
public class Bug1WithWorkaroundEntity extends GenericEntity {

    @Transient
    @NotNull
    private final char waitForEclipseLinkToFixTheVersion264 = 'a';
}
