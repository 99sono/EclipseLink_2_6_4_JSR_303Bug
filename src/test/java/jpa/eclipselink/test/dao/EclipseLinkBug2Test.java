package jpa.eclipselink.test.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.RollbackException;
import javax.validation.ConstraintViolationException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jpa.eclipselink.test.domain.Bug2Entity;

public class EclipseLinkBug2Test {

    EntityManagerFactory emf;
    EntityManager em;

    private static final int ALLOWED_AGE = 18;
    private static final int NOT_ALLOWED_AGE = 17;

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
        try {
            em.getTransaction().rollback();
        } catch (Exception ignoreE) {
            System.out.println("Rollback not possible. This is ok. The test has issued a commit to proove a point.");
        }

    }

    /**
     * In this test everything goes alright and no business rule gets broken.
     *
     */
    @Test
    public void placeboTest() {
        // (a) persisting a valid entity No problem
        Bug2Entity entityFlowThatWorks = new Bug2Entity();
        entityFlowThatWorks.setNotNullField("InThisTestTheNotNullFieldIsIrrelevantToUs");
        entityFlowThatWorks.setAge(ALLOWED_AGE);
        em.persist(entityFlowThatWorks);

        // (b) persisting a valid entity No problem
        try {
            Bug2Entity entityFlowThatWorksB = new Bug2Entity();
            entityFlowThatWorksB.setNotNullField("InThisTestTheNotNullFieldIsIrrelevantToUs");
            entityFlowThatWorksB.setAge(NOT_ALLOWED_AGE);
            em.persist(entityFlowThatWorksB);
            Assert.fail(
                    "Code is not expected to here. JPA should be invoked in prePersist even the JSR 303 bean validations");
        } catch (ConstraintViolationException e) {
            System.out.println(
                    "This is what eclipse link is expecting to be doing we are using a not allowed age on our entity.");
        }

    }

    /**
     * To understand this ISSUE please read the answer on: <a href=
     * "http://stackoverflow.com/questions/37310853/how-to-get-eclipselink-to-fire-jsr303-constraints-in-mapped-super-class/40942442#40942442">
     * Explains the issue On the BeanValidationHelper class</a>
     *
     */
    @Test
    public void demonstrateHowWeCanViolateBusinessRulesByFollowingADifferentFlow() {
        // (a) we start by calling persist on a fresh new entity thaat starts off being valid
        Bug2Entity bugThatNeedsFixing = new Bug2Entity();
        bugThatNeedsFixing.setNotNullField("InThisTestTheNotNullFieldIsIrrelevantToUs");
        bugThatNeedsFixing.setAge(ALLOWED_AGE);
        em.persist(bugThatNeedsFixing);

        // (b) now we demonstrate how eclipse link fails to invoke the JSR validations on entity
        // when we modify the entity during the transaction

        try {
            bugThatNeedsFixing.setAge(NOT_ALLOWED_AGE);
            em.getTransaction().commit();

            Bug2Entity weHaveJustCommittedAnEntityWithInvalidAge = em.find(Bug2Entity.class,
                    bugThatNeedsFixing.getId());
            System.out.println("We have just committed an entity with violation bean rulles. Its age is: "
                    + weHaveJustCommittedAnEntityWithInvalidAge.getAge());
            Assert.fail("A JSR 303 constraint should have been fired. "
                    + "Eclipse link is not firing the constraint violation exception because the "
                    + "org.eclipse.persistence.internal.jpa.metadata.listeners.BeanValiationListner "
                    + " does not subscribe to the preInsert event. So if commit we violate a business rule.");
        } catch (ConstraintViolationException | RollbackException e) {
            System.out.println(
                    "Yes we would have liked eclipselink to have triggered the JSR bean validation and to get the constraint violation."
                            + " Instead we now have a corrupt database. ");
        }

    }

}
