package com.open_f.fu.shared.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

// Registrado en web.xml: abre y cierra la persistencia con la app.
public class PersistenceListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(PersistenceListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("Inicializando PersistenceManager");
        PersistenceManager.init();
        log.info("EntityManagerFactory inicializada");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Cerrando PersistenceManager");
        PersistenceManager.close();
    }
}
