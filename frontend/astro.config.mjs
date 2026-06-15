import { existsSync } from "node:fs";
import { defineConfig } from "astro/config";
import solidJs from "@astrojs/solid-js";
import node from "@astrojs/node";
import tailwindcss from "@tailwindcss/vite";

if (existsSync(".env")) process.loadEnvFile(".env");

export default defineConfig({

  output: "server",
  adapter: node({ mode: "standalone" }),
  integrations: [solidJs()],
  server: {
    host: "0.0.0.0",
    port: 4321,
  },
  vite: {
    plugins: [tailwindcss()],
    server: {
      // Alle Backend-Aufrufe laufen über /api -> Gateway.
      // Das umgeht CORS, da Frontend und Backend für den Browser
      // als ein Origin erscheinen.
      proxy: {
        "/api": {
          target: process.env.BACKEND_TARGET ?? "http://localhost:8080",
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api/, ""),
        },
      },
    },
  },
});
