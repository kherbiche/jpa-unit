package eu.drus.test.persistence;

import static eu.drus.test.persistence.core.metadata.TestCodeUtils.buildModel;
import static eu.drus.test.persistence.core.metadata.TestCodeUtils.compileModel;
import static eu.drus.test.persistence.core.metadata.TestCodeUtils.loadClass;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.PersistenceUnit;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.mockito.ArgumentCaptor;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;

public class JpaUnitRunnerTest {

    @ClassRule
    public static TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void testClassWithoutPersistenceContextField() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(RunWith.class);
        jAnnotationUse.param("value", JpaUnitRunner.class);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "testMethod");
        jMethod.annotate(Test.class);

        buildModel(testFolder.getRoot(), jCodeModel);
        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());

        try {
            // WHEN
            new JpaUnitRunner(cut);
            fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException e) {

            // THEN
            assertThat(e.getMessage(), containsString("EntityManagerFactory or EntityManager field annotated"));
        }
    }

    @Test
    public void testClassWithMultiplePersistenceContextFields() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(RunWith.class);
        jAnnotationUse.param("value", JpaUnitRunner.class);
        final JFieldVar em1Field = jClass.field(JMod.PRIVATE, EntityManager.class, "em1");
        em1Field.annotate(PersistenceContext.class);
        final JFieldVar em2Field = jClass.field(JMod.PRIVATE, EntityManager.class, "em2");
        em2Field.annotate(PersistenceContext.class);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "testMethod");
        jMethod.annotate(Test.class);

        buildModel(testFolder.getRoot(), jCodeModel);
        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());

        try {
            // WHEN
            new JpaUnitRunner(cut);
            fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException e) {

            // THEN
            assertThat(e.getMessage(), containsString("Only single field is allowed"));
        }
    }

    @Test
    public void testClassWithMultiplePersistenceUnitFields() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(RunWith.class);
        jAnnotationUse.param("value", JpaUnitRunner.class);
        final JFieldVar emf1Field = jClass.field(JMod.PRIVATE, EntityManagerFactory.class, "emf1");
        emf1Field.annotate(PersistenceUnit.class);
        final JFieldVar emf2Field = jClass.field(JMod.PRIVATE, EntityManagerFactory.class, "emf2");
        emf2Field.annotate(PersistenceUnit.class);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "testMethod");
        jMethod.annotate(Test.class);

        buildModel(testFolder.getRoot(), jCodeModel);
        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());

        try {
            // WHEN
            new JpaUnitRunner(cut);
            fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException e) {

            // THEN
            assertThat(e.getMessage(), containsString("Only single field is allowed"));
        }
    }

    @Test
    public void testClassWithPersistenceContextAndPersistenceUnitFields() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(RunWith.class);
        jAnnotationUse.param("value", JpaUnitRunner.class);
        final JFieldVar emf1Field = jClass.field(JMod.PRIVATE, EntityManager.class, "em");
        emf1Field.annotate(PersistenceContext.class);
        final JFieldVar emf2Field = jClass.field(JMod.PRIVATE, EntityManagerFactory.class, "emf");
        emf2Field.annotate(PersistenceUnit.class);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "testMethod");
        jMethod.annotate(Test.class);

        buildModel(testFolder.getRoot(), jCodeModel);
        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());

        try {
            // WHEN
            new JpaUnitRunner(cut);
            fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException e) {

            // THEN
            assertThat(e.getMessage(), containsString("either @PersistenceUnit or @PersistenceContext"));
        }
    }

    @Test
    public void testClassWithPersistenceContextFieldOfWrongType() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(RunWith.class);
        jAnnotationUse.param("value", JpaUnitRunner.class);
        final JFieldVar emField = jClass.field(JMod.PRIVATE, EntityManagerFactory.class, "em");
        emField.annotate(PersistenceContext.class);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "testMethod");
        jMethod.annotate(Test.class);

        buildModel(testFolder.getRoot(), jCodeModel);
        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());

        try {
            // WHEN
            new JpaUnitRunner(cut);
            fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException e) {

            // THEN
            assertThat(e.getMessage(), containsString("annotated with @PersistenceContext is not of type EntityManager"));
        }
    }

    @Test
    public void testClassWithPersistenceUnitFieldOfWrongType() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(RunWith.class);
        jAnnotationUse.param("value", JpaUnitRunner.class);
        final JFieldVar emField = jClass.field(JMod.PRIVATE, EntityManager.class, "emf");
        emField.annotate(PersistenceUnit.class);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "testMethod");
        jMethod.annotate(Test.class);

        buildModel(testFolder.getRoot(), jCodeModel);
        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());

        try {
            // WHEN
            new JpaUnitRunner(cut);
            fail("IllegalArgumentException expected");
        } catch (final IllegalArgumentException e) {

            // THEN
            assertThat(e.getMessage(), containsString("annotated with @PersistenceUnit is not of type EntityManagerFactory"));
        }
    }

    @Test
    public void testClassWithPersistenceContextWithoutUnitNameSpecified() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(RunWith.class);
        jAnnotationUse.param("value", JpaUnitRunner.class);
        final JFieldVar emField = jClass.field(JMod.PRIVATE, EntityManager.class, "em");
        emField.annotate(PersistenceContext.class);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "testMethod");
        jMethod.annotate(Test.class);

        buildModel(testFolder.getRoot(), jCodeModel);
        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());

        try {
            // WHEN
            new JpaUnitRunner(cut);
            fail("PersistenceException expected");
        } catch (final PersistenceException e) {

            // THEN
            assertThat(e.getMessage(), containsString("No Persistence"));
        }
    }

    @Test
    public void testClassWithPersistenceUnitWithoutUnitNameSpecified() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(RunWith.class);
        jAnnotationUse.param("value", JpaUnitRunner.class);
        final JFieldVar emField = jClass.field(JMod.PRIVATE, EntityManagerFactory.class, "emf");
        emField.annotate(PersistenceUnit.class);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "testMethod");
        jMethod.annotate(Test.class);

        buildModel(testFolder.getRoot(), jCodeModel);
        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());

        try {
            // WHEN
            new JpaUnitRunner(cut);
            fail("PersistenceException expected");
        } catch (final PersistenceException e) {

            // THEN
            assertThat(e.getMessage(), containsString("No Persistence"));
        }
    }

    @Test
    public void testClassWithPersistenceContextWithUnknownUnitNameSpecified() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(RunWith.class);
        jAnnotationUse.param("value", JpaUnitRunner.class);
        final JFieldVar emField = jClass.field(JMod.PRIVATE, EntityManager.class, "em");
        final JAnnotationUse jAnnotation = emField.annotate(PersistenceContext.class);
        jAnnotation.param("unitName", "foo");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "testMethod");
        jMethod.annotate(Test.class);

        buildModel(testFolder.getRoot(), jCodeModel);
        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());

        try {
            // WHEN
            new JpaUnitRunner(cut);
            fail("PersistenceException expected");
        } catch (final PersistenceException e) {

            // THEN
            assertThat(e.getMessage(), containsString("No Persistence"));
        }
    }

    @Test
    public void testClassWithPersistenceUnitWithUnknownUnitNameSpecified() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(RunWith.class);
        jAnnotationUse.param("value", JpaUnitRunner.class);
        final JFieldVar emField = jClass.field(JMod.PRIVATE, EntityManagerFactory.class, "emf");
        final JAnnotationUse jAnnotation = emField.annotate(PersistenceUnit.class);
        jAnnotation.param("unitName", "foo");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "testMethod");
        jMethod.annotate(Test.class);

        buildModel(testFolder.getRoot(), jCodeModel);
        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());

        try {
            // WHEN
            new JpaUnitRunner(cut);
            fail("PersistenceException expected");
        } catch (final PersistenceException e) {

            // THEN
            assertThat(e.getMessage(), containsString("No Persistence"));
        }
    }

    @Test
    public void testClassWithPersistenceContextWithKonfiguredUnitNameSpecified() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(RunWith.class);
        jAnnotationUse.param("value", JpaUnitRunner.class);
        final JFieldVar emField = jClass.field(JMod.PRIVATE, EntityManager.class, "em");
        final JAnnotationUse jAnnotation = emField.annotate(PersistenceContext.class);
        jAnnotation.param("unitName", "test-unit-1");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "testMethod");
        jMethod.annotate(Test.class);

        buildModel(testFolder.getRoot(), jCodeModel);
        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final JpaUnitRunner runner = new JpaUnitRunner(cut);

        final RunListener listener = mock(RunListener.class);
        final RunNotifier notifier = new RunNotifier();
        notifier.addListener(listener);

        // WHEN
        runner.run(notifier);

        // THEN
        final ArgumentCaptor<Description> descriptionCaptor = ArgumentCaptor.forClass(Description.class);
        verify(listener).testStarted(descriptionCaptor.capture());
        assertThat(descriptionCaptor.getValue().getClassName(), equalTo("ClassUnderTest"));
        assertThat(descriptionCaptor.getValue().getMethodName(), equalTo("testMethod"));

        verify(listener).testFinished(descriptionCaptor.capture());
        assertThat(descriptionCaptor.getValue().getClassName(), equalTo("ClassUnderTest"));
        assertThat(descriptionCaptor.getValue().getMethodName(), equalTo("testMethod"));

        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testClassWithPersistenceUnitWithKonfiguredUnitNameSpecified() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(RunWith.class);
        jAnnotationUse.param("value", JpaUnitRunner.class);
        final JFieldVar emField = jClass.field(JMod.PRIVATE, EntityManagerFactory.class, "emf");
        final JAnnotationUse jAnnotation = emField.annotate(PersistenceUnit.class);
        jAnnotation.param("unitName", "test-unit-1");
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "testMethod");
        jMethod.annotate(Test.class);

        buildModel(testFolder.getRoot(), jCodeModel);
        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());
        final JpaUnitRunner runner = new JpaUnitRunner(cut);

        final RunListener listener = mock(RunListener.class);
        final RunNotifier notifier = new RunNotifier();
        notifier.addListener(listener);

        // WHEN
        runner.run(notifier);

        // THEN
        final ArgumentCaptor<Description> descriptionCaptor = ArgumentCaptor.forClass(Description.class);
        verify(listener).testStarted(descriptionCaptor.capture());
        assertThat(descriptionCaptor.getValue().getClassName(), equalTo("ClassUnderTest"));
        assertThat(descriptionCaptor.getValue().getMethodName(), equalTo("testMethod"));

        verify(listener).testFinished(descriptionCaptor.capture());
        assertThat(descriptionCaptor.getValue().getClassName(), equalTo("ClassUnderTest"));
        assertThat(descriptionCaptor.getValue().getMethodName(), equalTo("testMethod"));

        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testJpaUnitRunnerAndJpaUnitRuleFieldExcludeEachOther() throws Exception {
        // GIVEN
        final JCodeModel jCodeModel = new JCodeModel();
        final JPackage jp = jCodeModel.rootPackage();
        final JDefinedClass jClass = jp._class(JMod.PUBLIC, "ClassUnderTest");
        final JAnnotationUse jAnnotationUse = jClass.annotate(RunWith.class);
        jAnnotationUse.param("value", JpaUnitRunner.class);
        final JFieldVar emField = jClass.field(JMod.PRIVATE, EntityManagerFactory.class, "emf");
        final JAnnotationUse jAnnotation = emField.annotate(PersistenceUnit.class);
        jAnnotation.param("unitName", "test-unit-1");
        final JFieldVar ruleField = jClass.field(JMod.PRIVATE, JpaUnitRule.class, "rule");
        ruleField.annotate(Rule.class);
        final JMethod jMethod = jClass.method(JMod.PUBLIC, jCodeModel.VOID, "testMethod");
        jMethod.annotate(Test.class);

        buildModel(testFolder.getRoot(), jCodeModel);
        compileModel(testFolder.getRoot());

        final Class<?> cut = loadClass(testFolder.getRoot(), jClass.name());

        try {
            new JpaUnitRunner(cut);
            fail("InitializationError expected");
        } catch (final InitializationError e) {
            // expected
        }

    }
}
