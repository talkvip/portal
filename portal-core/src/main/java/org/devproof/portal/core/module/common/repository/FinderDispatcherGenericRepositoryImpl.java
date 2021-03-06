/*
 * Copyright 2009-2011 Carsten Hufe devproof.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.devproof.portal.core.module.common.repository;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.devproof.portal.core.module.common.annotation.BulkUpdate;
import org.devproof.portal.core.module.common.annotation.DelegateRepositoryMethod;
import org.devproof.portal.core.module.common.annotation.Query;
import org.devproof.portal.core.module.user.service.UsernameResolver;
import org.hibernate.SessionFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The executing class for the generic dao:
 * <p/>
 * 1. executes the generic dao methods 2. executes generic queries 3. executes
 * generic bulk updates 4. executes own implemented dao methods (from
 * servicesImpl) if no Query or BulkUpdate annotation exist 5. opens transaction
 * on the first change call (save, update, merge, delete, bulkupdate) 6. opens a
 * session if no ones exists and closes it after the call
 *
 * @param <T>  entity type
 * @param <PK> primary key type
 * @author Carsten Hufe
 */
public class FinderDispatcherGenericRepositoryImpl<T, PK extends Serializable> implements FactoryBean<Object>, Serializable, ApplicationContextAware {

    private static final long serialVersionUID = -3752572093862325307L;

    private Object servicesImpl;
    private Class<T> entityClass;
    private Class<?> daoInterface;
    private UsernameResolver usernameResolver;
    private ApplicationContext applicationContext;
    private SessionFactory sessionFactory;

    public Object getObject() throws Exception {
        ProxyFactory result = new ProxyFactory();
        CrudRepository<T, PK> crudRepository = createGenericHibernateDao();
        result.setTarget(crudRepository);
        result.setInterfaces(new Class[]{daoInterface});
        result.addAdvice(createGenericDaoInterceptor());
        return result.getProxy();
    }

    protected CrudRepository<T, PK> createGenericHibernateDao() {
        GenericHibernateRepositoryImpl<T, PK> genericRepository = new GenericHibernateRepositoryImpl<T, PK>(entityClass);
        genericRepository.setSessionFactory(sessionFactory);
        genericRepository.setUsernameResolver(usernameResolver);
        return genericRepository;
    }

    private MethodInterceptor createGenericDaoInterceptor() {
        return new MethodInterceptor() {
            public Object invoke(MethodInvocation invocation) throws Throwable {
                return evaluateMethodInvocation(invocation);

            }

            private Object evaluateMethodInvocation(MethodInvocation invocation) throws Throwable {
                Object result = null;
                Method method = invocation.getMethod();
                if (method.isAnnotationPresent(Query.class)) {
                    result = executeQuery(invocation);
                } else if (method.isAnnotationPresent(BulkUpdate.class)) {
                    executeBulkUpdate(invocation);
                } else if (method.isAnnotationPresent(DelegateRepositoryMethod.class)) {
                    result = delegateRepositoryMethod(invocation, method);
                } else {
                    result = delegateToServiceMethod(invocation);
                }
                return result;
            }

            private Object delegateToServiceMethod(MethodInvocation invocation) throws Throwable {
                Method serviceMethod = FinderDispatcherGenericRepositoryImpl.this.servicesImpl != null ? FinderDispatcherGenericRepositoryImpl.this.servicesImpl.getClass().getMethod(invocation.getMethod().getName(), invocation.getMethod().getParameterTypes()) : null;
                if (serviceMethod != null) {
                    return serviceMethod.invoke(FinderDispatcherGenericRepositoryImpl.this.servicesImpl, invocation.getArguments());
                } else {
                    // should be only save, update, delete from the generic
                    // dao
                    return invocation.proceed();
                }
            }

            private void executeBulkUpdate(MethodInvocation invocation) {
//				openTransaction();
                Method method = invocation.getMethod();
                BulkUpdate bulkUpdate = method.getAnnotation(BulkUpdate.class);
                FinderExecutor target = (FinderExecutor) invocation.getThis();
                target.executeUpdate(bulkUpdate.value(), invocation.getArguments());
            }

            private Object executeQuery(MethodInvocation invocation) {
                Method method = invocation.getMethod();
                Query query = method.getAnnotation(Query.class);
                FinderExecutor target = (FinderExecutor) invocation.getThis();
                if (query.limitClause()) {
                    Object orginal[] = invocation.getArguments();
                    int len = orginal.length - 2;
                    Object copy[] = new Object[len];
                    for (int i = 0; i < len; i++) {
                        copy[i] = orginal[i];
                    }
                    return target.executeFinder(query.value(), copy, method, (Integer) orginal[len], (Integer) orginal[len + 1]);
                } else {
                    return target.executeFinder(query.value(), invocation.getArguments(), method, null, null);
                }
            }
        };
    }

    private Object delegateRepositoryMethod(MethodInvocation invocation, Method method) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        DelegateRepositoryMethod annotation = method.getAnnotation(DelegateRepositoryMethod.class);
        Object bean = applicationContext.getBean(annotation.value());
        Method delegateMethod = bean.getClass().getMethod(method.getName(), method.getParameterTypes());
        return delegateMethod.invoke(bean, invocation.getArguments());
    }

    public Class<?> getObjectType() {
        return daoInterface;
    }

    public boolean isSingleton() {
        return true;
    }

    public Object getServicesImpl() {
        return servicesImpl;
    }

    public void setServicesImpl(Object servicesImpl) {
        this.servicesImpl = servicesImpl;
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    @Required
    public void setEntityClass(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public Class<?> getDaoInterface() {
        return daoInterface;
    }

    @Required
    public void setDaoInterface(Class<?> daoInterface) {
        this.daoInterface = daoInterface;
    }

    @Required
    public void setUsernameResolver(UsernameResolver usernameResolver) {
        this.usernameResolver = usernameResolver;
    }

    @Required
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
