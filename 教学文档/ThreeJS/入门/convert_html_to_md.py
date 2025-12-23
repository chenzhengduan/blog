# -*- coding: utf-8 -*-
import os
import re
from html.parser import HTMLParser

class HTML2MarkdownConverter(HTMLParser):
    def __init__(self):
        super().__init__()
        self.markdown = []
        self.current_tag = []
        self.in_code = False
        self.code_lang = ""
        self.list_level = 0
        
    def handle_starttag(self, tag, attrs):
        self.current_tag.append(tag)
        attrs_dict = dict(attrs)
        
        if tag == 'h1':
            self.markdown.append('\n# ')
        elif tag == 'h2':
            self.markdown.append('\n## ')
        elif tag == 'h3':
            self.markdown.append('\n### ')
        elif tag == 'h4':
            self.markdown.append('\n#### ')
        elif tag == 'p':
            self.markdown.append('\n\n')
        elif tag == 'strong' or tag == 'b':
            self.markdown.append('**')
        elif tag == 'em' or tag == 'i':
            self.markdown.append('*')
        elif tag == 'code':
            parent = self.current_tag[-2] if len(self.current_tag) > 1 else None
            if parent == 'pre':
                self.in_code = True
                # 尝试从data-language属性获取语言
                lang = attrs_dict.get('data-language', 'javascript')
                self.markdown.append(f'\n```{lang}\n')
            else:
                self.markdown.append('`')
        elif tag == 'a':
            href = attrs_dict.get('href', '')
            self.markdown.append('[')
            self.link_href = href
        elif tag == 'img':
            src = attrs_dict.get('src', '')
            # 转换图片路径
            if '/_next/static/media/' in src:
                # 提取文件名
                match = re.search(r'/([^/]+\.[a-f0-9]+\.(png|gif|jpg|jpeg))$', src)
                if match:
                    filename_with_hash = match.group(1)
                    # 移除hash部分
                    clean_name = re.sub(r'\.[a-f0-9]{8}\.', '.', filename_with_hash)
                    src = f'./image/{clean_name}'
            self.markdown.append(f'![]({{src}})')
        elif tag == 'ul':
            self.list_level += 1
        elif tag == 'li':
            self.markdown.append(f'\n{"  " * (self.list_level - 1)}+ ')
            
    def handle_endtag(self, tag):
        if self.current_tag and self.current_tag[-1] == tag:
            self.current_tag.pop()
            
        if tag in ['h1', 'h2', 'h3', 'h4']:
            self.markdown.append('\n')
        elif tag == 'strong' or tag == 'b':
            self.markdown.append('**')
        elif tag == 'em' or tag == 'i':
            self.markdown.append('*')
        elif tag == 'code':
            if self.in_code:
                self.markdown.append('\n```\n')
                self.in_code = False
            else:
                self.markdown.append('`')
        elif tag == 'a':
            if hasattr(self, 'link_href'):
                self.markdown.append(f']({self.link_href})')
                delattr(self, 'link_href')
        elif tag == 'ul':
            self.list_level -= 1
            self.markdown.append('\n')
            
    def handle_data(self, data):
        # 跳过某些无关内容
        if any(skip in data for skip in ['nextra-', 'nx-', 'Toggle word wrap', 'Permalink for this section']):
            return
        if data.strip():
            # 在代码块中保留原始格式
            if self.in_code:
                self.markdown.append(data)
            else:
                # 清理多余空格
                cleaned = ' '.join(data.split())
                if cleaned:
                    self.markdown.append(cleaned)
    
    def get_markdown(self):
        result = ''.join(self.markdown)
        # 清理多余的空行
        result = re.sub(r'\n{3,}', '\n\n', result)
        return result.strip()

# 文件映射
file_mapping = {
    3: '案例：创建一个立方体',
    4: '摄像机',
    5: '相机控件',
    6: '灯光',
    7: '画布自适应',
    8: '三维坐标系',
    9: '常见几何体',
    10: '粒子效果',
    11: '鼠标射线拾取',
    12: '模型',
    13: '字体',
    14: '贴图材质',
    15: '阴影效果',
    16: 'EffectComposer选中效果',
    17: 'Gui调试界面',
    18: 'React 安装和配置',
    19: 'Shape 创建形状',
    20: 'TransformControls平移',
    21: 'uniapp搭建Three.js环境',
    22: 'Vue3 安装和配置',
    23: '体积光',
    24: '在Three.js中用Shader',
    25: '实现语边发光效果',
    26: '对象分组',
    27: '物理渲染',
    28: '辅助对象'
}

print("开始转换HTML文件到Markdown...")
for num in range(3, 29):
    html_file = str(num)
    md_file = f'{num}.{file_mapping[num]}.md'
    
    if os.path.exists(html_file):
        print(f'\n处理文件 {num}: {md_file}')
        try:
            with open(html_file, 'r', encoding='utf-8') as f:
                html_content = f.read()
            
            # 转换
            converter = HTML2MarkdownConverter()
            converter.feed(html_content)
            markdown_content = converter.get_markdown()
            
            # 写入文件
            with open(md_file, 'w', encoding='utf-8') as f:
                f.write(markdown_content)
            
            print(f'✓ 成功转换: {md_file}')
        except Exception as e:
            print(f'✗ 转换失败 {md_file}: {str(e)}')
    else:
        print(f'⚠ 源文件不存在: {html_file}')

print("\n\n转换完成！")
