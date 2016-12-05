package jpa.eclipselink.test.bug2workaround;

import java.util.Map;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.descriptors.DescriptorEvent;
import org.eclipse.persistence.descriptors.DescriptorEventAdapter;
import org.eclipse.persistence.descriptors.changetracking.DeferredChangeDetectionPolicy;
import org.eclipse.persistence.internal.jpa.deployment.BeanValidationInitializationHelper;
import org.eclipse.persistence.internal.jpa.metadata.listeners.BeanValidationListener;

/**
 * Temporary work-around for JSR 303 bean validation flow in eclipselink.
 *
 * <P>
 * Problem: <br>
 * The
 * {@link DeferredChangeDetectionPolicy#calculateChanges(Object, Object, boolean, org.eclipse.persistence.internal.sessions.UnitOfWorkChangeSet, org.eclipse.persistence.internal.sessions.UnitOfWorkImpl, org.eclipse.persistence.descriptors.ClassDescriptor, boolean)}
 * during a flush will do one of the following: <br>
 * {@code descriptor.getEventManager().executeEvent(new DescriptorEvent(DescriptorEventManager.PreInsertEvent, writeQuery)); }
 * or <br>
 *
 * {@code descriptor.getEventManager().executeEvent(new DescriptorEvent(DescriptorEventManager.PreUpdateEvent, writeQuery)); }
 *
 * <P>
 * WHe it does
 * {@code descriptor.getEventManager().executeEvent(new DescriptorEvent(DescriptorEventManager.PreInsertEvent, writeQuery)); }
 * the {@link BeanValidationListener} will not do anything. We want it to do bean validation.
 */
public class ForceBeanManagerValidationOnPreInsert extends DescriptorEventAdapter {

    private static final Class[] DUMMY_GROUP_PARAMETER = null;

    /**
     * This is is the EJB validator that eclipselink uses to do JSR 303 validations during pre-update, pre-delete,
     * pre-persist, but not pre-insert.
     *
     * Do not access this field directly. Use the {@link #getBeanValidationListener(DescriptorEvent)} api to get it, as
     * this api will initialize the tool if necessary.
     */
    BeanValidationListener beanValidationListener = null;

    final Object beanValidationListenerLock = new Object();

    /**
     *
     */
    public ForceBeanManagerValidationOnPreInsert() {
        super();

    }

    /**
     * As a work-around we want to do bean validation that the container is currently not doing.
     */
    @Override
    public void preInsert(DescriptorEvent event) {
        // (a) get for ourselves an instances of the eclipse link " Step 4 - Notify internal listeners."
        // that knows how to run JSR 303 validations on beans associated to descriptor events
        BeanValidationListener eclipseLinkBeanValidationListenerTool = getBeanValidationListener(event);

        // (b) let the validation listener run its pre-update logic on a preInsert it serves our purpose
        eclipseLinkBeanValidationListenerTool.preUpdate(event);

    }

    /**
     * Returns the BeanValidationListener that knows how to do JSR 303 validation. Creates a new instance if needed,
     * otherwise return the already created listener.
     *
     * <P>
     * We can only initialize our {@link BeanValidationListener} during runtime, to get access to the JPA persistence
     * unit properties. (e.g. to the validation factory).
     *
     * @param event
     *            This event describes an ongoing insert, updetae, delete event on an entity and for which we may want
     *            to force eclipselink to kill the transaction if a JSR bean validation fails.
     * @return the BeanValidationListener that knows how to do JSR 303 validation.
     */
    protected BeanValidationListener getBeanValidationListener(DescriptorEvent event) {
        synchronized (beanValidationListenerLock) {
            // (a) initializae our BeanValidationListener if needed
            boolean initializationNeeded = beanValidationListener == null;
            if (initializationNeeded) {
                beanValidationListener = createBeanValidationListener(event);
            }

            // (b) return the validation listerner that is normally used by eclipse link
            // for pre-persist, pre-update and pre-delete so that we can force it run on pre-insert
            return beanValidationListener;
        }

    }

    /**
     * Creates a new instance of the {@link BeanValidationListener} that comes with eclipse link.
     *
     * @param event
     *            the ongoing db event (e.g. pre-insert) where we want to trigger JSR 303 bean validation.
     *
     * @return A new a new instance of the {@link BeanValidationListener} .
     */
    protected BeanValidationListener createBeanValidationListener(DescriptorEvent event) {
        Map peristenceUnitProperties = event.getSession().getProperties();
        ValidatorFactory validatorFactory = getValidatorFactory(peristenceUnitProperties);
        return new BeanValidationListener(validatorFactory, DUMMY_GROUP_PARAMETER, DUMMY_GROUP_PARAMETER,
                DUMMY_GROUP_PARAMETER);
    }

    /**
     * Snippet of code taken out of {@link BeanValidationInitializationHelper}
     *
     * @param puProperties
     *            the persistence unit properties that may be specifying the JSR 303 validation factory.
     * @return the validation factory that can check if a bean is violating business rules. Almost everyone uses
     *         hirbernate JSR 303 validation.
     */
    protected ValidatorFactory getValidatorFactory(Map puProperties) {
        ValidatorFactory validatorFactory = (ValidatorFactory) puProperties
                .get(PersistenceUnitProperties.VALIDATOR_FACTORY);

        if (validatorFactory == null) {
            validatorFactory = Validation.buildDefaultValidatorFactory();
        }
        return validatorFactory;
    }

}
