package com.example.textprocessingsystem.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class CustomLogger {
    public static Logger createLogger(String name) {
        Logger logger = Logger.getLogger(name);
        logger.setUseParentHandlers(false); // Disable default console handler

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                String color;
                if (record.getLevel() == Level.INFO) {
                    color = "\u001B[32m"; // Green
                } else if (record.getLevel() == Level.WARNING) {
                    color = "\u001B[33m"; // Yellow
                } else if (record.getLevel() == Level.SEVERE) {
                    color = "\u001B[31m"; // Red
                } else {
                    color = "\u001B[0m"; // Default
                }
                return color + record.getLevel() + ": " + record.getMessage() + "\u001B[0m\n";
            }
        });

        logger.addHandler(consoleHandler);
        return logger;
    }
}