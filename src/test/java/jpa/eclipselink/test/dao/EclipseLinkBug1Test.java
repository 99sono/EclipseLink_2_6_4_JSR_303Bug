package jpa.eclipselink.test.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.validation.ConstraintViolationException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jpa.eclipselink.test.domain.Bug1Entity;
import jpa.eclipselink.test.domain.Bug1WithWorkaroundEntity;

public class EclipseLinkBug1Test {

    EntityManagerFactory emf;
    EntityManager em;

    @Before
    public void before() {
        // (a) create an entity manager to HSQL
        emf = Persistence.createEntityManagerFactory("eclipseLinkPersistenceUnit");
        em = emf.createEntityManager();

        // (b) begin a transaction
        em.getTransaction().begin();

    }

    @After
    public void after() {
        // (a) rollback the transaction
        em.getTransaction().rollback();

    }

    /**
     * To understand this ISSUE please read the answer on: <a href=
     * "http://stackoverflow.com/questions/37310853/how-to-get-eclipselink-to-fire-jsr303-constraints-in-mapped-super-class/40942442#40942442">
     * Explains the issue On the BeanValidationHelper class</a>
     *
     * <P>
     * SUMMARY: <br>
     * The NotNull validation constraint on the parent class is not geting fired. Because BenValidationHelpoer does not
     * figure out that Bug1Entity needs to be JSR validated because the method is not recursive.
     *
     */
    @Test
    public void peristEntityBug1ShowCaseTest() {
        em.persist(new Bug1Entity());
        Assert.fail(
                "A JSR 303 constraint should have been fired. But it is not because the annotations are on the parent class.");

    }

    /**
     * Explanation, because the bug1 entity in this case has in there a dummy transient field that is JSR annotated,
     * eclipse link will know that this class should be bean validated.
     */
    @Test
    public void bug1WorkAroundDemonstrationTest() {
        try {
            em.persist(new Bug1WithWorkaroundEntity());
            Assert.fail(
                    "A JSR 303 constraint should have been fired. But it is not because the annotations are on the parent class.");
        } catch (ConstraintViolationException e) {
            System.out.println(
                    "This is exactly what we want to happen. To get a JSR validation. The work-around works for this BUG 1.");

        }

    }

}
