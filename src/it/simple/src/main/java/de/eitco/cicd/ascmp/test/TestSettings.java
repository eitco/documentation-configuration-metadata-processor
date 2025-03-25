package de.eitco.cicd.ascmp.test;

import de.eitco.cicd.ascmp.AdditionalConfigurationMetadata;

@AdditionalConfigurationMetadata(group = "TestGroup")
public class TestSettings {

    /**
     * This is my first setting.
     */
    private String myFirstSetting = "myFirstSetting";

    public String getMyFirstSetting() {
        return myFirstSetting;
    }

    public void setMyFirstSetting(String myFirstSetting) {
        this.myFirstSetting = myFirstSetting;
    }
}