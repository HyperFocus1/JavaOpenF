package com.open_f.fu.shared.persistence;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

import jakarta.persistence.EntityManager;

/**
 * EntityManager único para inyectar en los repositorios. Cada llamada se delega al
 * EntityManager de la transacción abierta por {@link JpaTransactionManager}, así un
 * repositorio Singleton puede guardar {@code this.em} aunque cada transacción tenga el suyo.
 */
public final class TransactionalEntityManager {

    private TransactionalEntityManager() {
    }

    public static EntityManager create() {
        return (EntityManager) Proxy.newProxyInstance(
                TransactionalEntityManager.class.getClassLoader(),
                new Class<?>[] { EntityManager.class },
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "toString": return "TransactionalEntityManager";
                        case "hashCode": return System.identityHashCode(proxy);
                        case "equals": return proxy == args[0];
                        case "close", "getTransaction":
                            throw new IllegalStateException("La transacción la maneja TransactionManager");
                        default:
                            break;
                    }
                    try {
                        return method.invoke(JpaTransactionManager.currentEntityManager(), args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }
}
