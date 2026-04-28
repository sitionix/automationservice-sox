package com.sitionix.atmssox.application.usecase;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sitionix.atmssox.domain.model.capability.CapabilityDefinition;
import com.sitionix.atmssox.domain.model.capability.CapabilityInputSchemaBuilder;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapabilityToolPayloadCodecTest {

    private CapabilityToolPayloadCodec capabilityToolPayloadCodec;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.capabilityToolPayloadCodec = new CapabilityToolPayloadCodec(this.objectMapper);
    }

    @Test
    void givenInvalidArgsJson_whenParseArgs_thenReturnEmptyObjectNode() throws Exception {
        //given
        when(this.objectMapper.readTree("invalid")).thenThrow(new JsonProcessingException("bad json") { });
        when(this.objectMapper.createObjectNode()).thenReturn(new ObjectMapper().createObjectNode());

        //when
        final JsonNode actual = this.capabilityToolPayloadCodec.parseArgs("invalid");

        //then
        assertThat(actual.isObject()).isTrue();
        assertThat(actual.size()).isZero();
    }

    @Test
    void givenSerializationFailure_whenSerializeError_thenReturnFallbackJson() throws Exception {
        //given
        when(this.objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("fail") { });

        //when
        final String discovery = this.capabilityToolPayloadCodec.serializeDiscoveredCapabilities(List.of(this.getCapability()));
        final String jsonNode = this.capabilityToolPayloadCodec.serializeJsonNode(new ObjectMapper().createObjectNode());
        final String error = this.capabilityToolPayloadCodec.serializeError("x");

        //then
        assertThat(discovery).isEqualTo("{\"capabilities\":[]}");
        assertThat(jsonNode).isEqualTo("{}");
        assertThat(error).isEqualTo("{\"error\":\"internal_error\"}");
    }

    private CapabilityDefinition getCapability() {
        return new CapabilityDefinition(
                "GET_WORKSPACE_SITES",
                "desc",
                List.of("tag"),
                CapabilityInputSchemaBuilder.objectSchema().build(),
                "out"
        );
    }
}
