import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
    basePath: '/SoftwareEngineering-WS2025-just-donate',
    output: "export",
    dynamic: 'force-static',
    images: {
      unoptimized: true, // Disable image optimization
    },
};

export default nextConfig;
