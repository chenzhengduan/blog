"""文档索引和静态验证；不连接外部服务、不导出源码中的秘密值。"""
from pathlib import Path
import re, json, hashlib

ROOT = Path('D:/code/Wtwo-project')
OUT = Path(__file__).resolve().parent
SKIP = {'node_modules', 'bin', 'obj', '.git', '.omx', 'dist', 'outputs', 'output'}

def files(base):
    for p in base.iterdir():
        if p.name in SKIP:
            continue
        if p.is_dir():
            yield from files(p)
        else:
            yield p

docs = sorted(OUT.glob('*.md'))
docs = [p for p in docs if p.name not in {'AIRA-技术框架全稿.md', '附录-源码导航.md', '验证记录.md'}]
text = '\n'.join(p.read_text(encoding='utf-8-sig') for p in docs)
names = set(re.findall(r'([\w.-]+\.(?:cs|csproj|ts|vue|mjs|json|yml|md))', text))
names -= {'README.md', 'AGENTS.md', 'SKILL.md', 'package.json', 'package-lock.json', 'tsconfig.json', 'Program.cs', '.gitlab-ci.yml'}
paths = []
for base in [ROOT/'wtwo-server', ROOT/'wtwo-web']:
    for p in files(base):
        if p.name in names:
            paths.append(p)
for rel in ['wtwo-server/WTwo.Agent.Web/WTwo.Agent.Webapi/Program.cs', 'wtwo-server/.gitlab-ci.yml', 'wtwo-web/.gitlab-ci.yml', 'wtwo-web/package.json', 'wtwo-server/WTwo.AgentIM.Web/WTwo.AgentIM.GateWay/package.json', 'wtwo-server/.deploy/Dockerfile.agent', 'wtwo-server/.deploy/Dockerfile.agentim-gateway']:
    p=ROOT/rel
    if p.exists(): paths.append(p)
paths = sorted(set(paths))
records=[{'path': p.relative_to(ROOT).as_posix(), 'sha256':hashlib.sha256(p.read_bytes()).hexdigest(), 'lines':len(p.read_text(encoding='utf-8-sig').splitlines())} for p in paths]
nav='# 附录：源码导航与快照指纹\n\n根目录：`D:/code/Wtwo-project`。下表按正文涉及的文件名自动匹配，重名文件保留所有候选，不表示每个候选都被正文引用。精确证据以章节给出的路径、类名和行号为准；行号随代码变化，SHA256 用于辨别快照变化。没有复制源码内容或配置值。\n\n| 源码路径 | 行数 | SHA256 |\n|---|---:|---|\n'
nav+=''.join(f"| `{r['path']}` | {r['lines']} | {r['sha256']} |\n" for r in records)
(OUT/'附录-源码导航.md').write_text(nav,encoding='utf-8')
(OUT/'source-manifest.json').write_text(json.dumps({'date':'2026-10-29','sourceRoot':str(ROOT),'files':records},ensure_ascii=False,indent=2),encoding='utf-8')

chapters = sorted(OUT.glob('0[0-6]-*.md'))+[OUT/'附录-提示词参考快照.md']
missing=[str(p.name) for p in chapters if not p.exists()]
if len(chapters)!=8 or missing:
    raise SystemExit('Chapters not complete: '+str(missing))
full='# AIRA / WTwo Workbench 技术框架全稿\n\n提取日期：2026-10-29。正文合订版；版本、验证边界和附录导航见 [README](./README.md)。本稿不包含真实凭证、线上数据库正本或业务数据。\n\n'
for p in chapters:
    full+='---\n\n'+p.read_text(encoding='utf-8-sig')+'\n\n'
(OUT/'AIRA-技术框架全稿.md').write_text(full,encoding='utf-8')

issues=[]
all_docs=list(OUT.rglob('*.md'))
for p in all_docs:
    s=p.read_text(encoding='utf-8-sig')
    if '\ufffd' in s: issues.append(f'{p.name}: replacement character')
    outside=[]
    active=None
    for line in s.splitlines():
        fence=re.match(r'^ {0,3}(`{3,}|~{3,})(.*)$',line)
        if active:
            if fence and fence[1][0]==active[0] and len(fence[1])>=len(active) and not fence[2].strip(): active=None
        elif fence: active=fence[1]
        else: outside.append(line)
    if active: issues.append(f'{p.name}: unbalanced fences')
    for target in re.findall(r'\]\(([^)]+)\)','\n'.join(outside)):
        target=target.strip('<>')
        if target.startswith(('http:', 'https:', '#','D:', '/')): continue
        if not (p.parent/target.split('#')[0]).exists(): issues.append(f'{p.name}: broken local link {target}')
for p in (OUT/'configs').glob('*.json'): json.loads(p.read_text(encoding='utf-8-sig'))

# 查找源码明确以 secret/password/key 命名的字符串字面量；只输出命中文档的位置，不打印值。
candidates=set()
candidate_origins={}
secret_name=r'\b\w*(?:ApiKey|AppKey|Secret|Password|EncryptKey|DecryptKey)\b'
secret_sources=list(files(ROOT/'wtwo-server/WTwo.Agent.Web'))+list(files(ROOT/'wtwo-server/WTwo.Bus.Common'))
for p in secret_sources:
    if p.suffix not in {'.cs','.ts','.json'}: continue
    s=p.read_text(encoding='utf-8-sig')
    for m in re.finditer(secret_name+r'[^\r\n="]{0,100}=\s*"([^"\r\n]+)"',s,re.I):
        v=m.group(1)
        if len(v)>=12 and re.search('[0-9]',v) and not re.search(r'[\s<>/\\]',v):
            candidates.add(v)
            candidate_origins[v]=str(p.relative_to(ROOT))+':'+m[0].split('=')[0].strip()
hits=[]
for p in all_docs+list((OUT/'configs').glob('*.json')):
    s=p.read_text(encoding='utf-8-sig')
    for v in candidates:
        if v in s:
            hits.append(p.name)
            print(json.dumps({'possibleSecretOrigin':candidate_origins[v],'document':p.name,'line':s[:s.index(v)].count('\n')+1},ensure_ascii=False))
    if re.search(r'\bsk-[A-Za-z0-9_-]{20,}|-----BEGIN (?:RSA |EC )?PRIVATE KEY-----',s): hits.append(p.name)
if hits: issues.append('Potential copied secrets in: '+','.join(sorted(set(hits))))

snapshots=re.findall(r'\| `(wtwo-server/[^`]+)` \| `([a-f0-9]{64})` \|',(OUT/'附录-提示词参考快照.md').read_text(encoding='utf-8'))
for rel,digest in snapshots:
    if not (ROOT/rel).exists() or hashlib.sha256((ROOT/rel).read_bytes()).hexdigest()!=digest: issues.append('Snapshot hash mismatch: '+rel)
report={'sourceFilesIndexed':len(records),'markdownFiles':len(all_docs),'chapters':len(chapters),'snapshotSourceHashesVerified':len(snapshots),'secretCandidatesCompared':len(candidates),'issues':issues}
(OUT/'validation.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(report,ensure_ascii=False))
if issues: raise SystemExit(1)
