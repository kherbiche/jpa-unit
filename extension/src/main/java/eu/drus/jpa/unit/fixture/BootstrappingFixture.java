package eu.drus.jpa.unit.fixture;

import static eu.drus.jpa.unit.util.Preconditions.checkArgument;

import java.lang.reflect.Method;
import java.util.List;

import javax.sql.DataSource;

import org.junit.runners.model.TestClass;

import eu.drus.jpa.unit.core.metadata.MetadataExtractor;
import eu.drus.jpa.unit.rule.ExecutionContext;
import eu.drus.jpa.unit.rule.GlobalTestFixture;

public class BootstrappingFixture implements GlobalTestFixture {

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void beforeAll(final ExecutionContext ctx, final Object target) throws Throwable {
        final MetadataExtractor extractor = new MetadataExtractor(new TestClass(target.getClass()));
        final List<Method> bootstrappingMethods = extractor.bootstrapping().getAnnotatedMethods();
        checkArgument(bootstrappingMethods.size() <= 1, "Only single method is allowed to be annotated with @Bootstrapping");

        if (!bootstrappingMethods.isEmpty()) {
            final Method tmp = bootstrappingMethods.get(0);
            final Class<?>[] parameterTypes = tmp.getParameterTypes();
            checkArgument(parameterTypes.length == 1, "A bootstrapping method is required to have a single parameter of type DataSource");
            checkArgument(parameterTypes[0].equals(DataSource.class),
                    "A bootstrapping method is required to have a single parameter of type DataSource");
            tmp.invoke(target, ctx.getDataSource());
        }
    }

    @Override
    public void afterAll(final ExecutionContext ctx, final Object target) throws Throwable {
        // nothing to do here
    }

}