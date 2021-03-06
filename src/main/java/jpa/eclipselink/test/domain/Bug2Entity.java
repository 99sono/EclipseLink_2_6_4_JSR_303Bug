package jpa.eclipselink.test.domain;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.validation.constraints.NotNull;

/**
 * This entity has no JSR annotations. Eclipse link will not have the intelligence of notince that the parent class does
 * have and so the blowup will happen on the DB not null field itself.
 */
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DESCRIMINATOR", length = 32)
@DiscriminatorValue("Bug2")
@Entity
public class Bug2Entity extends GenericEntity {

    @NotNull
    @javax.validation.constraints.Min(18)
    private Integer age;

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

}
