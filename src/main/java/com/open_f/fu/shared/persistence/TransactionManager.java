package com.open_f.fu.shared.persistence;

import java.util.function.Supplier;

/**
 * Los servicios abren transacciones sin conocer JPA.
 */
public interface TransactionManager {

    <T> T inTransaction(Supplier<T> work);
}
