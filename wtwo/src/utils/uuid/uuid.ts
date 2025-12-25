/**
 * 通用 UUID 生成器
 * - 优先使用 crypto.randomUUID()（RFC4122 v4）
 * - 兼容旧浏览器
 * - 加入时间戳减少碰撞概率
 */
export function generateUUID() {
    // 现代浏览器：原生 API
    if (typeof crypto !== 'undefined' && crypto.randomUUID) {
      return crypto.randomUUID();
    }
  
    // 有 crypto.getRandomValues()
    if (typeof crypto !== 'undefined' && crypto.getRandomValues) {
      const buf = new Uint8Array(16);
      crypto.getRandomValues(buf);
  
      // 调整版本号和变种
      buf[6] = (buf[6] & 0x0f) | 0x40; 
      buf[8] = (buf[8] & 0x3f) | 0x80;
  
      const hex = [...buf].map(b => b.toString(16).padStart(2, '0')).join('');
      return `${hex.slice(0,8)}-${hex.slice(8,12)}-${hex.slice(12,16)}-${hex.slice(16,20)}-${hex.slice(20)}`;
    }
  
    // 极老浏览器：Math.random + 时间戳
    let ts = Date.now();
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
      const r = (ts + Math.random() * 16) % 16 | 0;
      ts = Math.floor(ts / 16);
      return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
    });
}

export function isUUID(str:string) {
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
  return uuidRegex.test(str);
}