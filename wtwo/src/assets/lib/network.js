/**
 * code:
 * http状态码用数字
 * 仅执行一次的dialog 如登录失效提醒
 */
const dialogMap = new Map();
export function dialogOnce(code, proFn) {
  if (!dialogMap.get(code)) {
    dialogMap.set(
      code,
      Promise.resolve(proFn()).finally(() => {
        // dialogMap.delete(code);
      }),
    );
  }
}