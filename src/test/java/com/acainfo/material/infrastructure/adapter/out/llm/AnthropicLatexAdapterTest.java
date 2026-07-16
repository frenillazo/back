package com.acainfo.material.infrastructure.adapter.out.llm;

import com.anthropic.models.messages.StopReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Guard contra respuestas truncadas por max_tokens: un .tex cortado no compila
 * y el bucle de reintentos podría "arreglarlo" en un documento compilable pero
 * INCOMPLETO, publicado sin aviso. El guard convierte ese caso en job FAILED
 * con mensaje claro.
 */
class AnthropicLatexAdapterTest {

    @Test
    @DisplayName("stop_reason max_tokens lanza excepción con mensaje en español y el límite")
    void maxTokensLanzaExcepcionConMensajeClaro() {
        assertThatThrownBy(() ->
                AnthropicLatexAdapter.ensureNotTruncated(Optional.of(StopReason.MAX_TOKENS), 16000))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("16000")
                .hasMessageContaining("demasiado largo");
    }

    @Test
    @DisplayName("stop_reason end_turn (respuesta completa) no lanza nada")
    void endTurnNoLanza() {
        assertThatCode(() ->
                AnthropicLatexAdapter.ensureNotTruncated(Optional.of(StopReason.END_TURN), 16000))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sin stop_reason (Optional vacío) no lanza nada")
    void sinStopReasonNoLanza() {
        assertThatCode(() ->
                AnthropicLatexAdapter.ensureNotTruncated(Optional.empty(), 16000))
                .doesNotThrowAnyException();
    }
}
