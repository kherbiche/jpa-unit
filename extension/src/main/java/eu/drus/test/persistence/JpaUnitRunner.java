package eu.drus.test.persistence;

import java.util.List;

import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.InitializationError;

import eu.drus.test.persistence.core.metadata.FeatureResolverFactory;
import eu.drus.test.persistence.rule.context.PersistenceContextRule;
import eu.drus.test.persistence.rule.evaluation.EvaluationRule;
import eu.drus.test.persistence.rule.transaction.TransactionalRule;

public class JpaUnitRunner extends BlockJUnit4ClassRunner {

    private JpaUnitContext ctx;

    public JpaUnitRunner(final Class<?> klass) throws InitializationError {
        super(klass);

        ctx = JpaUnitContext.getInstance(getTestClass());

        final List<FrameworkField> ruleFields = getTestClass().getAnnotatedFields(Rule.class);
        if (ruleFields.stream().anyMatch(f -> f.getType().equals(JpaUnitRule.class))) {
            throw new InitializationError("JpaUnitRunner and JpaUnitRule exclude each other");
        }
    }

    @Override
    protected List<MethodRule> rules(final Object target) {
        final FeatureResolverFactory featureResolverFactory = new FeatureResolverFactory();

        final List<MethodRule> rules = super.rules(target);
        rules.add(new TransactionalRule(featureResolverFactory, ctx.getPersistenceField()));
        rules.add(new EvaluationRule(featureResolverFactory, ctx.getProperties()));
        rules.add(new PersistenceContextRule(ctx, ctx.getPersistenceField()));
        return rules;
    }
}
