package com.streamflow.app.data.remote

object NetworkConfig {
    /**
     * Base URL for the Vega Next.js API server (D:\Type Script Project\Vega).
     * PROD_BASE_URL connects live to the online production API ("https://vegamovies3.vercel.app/api/").
     * LOCAL_DEV_BASE_URL ("http://10.0.2.2:3000/api/") is for local dev testing via `npm run dev`.
     */
    const val PROD_BASE_URL = "https://vegamovies3.vercel.app/api/"
    const val LOCAL_DEV_BASE_URL = "http://10.0.2.2:3000/api/"

    var baseUrl: String = PROD_BASE_URL

    const val API_KEY = "streamflow_vega_key_2026"

    /** 15s timeout for reliable live API network calls */
    const val TIMEOUT_SECONDS = 15L
}
