package de.eitco.cicd.ascmp.test;

import de.eitco.cicd.ascmp.AdditionalConfigurationMetadata;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@AdditionalConfigurationMetadata(groups = {"TestGroup1","TestGroup2"})
public class TestSettings {

    /**
     * This is my first setting.
     */
    private String myFirstSetting = "myFirstSetting";

    @NestedConfigurationProperty
    private TestNested nested = new TestNested();

    public String getMyFirstSetting() {
        return myFirstSetting;
    }

    public void setMyFirstSetting(String myFirstSetting) {
        this.myFirstSetting = myFirstSetting;
    }

    public TestNested getNested() {
        return nested;
    }

    public void setNested(TestNested nested) {
        this.nested = nested;
    }
}