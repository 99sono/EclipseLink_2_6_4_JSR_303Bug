package jpa.eclipselink.test.domain;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

@MappedSuperclass

public abstract class GenericEntity {
    /**
     * This is required for various persistence mechanisms.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Version
    private Long version;

    @NotNull
    private String notNullField = null;

    /**
     * Required for JPA.
     *
     * @see javax.persistence.Id
     */
    public Long getId() {
        return id;
    }

    /**
     * Required for JPA.
     *
     * @see javax.persistence.Version
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Required for JPA.
     *
     * @see javax.persistence.Id
     */
    public void setId(final Long id) {
        this.id = id;
    }

    /**
     * Required for JPA.
     *
     * @see javax.persistence.Version
     */
    public void setVersion(final Long version) {
        this.version = version;
    }

    public String getNotNullField() {
        return notNullField;
    }

    public void setNotNullField(String notNullField) {
        this.notNullField = notNullField;
    }

}
