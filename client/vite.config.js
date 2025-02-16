import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import tailwindcss from "@tailwindcss/vite";
import mkcert from "vite-plugin-mkcert";
import { config } from "dotenv";

// Load environment variables from .env file
config();

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss(), mkcert()],
  define: {
    "process.env": process.env,
  },
  server: {
    host: "0.0.0.0",
    port: 443,
    https: true,
    headers: {
      "access-control-allow-origin": "*",
      "access-control-allow-headers": "*",
      "access-control-allow-methods": "*",
    },
    allowedHosts: true,
  },
  build: {
    outDir: "dist", // Cartella di destinazione per la build
    assetsDir: "assets", // Cartella per gli assets (immagini, CSS, JS)
    sourcemap: true, // Crea file di mappe per il debug
    minify: "esbuild", // Minifica il codice con esbuild (può essere anche 'terser' se preferisci)
    target: "esnext", // Target del codice per il browser
    rollupOptions: {
      // Configurazioni di Rollup per la build
      input: "index.html", // Punto di ingresso del tuo progetto
      output: {
        entryFileNames: "assets/[name].[hash].js", // Pattern per il nome dei file JS
        chunkFileNames: "assets/[name].[hash].js", // Pattern per il nome dei chunk JS
        assetFileNames: "assets/[name].[hash].[ext]", // Pattern per il nome degli asset
      },
    },
  },
});
