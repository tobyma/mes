package com.qcadoo.mes.core;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.junit.Before;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import com.qcadoo.mes.api.DataDefinitionService;
import com.qcadoo.mes.api.DictionaryService;
import com.qcadoo.mes.beans.sample.SampleParentDatabaseObject;
import com.qcadoo.mes.beans.sample.SampleSimpleDatabaseObject;
import com.qcadoo.mes.internal.DataAccessService;
import com.qcadoo.mes.internal.DataAccessServiceImpl;
import com.qcadoo.mes.internal.EntityService;
import com.qcadoo.mes.internal.PriorityService;
import com.qcadoo.mes.internal.ValidationService;
import com.qcadoo.mes.model.hooks.internal.HookFactory;
import com.qcadoo.mes.model.internal.DataDefinitionImpl;
import com.qcadoo.mes.model.internal.FieldDefinitionImpl;
import com.qcadoo.mes.model.types.FieldTypeFactory;
import com.qcadoo.mes.model.types.HasManyType;
import com.qcadoo.mes.model.types.internal.FieldTypeFactoryImpl;
import com.qcadoo.mes.model.validation.ValidatorFactory;
import com.qcadoo.mes.model.validators.internal.ValidatorFactoryImpl;

public abstract class DataAccessTest {

    protected final DataDefinitionService dataDefinitionService = mock(DataDefinitionService.class);

    protected final SessionFactory sessionFactory = mock(SessionFactory.class);

    protected final Session session = mock(Session.class);

    protected final Criteria criteria = mock(Criteria.class, RETURNS_DEEP_STUBS);

    protected final DictionaryService dictionaryService = mock(DictionaryService.class);

    protected final ApplicationContext applicationContext = mock(ApplicationContext.class);

    protected final DataAccessService dataAccessServiceMock = mock(DataAccessService.class);

    protected FieldTypeFactory fieldTypeFactory = null;

    protected ValidatorFactory fieldValidatorFactory = null;

    protected EntityService entityService = null;

    protected ValidationService validationService = null;

    protected PriorityService priorityService = null;

    protected HookFactory hookFactory;

    protected DataAccessService dataAccessService = null;

    protected DataDefinitionImpl parentDataDefinition = null;

    protected DataDefinitionImpl dataDefinition = null;

    protected FieldDefinitionImpl fieldDefinitionPriority = null;

    protected FieldDefinitionImpl fieldDefinitionBelongsTo = null;

    protected FieldDefinitionImpl fieldDefinitionLazyBelongsTo = null;

    protected FieldDefinitionImpl fieldDefinitionAge = null;

    protected FieldDefinitionImpl fieldDefinitionMoney = null;

    protected FieldDefinitionImpl fieldDefinitionRetired = null;

    protected FieldDefinitionImpl fieldDefinitionBirthDate = null;

    protected FieldDefinitionImpl fieldDefinitionName = null;

    protected FieldDefinitionImpl parentFieldDefinitionName = null;

    protected FieldDefinitionImpl parentFieldDefinitionHasMany = null;

    @Before
    public void superInit() {
        validationService = new ValidationService();
        ReflectionTestUtils.setField(validationService, "sessionFactory", sessionFactory);

        entityService = new EntityService();

        priorityService = new PriorityService();
        ReflectionTestUtils.setField(priorityService, "entityService", entityService);
        ReflectionTestUtils.setField(priorityService, "sessionFactory", sessionFactory);

        dataAccessService = new DataAccessServiceImpl();
        ReflectionTestUtils.setField(dataAccessService, "entityService", entityService);
        ReflectionTestUtils.setField(dataAccessService, "sessionFactory", sessionFactory);
        ReflectionTestUtils.setField(dataAccessService, "priorityService", priorityService);
        ReflectionTestUtils.setField(dataAccessService, "validationService", validationService);

        fieldTypeFactory = new FieldTypeFactoryImpl();
        ReflectionTestUtils.setField(fieldTypeFactory, "dictionaryService", dictionaryService);
        ReflectionTestUtils.setField(fieldTypeFactory, "dataDefinitionService", dataDefinitionService);

        hookFactory = new HookFactory();
        ReflectionTestUtils.setField(hookFactory, "applicationContext", applicationContext);

        fieldValidatorFactory = new ValidatorFactoryImpl();

        parentDataDefinition = new DataDefinitionImpl("parent", "parent.entity", dataAccessService);
        given(dataDefinitionService.get("parent", "entity")).willReturn(parentDataDefinition);

        dataDefinition = new DataDefinitionImpl("simple", "simple.entity", dataAccessService);
        given(dataDefinitionService.get("simple", "entity")).willReturn(dataDefinition);

        parentFieldDefinitionName = new FieldDefinitionImpl("name");
        parentFieldDefinitionName.withType(fieldTypeFactory.stringType());

        parentFieldDefinitionHasMany = new FieldDefinitionImpl("entities");
        parentFieldDefinitionHasMany.withType(fieldTypeFactory.hasManyType("simple", "entity", "belongsTo",
                HasManyType.Cascade.DELETE));

        parentDataDefinition.withField(parentFieldDefinitionName);
        parentDataDefinition.withField(parentFieldDefinitionHasMany);
        parentDataDefinition.setFullyQualifiedClassName(SampleParentDatabaseObject.class.getCanonicalName());

        fieldDefinitionBelongsTo = new FieldDefinitionImpl("belongsTo");
        fieldDefinitionBelongsTo.withType(fieldTypeFactory.eagerBelongsToType("parent", "entity", "name"));

        fieldDefinitionLazyBelongsTo = new FieldDefinitionImpl("lazyBelongsTo");
        fieldDefinitionLazyBelongsTo.withType(fieldTypeFactory.lazyBelongsToType("parent", "entity", "name"));

        fieldDefinitionName = new FieldDefinitionImpl("name");
        fieldDefinitionName.withType(fieldTypeFactory.stringType());

        fieldDefinitionAge = new FieldDefinitionImpl("age");
        fieldDefinitionAge.withType(fieldTypeFactory.integerType());

        fieldDefinitionPriority = new FieldDefinitionImpl("priority");
        fieldDefinitionPriority.withType(fieldTypeFactory.priorityType(fieldDefinitionBelongsTo));
        fieldDefinitionPriority.withReadOnly(true);

        fieldDefinitionMoney = new FieldDefinitionImpl("money");
        fieldDefinitionMoney.withType(fieldTypeFactory.decimalType());

        fieldDefinitionRetired = new FieldDefinitionImpl("retired");
        fieldDefinitionRetired.withType(fieldTypeFactory.booleanType());

        fieldDefinitionBirthDate = new FieldDefinitionImpl("birthDate");
        fieldDefinitionBirthDate.withType(fieldTypeFactory.dateType());

        dataDefinition.withField(fieldDefinitionName);
        dataDefinition.withField(fieldDefinitionAge);
        dataDefinition.withField(fieldDefinitionMoney);
        dataDefinition.withField(fieldDefinitionRetired);
        dataDefinition.withField(fieldDefinitionBirthDate);
        dataDefinition.withField(fieldDefinitionBelongsTo);
        dataDefinition.withField(fieldDefinitionLazyBelongsTo);
        dataDefinition.setFullyQualifiedClassName(SampleSimpleDatabaseObject.class.getCanonicalName());

        given(sessionFactory.getCurrentSession()).willReturn(session);

        given(session.createCriteria(any(Class.class))).willReturn(criteria);

        given(criteria.add(any(Criterion.class))).willReturn(criteria);
        given(criteria.setProjection(any(Projection.class))).willReturn(criteria);
        given(criteria.setFirstResult(anyInt())).willReturn(criteria);
        given(criteria.setMaxResults(anyInt())).willReturn(criteria);
        given(criteria.addOrder(any(Order.class))).willReturn(criteria);
    }

}
