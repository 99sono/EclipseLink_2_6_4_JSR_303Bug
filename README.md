# Eclipselink 2.6.4 bugs

Two bugs exist in eclipse link 2.6.4 related to JSR 303 valiations both bugs are absolutely critical.
BUG 1:
The identification of when a class should be bean validated or not is done by an improper non recursive algorithm that does
not explore fields of the parent class to figure out if there are bean validation rules at any level of the hierarhcy.

BUG 2:
The BeanValidationListener subscribes to validations on prePersist, preUpdate and preDelete.
This allows us to commit entities violationg validations rules if we first em.persit() and then modify the entity.
And finally em.commit()


All the details for this are given in two spots:
(A) https://www.eclipse.org/forums/index.php/m/1749303/#msg_1749303
(B) http://stackoverflow.com/questions/37310853/how-to-get-eclipselink-to-fire-jsr303-constraints-in-mapped-super-class/40942442#40942442

There are two unit test classes in this project.

One of the tests fails demonstrating the bug.
The other unit tests passing demonstrating either a work-around, in the case of bug1, or simply a placebo demonstration of what

Test results are expected to be as follows:



Results :

Failed tests:
  EclipseLinkBug1Test.peristEntityBug1ShowCaseTest:53 A JSR 303 constraint should have been fired. But it is not because the annotations are on the parent class
.
  EclipseLinkBug2Test.demonstrateHowWeCanViolateBusinessRulesByFollowingADifferentFlow:98 A JSR 303 constraint should have been fired. Eclipse link is not firin
g the constraint violation exception because the org.eclipse.persistence.internal.jpa.metadata.listeners.BeanValiationListner  does not subscribe to the preInse
rt event. So if commit we violate a business rule.

Tests run: 4, Failures: 2, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 7.038 s
[INFO] Finished at: 2016-12-05T12:46:24+01:00
[INFO] Final Memory: 20M/284M
[INFO] ------------------------------------------------------------------------
