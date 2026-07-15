package com.acainfo.shared.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the LaTeX generator/transcriber powered by the
 * Anthropic API (Claude) + tectonic. See docs/diseno-generador-latex.md.
 *
 * <p>The API key NEVER lives in the repo: it comes from the ANTHROPIC_API_KEY
 * environment variable (local dev) or /opt/acainfo/.env (prod).</p>
 */
@Configuration
@ConfigurationProperties(prefix = "app.anthropic")
@Getter
@Setter
public class AnthropicProperties {

    /**
     * Anthropic API key. Empty by default so the app boots without it;
     * the AI pipeline fails with a clear error if used unconfigured.
     */
    private String apiKey = "";

    /**
     * Model used for LaTeX generation/transcription. claude-opus-4-8 by default;
     * can be lowered to claude-sonnet-5 by property if quality allows (~40% cost).
     */
    private String model = "claude-opus-4-8";

    /**
     * Max output tokens per request. A 3-6 page .tex is 4-8k tokens;
     * 16000 leaves headroom without needing streaming.
     */
    private long maxTokens = 16000;

    /**
     * Max attempts to fix a .tex that does not compile by sending the
     * compilation errors back to Claude (on top of the initial attempt).
     */
    private int maxFixRetries = 2;

    /**
     * Tectonic (LaTeX compiler) settings.
     */
    private Tectonic tectonic = new Tectonic();

    @Getter
    @Setter
    public static class Tectonic {
        /**
         * Path to the tectonic binary. "tectonic" (PATH) in dev;
         * /app/bin/tectonic (mounted volume, NOT on the container PATH) in prod.
         */
        private String binaryPath = "tectonic";

        /**
         * Hard timeout for a single compilation, in seconds. First-ever run in a
         * fresh environment downloads the TeX bundle and needs a warm cache first
         * (see deploy notes in the design doc).
         */
        private long timeoutSeconds = 120;
    }
}
