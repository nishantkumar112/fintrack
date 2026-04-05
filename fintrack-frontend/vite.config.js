import path from "node:path";
import { fileURLToPath } from "node:url";
import { defineConfig, loadEnv } from "vite";
import react from "@vitejs/plugin-react";

const projectRoot = path.dirname(fileURLToPath(import.meta.url));

const createApiProxy = (target, stripApiPrefix) => {
  const common = {
    target,
    changeOrigin: true,
    secure: false,
    ws: true,
  };

  if (stripApiPrefix) {
    return {
      "/api": {
        ...common,
        rewrite: (path) => path.replace(/^\/api/, ""),
      },
    };
  }

  return {
    "/api": {
      ...common,
    },
  };
};

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, projectRoot, "");
  const proxyTarget =
    env.VITE_API_PROXY_TARGET || env.VITE_API_BASE_URL || "http://localhost:8080";
  const stripApiPrefix = env.VITE_PROXY_STRIP_API_PREFIX !== "false";

  const proxy = createApiProxy(proxyTarget, stripApiPrefix);

  return {
    plugins: [react()],
    server: {
      proxy,
    },
    preview: {
      proxy,
    },
  };
});
