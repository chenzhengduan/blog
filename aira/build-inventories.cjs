// 从源码提取结构，不导出配置值、凭证或业务数据。
const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const root = 'D:/code/Wtwo-project';
const out = __dirname;
const JSON5 = require(path.join(root, 'wtwo-web/node_modules/json5'));
const rel = p => path.relative(root,p).replaceAll('\\','/');
const read = p => fs.readFileSync(p,'utf8').replace(/^\uFEFF/,'');
const write = (n,s) => fs.writeFileSync(path.join(out,n),s,'utf8');
function walk(p) { return fs.readdirSync(p,{withFileTypes:true}).flatMap(e => ['node_modules','bin','obj','.git','.omx'].includes(e.name) ? [] : e.isDirectory() ? walk(path.join(p,e.name)) : [path.join(p,e.name)]); }
const agent = path.join(root,'wtwo-server/WTwo.Agent.Web');
const central = path.join(root,'wtwo-server/WTwo.Bus.Common/WTwo.Bus.Common.Model/DBEntity/CentraHub');
let cfg = '# 附录：配置字段结构（不含环境值）\n\n自动读取本地 JSON/JSONC，仅记录字段路径和类型。配置文件存在不代表远端 Nacos 使用同值；数组的数字索引不能当成固定数量。\n\n';
for (const p of [...walk(path.join(agent,'WTwo.Agent.Webapi')).filter(p=> /appsettings[^/\\]*\.json$/.test(p)), ...walk(path.join(root,'wtwo-server/WTwo.AgentIM.Web/WTwo.AgentIM.GateWay/config')).filter(p=>p.endsWith('.json'))]) {
 cfg += `## ${rel(p)}\n\n| 字段 | 类型 |\n|---|---|\n`;
 function scan(v,k='') { if(v && typeof v==='object') for(const [a,b] of Object.entries(v)) scan(b,k?`${k}:${a}`:a); else cfg+=`| \`${k}\` | ${v===null?'null':typeof v} |\n`; }
 scan(JSON5.parse(read(p))); cfg+='\n';
}
write('附录-配置字段索引.md',cfg);
let schema = '# 附录：Agent 中心库实体字段\n\n从当前 C# FreeSql 实体自动提取。仅描述字段和类型，不含任何数据；不是可直接执行的迁移。默认值、索引、CHECK、外键、历史迁移顺序仍须核对 DbScripts 和数据库结构。\n\n';
for(const p of walk(central).filter(p=>/Agent.*DO\.cs$/.test(p))) {
 const lines=read(p).split(/\r?\n/); const table=read(p).match(/\bTable\(Name\s*=\s*"([^"]+)"/);
 if(!table)continue;
 schema+=`## ${table[1]}\n\n源码：\`${rel(p)}\`。\n\n| C# 字段 | 类型 | 列映射 | 源码行 |\n|---|---|---|---|\n`;
 let column='';
 for(let i=0;i<lines.length;i++) { const c=lines[i].match(/\bColumn\((.*)\)\]/); if(c) column=c[1]; const f=lines[i].match(/public\s+([\w<>?,.\[\] ]+)\s+(\w+)\s*\{\s*get;/); if(f) {schema+=`| ${f[2]} | \`${f[1]}\` | ${column.replaceAll('|','\\|')} | ${i+1} |\n`; column='';} }
 schema+='\n';
}
write('附录-数据库实体字段.md',schema);
let dep='# 附录：直接依赖版本\n\n来源是项目清单声明；不是漏洞扫描或线上锁定版本证明。前端/网关实际解析版本还取决于 lock 文件；NuGet 传递版本需以 restore 后的 assets 文件为准。\n\n';
for(const r of ['wtwo-web/package.json','wtwo-web/report-validator/package.json','wtwo-server/WTwo.AgentIM.Web/WTwo.AgentIM.GateWay/package.json','wtwo-server/.deploy/agent-scripts/package.json']) {
const j=JSON.parse(read(path.join(root,r))); dep+=`## ${r}\n\n| 分类 | 包 | 声明版本 |\n|---|---|---|\n`; for(const group of ['dependencies','devDependencies']) for(const [k,v] of Object.entries(j[group]||{})) dep+=`| ${group} | ${k} | ${v} |\n`;dep+='\n';
}
const csproj=[...walk(agent),...walk(path.join(root,'wtwo-server/WTwo.Common')), ...walk(path.join(root,'wtwo-server/WTwo.Bus.Common'))].filter(p=>p.endsWith('.csproj'));
for(const p of csproj) { const s=read(p);dep+=`## ${rel(p)}\n\n目标框架：${s.match(/<TargetFramework>(.*?)<\/TargetFramework>/)?.[1]||'见项目文件'}\n\n| 包 | 声明版本 |\n|---|---|\n`;for(const m of s.matchAll(/<PackageReference\s+Include="([^"]+)"\s+Version="([^"]+)"/g))dep+=`| ${m[1]} | ${m[2]} |\n`;dep+='\n'; }
write('附录-依赖版本清单.md',dep);
let api='# 附录：HTTP 控制器与 Hub 方法索引\n\n静态提取控制器声明和方法签名，方便定位完整 DTO。内网路由与公网网关前缀须分开；这里只列源码路径。访问控制、参数默认值和响应体以方法原文为准。\n\n';
const ctrl=walk(path.join(agent,'WTwo.Agent.Webapi/Controllers')).filter(p=>p.endsWith('.cs'));
for(const p of [...ctrl,path.join(agent,'WTwo.Agent.Service/Workbench/Hubs/WorkbenchHub.cs')]) {
const ls=read(p).split(/\r?\n/);api+=`## ${path.basename(p)}\n\n源码：\`${rel(p)}\`\n\n\`\`\`csharp\n`;for(let i=0;i<ls.length;i++)if(/\[(Route|HttpGet|HttpPost|HttpPut|HttpDelete|NoAuth|NoTenant|WorkbenchAccess|AgentManageAuth)|^\s*public\s+(?:async\s+)?(?:Task|CallResult|IActionResult|ActionResult|ValueTask)/.test(ls[i]))api+=`${i+1}: ${ls[i].trim()}\n`; api+='```\n\n';}
write('附录-接口与Hub方法索引.md',api);
let skills='# 附录：仓库 Skill 参考目录\n\n以下为 Docs/skill 的本地参考快照，生产正本在 tAgentCfgSkill。存在文件不等于已绑定、已启用或已发布。正文参见源码，复刻时应取得脱敏的数据库版本导出。\n\n| Skill 目录 | 文件 | SHA256 |\n|---|---|---|\n';
for(const p of walk(path.join(agent,'Docs/skill')).filter(p=>path.basename(p)==='SKILL.md')) { const bytes=fs.readFileSync(p);skills+=`| ${path.basename(path.dirname(p))} | \`${rel(p)}\` | ${crypto.createHash('sha256').update(bytes).digest('hex')} |\n`; }
write('附录-Skill目录索引.md',skills);
console.log('Generated 6 structure-only inventories.');
