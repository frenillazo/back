package com.acainfo.material.infrastructure.adapter.out.llm;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Base64ImageSource;
import com.anthropic.models.messages.Base64PdfSource;
import com.anthropic.models.messages.ContentBlockParam;
import com.anthropic.models.messages.DocumentBlockParam;
import com.anthropic.models.messages.ImageBlockParam;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StopReason;
import com.anthropic.models.messages.TextBlockParam;
import com.anthropic.models.messages.ThinkingConfigAdaptive;
import com.acainfo.material.application.dto.AiImageInput;
import com.acainfo.material.application.port.out.LlmLatexPort;
import com.acainfo.shared.infrastructure.config.AnthropicProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * LlmLatexPort adapter over the official Anthropic Java SDK.
 *
 * <p>System prompts are FIXED per mode (cacheable) and built by COMPOSITION
 * (shared core + mode block + empty subject-context slot): evolving to
 * per-(subject, functionality) templates later is additive, not a rewrite.</p>
 */
@Slf4j
@Component
public class AnthropicLatexAdapter implements LlmLatexPort {

    /**
     * Shared core: compilable LaTeX only, XeTeX/tectonic (NO inputenc/fontenc),
     * limited preamble, no shell-escape, Spanish.
     */
    static final String PROMPT_BASE = """
            Eres el asistente de generación de material docente de una academia universitaria \
            (ingenierías: matemáticas, física, informática, electrónica).
            Devuelve EXCLUSIVAMENTE el código fuente de un documento LaTeX completo y compilable, \
            empezando por \\documentclass y terminando en \\end{document}. Sin explicación previa \
            ni posterior, sin bloques de markdown, sin ```.
            El documento se compila con tectonic (motor XeTeX): NO uses los paquetes inputenc ni \
            fontenc (innecesarios en XeTeX y degradan la fuente). Prohibido \\write18, shell-escape \
            y cualquier paquete que acceda a red o disco.
            Preámbulo limitado: usa únicamente los paquetes amsmath, amssymb, amsthm, geometry, \
            graphicx y enumitem (y solo los que necesites). No uses tikz. No uses \\includegraphics: \
            el documento debe ser autocontenido, sin ficheros externos.
            Idioma: español, con tildes y puntuación correctas.
            """;

    /** Hook para plantillas por (asignatura, funcionalidad) en el futuro; hoy vacío. */
    static final String PROMPT_SUBJECT_CONTEXT = "";

    static final String PROMPT_GENERATE = """
            Tarea: en las imágenes adjuntas hay enunciados de ejercicios trabajados hoy en clase. \
            Crea %d ejercicio(s) NUEVO(s) de repaso del mismo tipo y dificultad similar (no copies \
            los originales), cada uno con su solución completa razonada paso a paso; acompaña los \
            cálculos con un comentario breve del procedimiento ("multiplicando por el factor \
            integrante...", "deshaciendo el cambio...").
            Estructura del documento: \\title{<tema corto, máximo 6 palabras>} y \\maketitle al \
            principio; después, cada ejercicio numerado con su enunciado y a continuación su solución.
            El \\title es OBLIGATORIO y debe describir el tema (p. ej. \\title{Ecuaciones diferenciales exactas}).
            """;

    static final String PROMPT_TRANSCRIBE = """
            Tarea: el PDF adjunto son apuntes manuscritos de pizarra de una clase. Transcríbelos \
            a limpio con tipografía LaTeX, de forma FIEL: no corrijas ni alteres el contenido \
            matemático aunque creas que hay un error (los alumnos contrastan con el original). \
            Si un símbolo o palabra resulta ilegible, escribe \\textbf{[?]} en su lugar. \
            Conserva el orden y la estructura del original (títulos, apartados, ejercicios, ejemplos).
            """;

    static final String PROMPT_FIX = """
            Tarea: el siguiente documento LaTeX NO compila con tectonic (XeTeX). Te paso el fuente \
            completo y las líneas de error del compilador. Corrígelo cambiando lo MÍNIMO \
            imprescindible y sin alterar el contenido. Devuelve el documento corregido COMPLETO.
            """;

    private final AnthropicProperties properties;
    private volatile AnthropicClient client;

    public AnthropicLatexAdapter(AnthropicProperties properties) {
        this.properties = properties;
    }

    @Override
    public String generateExercises(List<AiImageInput> images, int exerciseCount) {
        List<ContentBlockParam> blocks = new ArrayList<>();
        for (AiImageInput image : images) {
            blocks.add(ContentBlockParam.ofImage(ImageBlockParam.builder()
                    .source(Base64ImageSource.builder()
                            .mediaType(Base64ImageSource.MediaType.of(image.mimeType()))
                            .data(Base64.getEncoder().encodeToString(image.data()))
                            .build())
                    .build()));
        }
        blocks.add(text("Genera los ejercicios de repaso a partir de estas capturas."));

        String system = PROMPT_BASE + PROMPT_SUBJECT_CONTEXT + PROMPT_GENERATE.formatted(exerciseCount);
        return call(system, blocks);
    }

    @Override
    public String transcribeDocument(byte[] pdfBytes) {
        List<ContentBlockParam> blocks = List.of(
                ContentBlockParam.ofDocument(DocumentBlockParam.builder()
                        .source(Base64PdfSource.builder()
                                .data(Base64.getEncoder().encodeToString(pdfBytes))
                                .build())
                        .build()),
                text("Transcribe este documento a limpio."));

        String system = PROMPT_BASE + PROMPT_SUBJECT_CONTEXT + PROMPT_TRANSCRIBE;
        return call(system, blocks);
    }

    @Override
    public String fixLatex(String texSource, String compilationErrors) {
        String userMessage = """
                ERRORES DEL COMPILADOR:
                %s

                DOCUMENTO:
                %s
                """.formatted(compilationErrors, texSource);

        String system = PROMPT_BASE + PROMPT_FIX;
        return call(system, List.of(text(userMessage)));
    }

    private String call(String systemPrompt, List<ContentBlockParam> userBlocks) {
        MessageCreateParams params = MessageCreateParams.builder()
                .model(properties.getModel())
                .maxTokens(properties.getMaxTokens())
                .thinking(ThinkingConfigAdaptive.builder().build())
                .system(systemPrompt)
                .addUserMessageOfBlockParams(userBlocks)
                .build();

        log.debug("Calling Anthropic API (model={}, maxTokens={})",
                properties.getModel(), properties.getMaxTokens());
        Message response = getClient().messages().create(params);
        ensureNotTruncated(response.stopReason(), properties.getMaxTokens());

        String tex = response.content().stream()
                .flatMap(block -> block.text().stream())
                .map(textBlock -> textBlock.text())
                .collect(Collectors.joining("\n"));
        return stripMarkdownFences(tex).trim();
    }

    private static ContentBlockParam text(String value) {
        return ContentBlockParam.ofText(TextBlockParam.builder().text(value).build());
    }

    /**
     * A response cut off by max_tokens is a truncated .tex: without this check
     * it would fail to compile and the fix loop could "repair" it into a
     * compilable but INCOMPLETE document, published silently. Better a FAILED
     * job with a clear message.
     */
    static void ensureNotTruncated(Optional<StopReason> stopReason, long maxTokens) {
        if (stopReason.filter(StopReason.MAX_TOKENS::equals).isPresent()) {
            throw new IllegalStateException(
                    "La IA agotó el máximo de tokens de salida (" + maxTokens
                            + "): el documento es demasiado largo y el resultado llegaría incompleto");
        }
    }

    /**
     * Defensive: the prompt forbids markdown, but if the model still wraps the
     * document in ```latex fences, unwrap it instead of failing to compile.
     */
    static String stripMarkdownFences(String tex) {
        String trimmed = tex.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }
        int firstNewline = trimmed.indexOf('\n');
        if (firstNewline < 0) {
            return trimmed;
        }
        String body = trimmed.substring(firstNewline + 1);
        int closingFence = body.lastIndexOf("```");
        return closingFence >= 0 ? body.substring(0, closingFence) : body;
    }

    /**
     * Lazy client so the app boots without a key; the pipeline fails with a
     * clear message if someone launches a job unconfigured.
     */
    private AnthropicClient getClient() {
        AnthropicClient current = client;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (client == null) {
                if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
                    throw new IllegalStateException(
                            "ANTHROPIC_API_KEY no configurada: el generador LaTeX no puede llamar a la API");
                }
                client = AnthropicOkHttpClient.builder()
                        .apiKey(properties.getApiKey())
                        .build();
            }
            return client;
        }
    }
}
