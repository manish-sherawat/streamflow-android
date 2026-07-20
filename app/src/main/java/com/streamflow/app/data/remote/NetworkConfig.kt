package com.streamflow.app.data.remote

object NetworkConfig {
    /**
     * Default Base URL for the Neon PostgreSQL API backend.
     * Replace this placeholder once your backend domain / API server is live.
     * Example: "https://api.streamflow.app/" or "https://your-nextjs-site.com/api/"
     */
    const val DEFAULT_BASE_URL = "https://vegamovies3.vercel.app/api/"

    var baseUrl: String = DEFAULT_BASE_URL

    const val API_KEY = "streamflow_vega_key_2026"

    const val TIMEOUT_SECONDS = 30L
}
