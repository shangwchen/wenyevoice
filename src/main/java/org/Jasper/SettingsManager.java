package org.Jasper;

import java.io.*;
import java.util.Properties;

class SettingsManager {
    private Properties properties = new Properties();
    private final String settingsFile = "settings.properties";

    public SettingsManager() {
        loadSettings();
    }

    public void loadSettings() {
        try (InputStream input = new FileInputStream(settingsFile)) {
            properties.load(input);
        } catch (IOException e) {
            System.out.println("设置文件不存在，将创建一个新文件。");
        }
    }

    public void saveSettings() {
        try (OutputStream output = new FileOutputStream(settingsFile)) {
            properties.store(output, "应用设置");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getSetting(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }


    public void setSetting(String key, String value) {
        properties.setProperty(key, value);
    }
}