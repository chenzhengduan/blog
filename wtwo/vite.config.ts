import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import ssl from "@vitejs/plugin-basic-ssl";
import path, { resolve } from "path";
import fs from "fs";
import UnoCSS from "unocss/vite";
import AutoImport from "unplugin-auto-import/vite";
import Components from "unplugin-vue-components/vite";
import { ElementPlusResolver } from "unplugin-vue-components/resolvers";
import svgLoader from 'vite-svg-loader'
// import { sentryVitePlugin } from "@sentry/vite-plugin";
// function deleteSourceMaps(distPath:string){
//   const files = fs.readdirSync(distPath);
//   files.forEach(file => {
//     const filePath = path.join(distPath, file);
//     const stat = fs.statSync(filePath);
    
//     if (stat.isDirectory()) {
//       deleteSourceMaps(filePath);
//     } else if (file.endsWith('.map')) {
//       fs.unlinkSync(filePath);
//       console.log(`🗑️  已删除: ${filePath}`);
//     }
//   });
// }
// https://vitejs.dev/config/
export default defineConfig(({command, mode}) => {
  return {
    plugins: [
      ssl(),
      UnoCSS(),
      vue(),
      // // 只在构建生产包时才启用 Sentry 插件
      // command === 'build' && sentryVitePlugin({
      //   url:'https://sentry2.xiaogj.com',
      //   org: "xiaogj",
      //   project: "vue_wtwo_pc",
      //   authToken: "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",
      // }),
      svgLoader(),
      AutoImport({
        resolvers: [ElementPlusResolver()],
      }),
      Components({
        resolvers: [ElementPlusResolver()]
      }),
      // {
      //   name:'delete-sourcemaps',
      //   closeBundle(){
      //     // 构建完成后删除 SourceMap 文件
      //     const distPath=path.resolve(__dirname,'dist');
      //     if(fs.existsSync(distPath)){
      //       deleteSourceMaps(distPath);
      //     }
      //   },
        
      // }
    ],
    resolve: {
      alias: [
        {
          find: "@",
          replacement: resolve("./src"),
        },
        {
          find: "@common",
          replacement: resolve("./common"),
        },
      ],
      extensions: [".js", ".json", ".ts", ".vue", ".d.ts","scss"],
    },
    base: "/wtwo",
    build: {
      target: "es2018",
      cssTarget: "chrome61",
      outDir: "dist/",
      sourcemap: true,
      rollupOptions: {
        input: {
          main: resolve(__dirname, "index.html")
        },
      },
    },
    server: {
    //   cors:{
    //     origin:'http://172.16.30.38:5000'
    //   },
      host: true,
      port: 3011,
      proxy: {
        "/api": {
        //   target: "https://test.xiaogj.com/",
            target: "https://beta01.xiaogj.com/",
          // target:"http://172.16.30.38:5000/",
          changeOrigin: true
        },
        // "/exam": {
        //   target: "http://172.16.30.158:9002",
        //   // target: "https://wtwotest.xiaogj.com/",
        //   changeOrigin: true,
        //   secure: false,
        //   configure: (proxy, options) => {
        //     proxy.on('error', (err, req, res) => {
        //       console.log('proxy error', err);
        //     });
        //     proxy.on('proxyReq', (proxyReq, req, res) => {
        //       console.log('Sending Request to the Target:', req.method, req.url);
        //     });
        //     proxy.on('proxyRes', (proxyRes, req, res) => {
        //       console.log('Received Response from the Target:', proxyRes.statusCode, req.url);
        //     });
        //   },
        // },
      },
    },
    css:{
        preprocessorOptions: {
            scss: {
                silenceDeprecations:["legacy-js-api"]
            }
        }
    }
  }
});
