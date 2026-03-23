package com.gotocompany.dagger.common.configuration;

import org.apache.flink.api.java.utils.ParameterTool;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ConfigurationTest {

    @Mock
    private ParameterTool parameterTool;

    private Configuration configuration;

    @Before
    public void setup() {
        initMocks(this);
        configuration = new Configuration(parameterTool);
    }

    @Test
    public void shouldGetStringFromParamTool() {
        when(parameterTool.get("test_config", "test_default")).thenReturn("test_value");

        assertEquals("test_value", configuration.getString("test_config", "test_default"));
    }

    @Test
    public void shouldGetNullIfParamIsNotSet() {
        assertNull(configuration.getString("config_not_exist"));
    }

    @Test
    public void shouldGetStringArrayFromParamTool() {
        when(parameterTool.get("config_array_key")).thenReturn("test_value, test_value_2");

        assertArrayEquals(new String[]{"test_value", "test_value_2"}, configuration.getStringArray("config_array_key", new String[]{"default_not_used"}));
    }

    @Test
    public void shouldGetNullStringArrayIfParamIsNotSet() {
        String[] defaultValue = new String[]{"default"};

        assertArrayEquals(defaultValue, configuration.getStringArray("config_not_exist", defaultValue));
    }

    @Test
    public void shouldGetEmptyStringArrayForBlankValue() {
        String[] defaultValue = new String[]{"default"};
        when(parameterTool.get("config_array_key")).thenReturn("   ");

        assertArrayEquals(defaultValue, configuration.getStringArray("config_array_key", defaultValue));
    }

    @Test
    public void shouldGetIntegerFromParamTool() {
        when(parameterTool.getInt("test_config", 1)).thenReturn(2);

        assertEquals(Integer.valueOf(2), configuration.getInteger("test_config", 1));
    }

    @Test
    public void shouldGetBooleanFromParamTool() {
        when(parameterTool.getBoolean("test_config", false)).thenReturn(true);

        assertEquals(true, configuration.getBoolean("test_config", false));
    }

    @Test
    public void shouldGetLongFromParamTool() {
        when(parameterTool.getLong("test_config", 1L)).thenReturn(2L);

        assertEquals(Long.valueOf(2), configuration.getLong("test_config", 1L));
    }
}
